package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.panda.merge.dto.ConfigMarketOddsStatusDTO;
import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;
import com.panda.sport.rcs.trade.service.ApiService;
import com.panda.sport.rcs.trade.service.MongoDbService;
import com.panda.sport.rcs.trade.service.SecondaryPlayService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.MarketDisableVO;
import com.panda.sport.rcs.vo.secondary.BasketballTwoPlaySet;
import com.panda.sport.rcs.vo.secondary.BasketballTwoPlaySetInfo;
import com.panda.sport.rcs.vo.secondary.BasketballTwoReqVo;
import com.panda.sport.rcs.vo.secondary.FootballTwoPlaySetInfo;
import com.panda.sport.rcs.vo.secondary.FootballTwoReqVo;
import com.panda.sport.rcs.vo.trade.MatchInfoReqVo;
import com.panda.sport.rcs.vo.trade.OddsModeReqVo;
import com.panda.sport.rcs.vo.trade.WaterDiffRelevanceReqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 操盘
 * @Author : Paca
 * @Date : 2020-11-05 15:51
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Component
@RestController
@RequestMapping(value = "/trade")
public class TradeController {

    @Autowired
    private MongoDbService mongoDbService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private SecondaryPlayService secondaryPlayService;
    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;
    @Autowired
    private RcsPredictBetOddsService predictBetOddsService;
    @Autowired
    private MarketCategorySetService marketCategorySetService;
    @Autowired
    private ApiService apiService;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;

    /**
     * 修改投注项模式
     *
     * @param reqVo
     * @return
     */
    @PostMapping("/updateOddsMode")
    @OperateLog(operateType = OperateLogEnum.ODDS_UPDATE)
    public HttpResponse updateOddsMode(@RequestBody OddsModeReqVo reqVo) {
        log.info("::{}::修改投注项模式:{}，操盘手:{}",CommonUtil.getRequestId(reqVo.getMatchId(),reqVo.getPlayId()), JSONObject.toJSONString(reqVo), TradeUserUtils.getUserIdNoException());
        String linkId = CommonUtils.mdcPut();
        HttpResponse httpResponse;
        RcsMatchMarketConfig rcsMatchMarketConfig = rcsMatchMarketConfigService.getMaxAndMinOddsValue(reqVo.getMatchId(), reqVo.getPlayId());
        if(Objects.nonNull(rcsMatchMarketConfig)){
            BigDecimal oddsValue = new BigDecimal(reqVo.getOddsValue()+"").divide(new BigDecimal("100000"));
            if(oddsValue.compareTo(rcsMatchMarketConfig.getMinOdds()) < 0){
                return HttpResponse.failure("修改投注项模式错误："+oddsValue+"赔率修改低于模板最小赔率"+rcsMatchMarketConfig.getMinOdds());
            }
            if(oddsValue.compareTo(rcsMatchMarketConfig.getMaxOdds()) > 0){
                return HttpResponse.failure("修改投注项模式错误："+oddsValue+"赔率修改超出模板最大赔率"+rcsMatchMarketConfig.getMaxOdds());
            }
        }
        try {
            ConfigMarketOddsStatusDTO config = new ConfigMarketOddsStatusDTO();
            config.setId(reqVo.getOddsId());
            config.setStandardMatchInfoId(reqVo.getMatchId());
            config.setStandardCategoryId(reqVo.getPlayId());
            config.setOddsType(reqVo.getOddsType());
            config.setOddsValue(reqVo.getOddsValue());
            config.setStatus(reqVo.getMode());
            config.setOperaterId(TradeUserUtils.getUserIdNoException().longValue());
            apiService.updateOddsMode(config, linkId + "_trade");
            httpResponse = HttpResponse.success();
        } catch (RpcException e) {
            log.error("::{}::修改投注项模式,融合RPC异常:{}", CommonUtil.getRequestId(reqVo.getMatchId(),reqVo.getPlayId()), e.getMessage(), e);
            httpResponse = HttpResponse.failure("融合RPC异常：" + e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::修改投注项模式:{}", CommonUtil.getRequestId(reqVo.getMatchId(),reqVo.getPlayId()), e.getMessage(), e);
            httpResponse = HttpResponse.failure(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::修改投注项模式异常:{}", CommonUtil.getRequestId(reqVo.getMatchId(),reqVo.getPlayId()), e.getMessage(), e);
            httpResponse = HttpResponse.failure("修改投注项模式异常：" + e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
        httpResponse.setLinkId(linkId);
        return httpResponse;
    }

    @PostMapping("/marketDisableList")
    public HttpResponse marketDisableList(@RequestBody MarketDisableVO marketDisableVO) {
        List<StandardSportMarket> list = tradeService.marketDisableList(marketDisableVO);
        return HttpResponse.success(list);
    }

    @PostMapping("/marketDisable")
    @LogAnnotion(name = "盘口弃用",
            keys = {"sportId", "matchId", "playId", "marketId", "marketType", "marketValue", "disableFlag"},
            title = {"赛种", "赛事ID", "玩法ID", "盘口ID", "盘口类型", "盘口值", "弃用标志"})
    @OperateLog(operateType = OperateLogEnum.CONFIG_UPDATE)
    public HttpResponse marketDisable(@RequestBody MarketDisableVO marketDisableVO) {
        try {
            CommonUtils.mdcPut();
            Long matchId = marketDisableVO.getMatchId();
            if (matchId == null || matchId <= 0) {
                return HttpResponse.fail("参数matchId传值有误！");
            }
            log.info("::{}::盘口弃用:{}，操盘手:{}",CommonUtil.getRequestId(marketDisableVO.getMatchId(),marketDisableVO.getPlayId()), JSONObject.toJSONString(marketDisableVO), TradeUserUtils.getUserIdNoException());
//            boolean pass = rcsTradingAssignmentService.tradeJurisdictionByPlayId(marketDisableVO.getSportId(), matchId, marketDisableVO.getPlayId(), marketDisableVO.getMarketType());
            RcsMatchMarketConfig config = new RcsMatchMarketConfig(matchId,marketDisableVO.getPlayId());
            config.setSportId(marketDisableVO.getSportId().intValue());
            config.setMatchType(marketDisableVO.getMarketType());
            boolean pass = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!pass) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            String linkId = tradeService.marketDisable(marketDisableVO);
            return HttpResponse.success(linkId);
        } catch (RpcException e) {
            log.error("::{}::盘口弃用,融合RPC异常{}", CommonUtil.getRequestId(marketDisableVO.getMatchId(),marketDisableVO.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("融合RPC异常：" + e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::盘口弃用:{}", CommonUtil.getRequestId(marketDisableVO.getMatchId(),marketDisableVO.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::盘口弃用:{}", CommonUtil.getRequestId(marketDisableVO.getMatchId(),marketDisableVO.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("修改盘口状态异常：" + e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    /**
     * 水差关联
     *
     * @param reqVo
     * @return
     * @author Paca
     */
    @PostMapping("/waterDiffRelevance")
    public HttpResponse waterDiffRelevance(@RequestBody WaterDiffRelevanceReqVo reqVo) {
        try {
            CommonUtils.mdcPut();
            reqVo.paramCheck();
            RcsMatchMarketConfig config = new RcsMatchMarketConfig(reqVo.getMatchId(),reqVo.getPlayId());
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.BASKETBALL.getId().intValue());
            boolean pass = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!pass) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::水差关联:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String linkId = tradeService.waterDiffRelevance(reqVo);
            return HttpResponse.success(linkId);
        } catch (RcsServiceException e) {
            log.error("::{}::水差关联:{}", CommonUtil.getRequestId(reqVo.getMatchId(),reqVo.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::水差关联异常:{}", CommonUtil.getRequestId(reqVo.getMatchId(),reqVo.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("水差关联异常：" + e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    /**
     * 获取赛事信息
     *
     * @param matchInfoReqVo
     * @return
     */
    @PostMapping("/getMatchInfo")
    public HttpResponse getMatchInfo(@RequestBody MatchInfoReqVo matchInfoReqVo) {
        try {
            MatchMarketLiveBean result = mongoDbService.getMatchInfo(matchInfoReqVo);
            return HttpResponse.success(result);
        } catch (RcsServiceException e) {
            log.error("::{}::获取赛事信息:{}", CommonUtil.getRequestId(matchInfoReqVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 查询位置实货量
     *
     * @param matchId
     * @return
     */
    @GetMapping("/queryPlaceBetNums")
    public HttpResponse queryPlaceBetNums(@RequestParam("matchId") Long matchId, @RequestParam("seriesType") Integer seriesType) {
        try {
            Map<String, List<RcsPredictBetOdds>> resultMap = predictBetOddsService.queryPlaceBetNums(matchId, seriesType);
            return HttpResponse.success(resultMap);
        } catch (RcsServiceException e) {
            log.error("::{}::查询位置实货量:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 篮球两项盘玩法集
     *
     * @return
     */
    @PostMapping("/basketball/twoPlaySet")
    public HttpResponse basketballTwoPlaySet(@RequestHeader(value="lang",required = false)String lang) {
        try {
            List<BasketballTwoPlaySet> result = secondaryPlayService.basketballTwoPlaySet(lang);
            return HttpResponse.success(result);
        } catch (RcsServiceException e) {
            log.error("::{}::篮球两项盘玩法集:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }catch (Exception e) {
            log.error("::{}::篮球两项盘玩法集:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 篮球两项盘
     *
     * @param reqVo
     * @return
     */
    @PostMapping("/basketball/twoList")
    public HttpResponse basketballTwoList(@RequestBody BasketballTwoReqVo reqVo) {
        try {
            List<List<BasketballTwoPlaySetInfo>> result = secondaryPlayService.basketballTwoList(reqVo);
            return HttpResponse.success(result);
        } catch (Exception e) {
			log.error("::{}::篮球两项盘异常:{}", CommonUtil.getRequestId(reqVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("篮球两项盘异常：" + e.getMessage());
        }
    }

    /**
     * 足球两项盘
     *
     * @param reqVo
     * @return
     */
    @PostMapping("/football/twoList")
    public HttpResponse<List<FootballTwoPlaySetInfo>> footTwoList(@RequestBody FootballTwoReqVo reqVo) {
        Long matchId = reqVo.getMatchId();
        if (matchId == null || matchId <= 0) {
            return HttpResponse.fail("参数matchId传值有误！");
        }
        Integer oddBusiness = reqVo.getLiveOddBusiness();
        if (oddBusiness == null) {
            return HttpResponse.fail("参数liveOddBusiness传值有误！");
        }

        List<Long> categorySetIds = reqVo.getCategorySetIds();
        if (CollectionUtils.isEmpty(categorySetIds)) {
            return HttpResponse.fail("参数categorySetIds传值有误！");
        }
        try {
            List<FootballTwoPlaySetInfo> infos = secondaryPlayService.footballTwoPlaySet(reqVo);
            return HttpResponse.success(infos);
        } catch (Exception e) {
			log.error("::{}::足球两项盘异常:{}", CommonUtil.getRequestId(reqVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("足球两项盘异常：" + e.getMessage());
        }
    }

    /**
     * 次要玩法
     *
     * @param reqVo
     * @return
     */
    @PostMapping("/categorySet")
    public HttpResponse<List<FootballTwoPlaySetInfo>> tennisCategorySet(@RequestBody FootballTwoReqVo reqVo) {
        Long matchId = reqVo.getMatchId();
        if (matchId == null || matchId <= 0) {
            return HttpResponse.fail("参数matchId传值有误！");
        }
        Integer oddBusiness = reqVo.getLiveOddBusiness();
        if (oddBusiness == null) {
            return HttpResponse.fail("参数liveOddBusiness传值有误！");
        }

        List<Long> categorySetIds = reqVo.getCategorySetIds();
        if (CollectionUtils.isEmpty(categorySetIds)) {
            return HttpResponse.fail("参数categorySetIds传值有误！");
        }
        try {
            List<FootballTwoPlaySetInfo> infos = secondaryPlayService.footballTwoPlaySet(reqVo);
            return HttpResponse.success(infos);
        } catch (Exception e) {
			log.error("::{}::次要玩法:{}", CommonUtil.getRequestId(reqVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("次要玩法异常：" + e.getMessage());
        }
    }

    /**
     * 网球次要玩法
     *
     * @param reqVo
     * @return
     */
    @PostMapping("/categorySetScore")
    public HttpResponse<MatchMarketLiveBean> tennisScore(@RequestBody FootballTwoReqVo reqVo) {
        Long matchId = reqVo.getMatchId();
        if (matchId == null || matchId <= 0) {
            return HttpResponse.fail("参数matchId传值有误！");
        }
        try {
            MatchMarketLiveBean matchInfo = marketCategorySetService.getMatchInfo(null, matchId);
            return HttpResponse.success(matchInfo);
        } catch (Exception e) {
			log.error("::{}::网球比分获取异常{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.fail("网球比分获取异常" + e.getMessage());
        }
    }

}
