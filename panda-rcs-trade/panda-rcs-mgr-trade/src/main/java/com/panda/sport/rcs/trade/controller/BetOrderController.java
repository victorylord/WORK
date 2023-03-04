package com.panda.sport.rcs.trade.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.enums.MarketStatusEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.trade.log.format.LogFormatBean;
import com.panda.sport.rcs.trade.log.format.LogFormatPublicBean;
import com.panda.sport.rcs.trade.enums.LogTypeEnum;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.log.LogContext;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.mapper.TOrderMapper;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.pojo.vo.OrderTakingVo;
import com.panda.sport.rcs.trade.enums.HandleStatusEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.utils.StringUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.utils.i18n.I18nBean;
import com.panda.sport.rcs.vo.OrderSecondConfigVo;
import com.panda.sport.rcs.vo.PauseOrderVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.common.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig;
import com.panda.sport.rcs.trade.vo.betOrder.SetHalftimeVo;
import com.panda.sport.rcs.vo.HttpResponse;

import lombok.extern.slf4j.Slf4j;

import static com.panda.sport.rcs.constants.RedisKey.EXPRIY_TIME_5_MINS;
import static com.panda.sport.rcs.constants.RedisKey.RCS_PAUSE_ORDER;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  注单
 * @Date: 2020-01-31 18:28
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@RequestMapping(value = "/betOrder")
@Slf4j
@Component
public class BetOrderController {
    @Autowired
    TOrderDetailExtService tOrderDetailExtService;

    @Autowired
    private RcsMatchOrderAcceptConfigService rcsMatchOrderAcceptConfigService;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;


    @Autowired
    private RedisClient redisClient;

    @Autowired
    private RcsOrderSecondConfigService rcsOrderSecondConfigService;

    @Autowired
    private StandardMatchInfoService standardMatchInfoService;

    @Autowired
    private TOrderMapper orderMapper;

    @Autowired
    private MarketCategorySetService marketCategorySetService;

    @Autowired
    private LogFormatService logFormatService;

    @Autowired
    private SportMatchViewService sportMatchViewService;
    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Integer>
     * @Description 设置赛事半场休息
     * @Param [vo]
     * @Author toney
     * @Date 21:00 2020/1/31
     **/
    @RequestMapping(value = "/setHalftime", method = RequestMethod.GET)
    @LogAnnotion(name = "设置赛事半场休息", keys = {"matchId", "tournamentId", "state"}, title = {"赛事ID", "联赛Id", "状态"})
    public HttpResponse<Integer> setHalftime(SetHalftimeVo vo) {
        try {
            log.info("::{}::设置赛事半场休息:{}，操盘手:{}", CommonUtil.getRequestId(vo.getMatchId(),vo.getTournamentId()), JSONObject.toJSONString(vo),TradeUserUtils.getUserIdNoException());
            String cacheKey = getMatchAcceptConfigCacheKey(vo.getMatchId());
            RcsMatchOrderAcceptConfig rcsMatchOrderAcceptConfig = rcsMatchOrderAcceptConfigService.selectRcsMatchOrderAcceptConfigById(vo.getMatchId());
            if (rcsMatchOrderAcceptConfig == null) {
                rcsMatchOrderAcceptConfig = rcsMatchOrderAcceptConfigService.init(vo.getMatchId());
            }
            if (!vo.getState().equals(rcsMatchOrderAcceptConfig.getHalfTime())) {
                rcsMatchOrderAcceptConfig.setHalfTime(vo.getState());
                rcsMatchOrderAcceptConfigService.updateById(rcsMatchOrderAcceptConfig);


                redisClient.setExpiry(cacheKey, JsonFormatUtils.toJson(Arrays.asList(rcsMatchOrderAcceptConfig)), Long.valueOf(60 * 60 * 2));
            }


            return HttpResponse.success();
        } catch (Exception e) {
            log.error("::{}::设置赛事半场休息:{}", CommonUtil.getRequestId(vo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Integer>
     * @Description 订单处理
     * @Param [vo]
     * @Author toney
     * @Date 21:00 2020/1/31
     **/
    @LogFormatAnnotion
    @RequestMapping(value = "/orderTaking", method = RequestMethod.POST)
    public HttpResponse orderTaking(@RequestBody OrderTakingVo vo) {
        String state = vo.getState();
        Long matchId = vo.getMatchId();
        try {
            Assert.notNull(matchId, "赛事matchId不能为空");
            Assert.notNull(state, "状态state不能为空");
            if (HandleStatusEnum.THREE.getCode().equals(state)
                    || HandleStatusEnum.FOUR.getCode().equals(state)
                    || HandleStatusEnum.FIVE.getCode().equals(state)) {
                if (vo.getIds().size() <= 0) {
                    return HttpResponse.failure("请选择注单");
                }
            }
            return tOrderDetailExtService.orderTakingBatch(vo);
        } catch (Exception e) {
            log.error("::{}::订单处理orderTaking:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.failure("操作失败");
        }
    }


    /**
     * 赛事事件配置
     *
     * @param matchId
     * @return
     */
    private String getMatchEventConfigCacheKey(Long matchId) {
        return String.format(RedisKeys.MATCH_EVENT_CONFIG, matchId);
    }

    /**
     * 赛事联赛配置
     *
     * @param TournamentId
     * @return
     */
    private String getTournamentEventConfigCacheKey(Long TournamentId) {
        return String.format(RedisKeys.TOURNAMENT_EVENT_CONFIG, TournamentId);
    }

    /**
     * 赛事事件配置
     *
     * @param matchId
     * @return
     */
    private String getMatchAcceptConfigCacheKey(Long matchId) {
        return String.format(RedisKeys.MATCH_ACCEPT_CONFIG, matchId);
    }

    /**
     * 赛事联赛配置
     *
     * @param TournamentId
     * @return
     */
    private String getTournamentAcceptConfigCacheKey(Long TournamentId) {
        return String.format(RedisKeys.TOURNAMENT_ACCEPT_CONFIG, TournamentId);
    }

    /**
     * 根据操盘登录用户和赛事，获取玩法集一键秒接配置
     *
     * @param vo
     * @return
     */
    @PostMapping("/findOrderSecondConfig")
    public HttpResponse<List<OrderSecondConfigVo>> queryOrderSecondConfig(@RequestBody OrderSecondConfigVo vo,@RequestHeader(value="lang",required = false)String lang) {
        try {
            if (vo.getSportId() == null) {
                return HttpResponse.fail("赛种不能为空");
            }
            if (vo.getMatchInfoId() == null) {
                return HttpResponse.fail("标准赛事id不能为空");
            }
            Integer userId = TradeUserUtils.getUserId();
            vo.setUid(Long.valueOf(userId));
            List<OrderSecondConfigVo> orderSecondConfiglist = rcsOrderSecondConfigService.queryOrderSecondConfig(vo);
            if(orderSecondConfiglist!=null || orderSecondConfiglist.size()>0){
                orderSecondConfiglist.stream().forEach(item->{
                    if(item.getPlaySetName()!=null && StringUtils.isNotBlank(item.getPlaySetName())&& !item.getPlaySetName().equals("其他")){
                        JSONObject objectJson=JSONObject.parseObject(item.getPlaySetName());
                        if(objectJson!=null && !objectJson.getString(lang).equals("")){

                            item.setPlaySetName(objectJson.getString(lang));
                        }else{
                            item.setPlaySetName(objectJson.getString("zs"));
                        }
                    }
                    if(lang.equals("en") && item.getPlaySetName().equals("其他")){
                        item.setPlaySetName("other");
                    }
                });
            }
            return HttpResponse.success(orderSecondConfiglist);
        } catch (Exception ex) {
            log.error("::{}::获取玩法集一键秒接配置:{}", CommonUtil.getRequestId(vo.getMatchInfoId()), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    @PostMapping("/saveOrderSecondConfig")
    @LogFormatAnnotion
    public HttpResponse<List<String>> saveOrderSecondConfig(@RequestBody OrderSecondConfigVo vo) {
        try {
            Integer userId = TradeUserUtils.getUserId();
            vo.setUid(userId.longValue());
            String voTrader = vo.getTrader();
            Long matchId = vo.getMatchInfoId();
            int secondStatus = vo.getSecondStatus();

            if(secondStatus==2){
                List<String> traders = rcsOrderSecondConfigService.selectOrderSecondTraders(vo);
                return HttpResponse.success(traders);
            }
            if (CollectionUtils.isEmpty(vo.getPlaySetList())) {
                return HttpResponse.failure("请选择玩法集");
            }
            if (vo.getMatchInfoId() == null) {
                return HttpResponse.failure("未传matchInfoId");
            }
            if(!sportMatchViewService.isOwnTrade(matchId,userId)){
                return HttpResponse.failure("您不是本场赛事操盘手，无法使用一键秒接");
            }
            String key = RCS_PAUSE_ORDER + matchId;
            String value = redisClient.get(key);
            String trader ="";
            if (StringUtils.isNotBlank(value)) {
                PauseOrderVo  pauseOrderVo = JsonFormatUtils.fromJson(value, PauseOrderVo.class);
                trader = pauseOrderVo.getTrader();
            }
            if (StringUtils.isNotBlank(trader)) {
                if (!voTrader.equals(trader)) {
                    return HttpResponse.failure(trader + "正在暂停接单，无法开启");
                }else {
                    return HttpResponse.failure("正在暂停接单，无法开启");
                }
            }

            MatchMarketLiveBean matchMarketLiveBean = marketCategorySetService.getMatchInfo(null, vo.getMatchInfoId());

            if (matchMarketLiveBean == null) {
                return HttpResponse.failure("未查询到赛事信息");
            }

            if (matchMarketLiveBean.getMatchStatus().equals(3)) {
                return HttpResponse.failure("赛事已结束");
            }

            if (MarketStatusEnum.CLOSE.getState() == matchMarketLiveBean.getOperateMatchStatus()) {
                return HttpResponse.failure("赛事处于" + MarketStatusEnum.CLOSE.getName() + "状态，不能开启一键秒接");
            }
            if (MarketStatusEnum.SEAL.getState() == matchMarketLiveBean.getOperateMatchStatus()) {
                return HttpResponse.failure("赛事处于" + MarketStatusEnum.SEAL.getName() + "状态，不能开启一键秒接");
            }
            if (MarketStatusEnum.LOCK.getState() == matchMarketLiveBean.getOperateMatchStatus()) {
                return HttpResponse.failure("赛事处于" + MarketStatusEnum.LOCK.getName() + "状态，不能开启一键秒接");
            }

            rcsOrderSecondConfigService.saveOrderSecondConfig(vo);

            List<String> traders = rcsOrderSecondConfigService.selectOrderSecondTraders(vo);
            return HttpResponse.success(traders);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failure(e.getMessage());
        }
    }

    /**
     * @param vo
     * @return
     */
    @PostMapping("/pauseMatchOrder")
    @LogFormatAnnotion
    public HttpResponse<PauseOrderVo> pauseMatchOrder(@RequestBody OrderSecondConfigVo vo) {

        try {
            if (vo.getMatchInfoId() == null) {
                return HttpResponse.failure("标准赛事id不能为空");
            }
            Integer traderId = TradeUserUtils.getUserId();
            Long matchId = vo.getMatchInfoId();
            String trader = vo.getTrader();
            if(!sportMatchViewService.isOwnTrade(matchId,traderId)){
                return HttpResponse.failure("您不是本场赛事操盘手，无法使用暂停注单");
            }
            List<String> traders = rcsOrderSecondConfigService.selectOrderSecondTraders(vo);
            if (!CollectionUtils.isEmpty(traders)) {
                if (!traders.contains(trader)) {
                    return HttpResponse.failure(JsonFormatUtils.toJson(traders) + "正在一键秒接，无法开启");
                }else {
                    return HttpResponse.failure("正在一键秒接，无法开启");
                }
            }
            PauseOrderVo pauseOrderVo = null;
            String key = RCS_PAUSE_ORDER + matchId;
            Integer secondStatus = vo.getSecondStatus();
            boolean isLog=false;
            if (1 == secondStatus) {
                if (redisClient.exist(key)) {
                    String value = redisClient.get(key);
                    if (StringUtils.isNotBlank(value)) {
                        pauseOrderVo = JsonFormatUtils.fromJson(value, PauseOrderVo.class);
                        if(!pauseOrderVo.getTraderId().equals(traderId)){
                            return HttpResponse.failure(pauseOrderVo.getTrader()+"正在暂停接拒，无法开启");
                        }
                    }
                    pauseOrderVo.setPauseTime(300L-(System.currentTimeMillis()-pauseOrderVo.getPauseTime())/1000);
                } else {
                    pauseOrderVo = new PauseOrderVo()
                            .setPauseTime(System.currentTimeMillis())
                            .setTrader(trader)
                            .setTraderId(traderId);
                    redisClient.setExpiry(key, JsonFormatUtils.toJson(pauseOrderVo), EXPRIY_TIME_5_MINS);
                    pauseOrderVo.setPauseTime(300L);
                    isLog=true;
                }
            } else {
                redisClient.delete(key);
                isLog=true;
            }
            //记录操作日志
            if(isLog) logFormatService.pauseMatchOrderLog(vo);

            return HttpResponse.success(pauseOrderVo);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failure("操作失败");
        }
    }
}
