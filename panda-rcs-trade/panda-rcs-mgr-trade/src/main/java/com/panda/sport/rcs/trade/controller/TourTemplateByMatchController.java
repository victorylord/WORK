package com.panda.sport.rcs.trade.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.merge.dto.Request;
import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.mq.SpecialSpreadCalculatePlayVO;
import com.panda.sport.rcs.pojo.mq.SpecialSpreadCalculateVO;
import com.panda.sport.rcs.trade.enums.TempTypeEnum;
import com.panda.sport.rcs.trade.param.*;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.tourTemplate.PeningOrderCacheClearVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem.TournamentLevelTemplateVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness.MatchTournamentTemplateVo;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;
import com.panda.sport.rcs.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.common.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainRefMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateEvent;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsMatchTemplateModifyService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.TournamentTemplatePushService;
import com.panda.sport.rcs.vo.HttpResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 赛事模板
 *
 * @author carver
 * @date 2020-10-03
 */
@RestController
@RequestMapping(value = "/match/template")
@Slf4j
@Component
public class TourTemplateByMatchController {
    @Autowired
    private IRcsMatchTemplateModifyService rcsMatchTemplateModifyService;
    @Autowired
    private TournamentTemplatePushService tournamentTemplatePushService;
    @Autowired
    private RcsTournamentTemplateMapper templateMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper playMargainMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainRefMapper playMargainRefMapper;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;

    /**
     * 更新比分源数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/modifyScoreSource")
    @LogAnnotion(name = "更新赛事模板比分源", keys = {"id", "scoreSource"}, title = {"模板id", "比分源"})
    @OperateLog(operateType = OperateLogEnum.OPERATE_SETTING)
    public HttpResponse modifyTemplate(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            log.info("::{}::更新赛事模板数据:{}",CommonUtil.getRequestId(param.getMatchId(),param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            rcsMatchTemplateModifyService.modifyTemplate(param);
            //发送mq推送联赛模板赛事数据
            RcsTournamentTemplate template = templateMapper.selectById(param.getId());
            param.setTypeVal(template.getTypeVal());
            param.setMatchType(template.getMatchType());
            param.setDataSourceCode(template.getDataSourceCode());
            param.setSportId(template.getSportId());
            tournamentTemplatePushService.putTournamentTemplateMatchScoreSourceData(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板比分源:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板比分源:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 更新篮球滚球联赛设置，接单等待时间
     *
     * @author carver
     * @date 2021-01-12
     */
    @PostMapping("/modifyWaitTime")
    @LogAnnotion(name = "更新赛事模板接单等待时间", keys = {"id", "normalWaitTime", "pauseWaitTime"}, title = {"模板id", "常规接单等待时间", "暂停接单等待时间"})
    public HttpResponse modifyWaitTime(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            log.info("::{}::更新赛事模板接单等待时间:{}",CommonUtil.getRequestId(param.getMatchId(),param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            RcsTournamentTemplate template = templateMapper.selectById(param.getId());
            param.setScoreSource(template.getScoreSource());
            rcsMatchTemplateModifyService.modifyTemplate(param);
            //清除接拒单，接单等待时间缓存设置
            if (template.getType() == 3 && template.getSportId() == 2) {
                String redisKey = String.format("rcs:task:match:event:%s", template.getTypeVal());
                redisClient.delete(redisKey);
            }
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板接单等待时间:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板接单等待时间:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    @PostMapping("/modifyMatchPendingOrderParam")
    public HttpResponse modifyMatchPendingOrderParam(@RequestBody TournamentTemplateUpdatePendingOrderParam param){
        try {
            log.info("::{}::更新赛事模板预约投注数据:{}",CommonUtil.getRequestId(param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            TournamentTemplateUpdateParam updateParam=JSONObject.parseObject(JSONObject.toJSONString(param),TournamentTemplateUpdateParam.class);
            rcsMatchTemplateModifyService.modifyTemplate(updateParam);
            RcsTournamentTemplate template = templateMapper.selectById(param.getId());


            PeningOrderCacheClearVo peningOrderCacheClearVo=new PeningOrderCacheClearVo();
            peningOrderCacheClearVo.setTemplate("template");
            peningOrderCacheClearVo.setTypeVal(String.valueOf(template.getTypeVal()));
            peningOrderCacheClearVo.setMatchType(template.getMatchType());
            //通知缓存清理
            producerSendMessageUtils.sendMessage("PENDING_ORDER_DELETECACHE",peningOrderCacheClearVo);
            log.info("::{}::PENDING_ORDER_DELETECACHE缓存通知:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(peningOrderCacheClearVo));
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板预约投注数据:{}", CommonUtil.getRequestId(param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板预约投注数据:{}", CommonUtil.getRequestId(param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }
    /**
     * 更新赛事限额数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/modifyMatchPayVal")
    @LogAnnotion(name = "更新赛事限额数据", keys = {"id", "businesMatchPayVal", "userMatchPayVal"}, title = {"模板id", "商户单场赔付限额", "用户单场赔付限额"})
    @OperateLog(operateType = OperateLogEnum.OPERATE_SETTING)
    public HttpResponse modifyMatchPayVal(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            log.info("::{}::更新赛事模板数据:{}",CommonUtil.getRequestId(param.getMatchId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            RcsTournamentTemplate template = templateMapper.selectById(param.getId());
            param.setScoreSource(template.getScoreSource());
            param.setMatchType(template.getMatchType());
            param.setSportId(template.getSportId());
            rcsMatchTemplateModifyService.modifyTemplate(param);
            //通知更新订单缓存限额
            //这里判断下具体是修改哪个维度的 去除冗余的mq发送
            //用户单场 则去掉不用更新商户单场
            if(param.getUserMatchPayVal() !=null){
                template.setBusinesMatchPayVal(null);
                template.setUserMatchPayVal(param.getUserMatchPayVal());
            }
            //商户单场 则去掉不用更新用户单场
            if(param.getBusinesMatchPayVal() !=null){
                template.setUserMatchPayVal(null);
                template.setBusinesMatchPayVal(param.getBusinesMatchPayVal());
            }
            noticeOrderUpdate(template);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事限额数据:{}", CommonUtil.getRequestId(param.getMatchId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事限额数据:{}", CommonUtil.getRequestId(param.getMatchId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }



    private void noticeOrderUpdate(RcsTournamentTemplate template) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("sportId", template.getSportId());
        map.put("matchId", template.getTypeVal());
        map.put("matchType", template.getMatchType());
        //用户单场
        if(template.getUserMatchPayVal() !=null){
            map.put("dataType", "3");
            map.put("val",template.getUserMatchPayVal());
        }
        //商户单场
        if(template.getBusinesMatchPayVal() !=null){
            map.put("dataType", "1");
            map.put("val",template.getBusinesMatchPayVal());
        }
        log.info("::{}::RCS_LIMIT_CACHE_CLEAR_TOPIC缓存通知:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(map));
        producerSendMessageUtils.sendMessage("RCS_LIMIT_CACHE_CLEAR_TOPIC", map);


        PeningOrderCacheClearVo peningOrderCacheClearVo=new PeningOrderCacheClearVo();
        peningOrderCacheClearVo.setTemplate("template");
        peningOrderCacheClearVo.setTypeVal(String.valueOf(template.getTypeVal()));
        peningOrderCacheClearVo.setMatchType(template.getMatchType());
        //通知缓存清理
        producerSendMessageUtils.sendMessage("PENDING_ORDER_DELETECACHE",peningOrderCacheClearVo);
        log.info("::{}::PENDING_ORDER_DELETECACHE缓存通知:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(peningOrderCacheClearVo));
    }

    /**
     * 更新赛事模板玩法数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/modifyPlayMargain")
    @LogAnnotion(name = "更新赛事模板玩法数据", keys = {"id", "templateId", "playId", "marketCount", "autoCloseMarket", "matchProgressTime", "injuryTime", "marketWarn", "isSeries", "viceMarketRatio", "marketNearDiff", "marketNearOddsDiff", "oddsAdjustRange", "marketAdjustRange"},
            title = {"玩法模板id", "模板id", "玩法id", "最大盘口数", "自动关盘时间", "比赛进程时间", "补时时间", "盘口出涨预警", "支持串关", "副盘限额比列", "相邻盘口差值", "相邻盘口赔率差值", "赔率（水差）变动幅度", "盘口调整幅度"})
    @OperateLog(operateType = OperateLogEnum.OPERATE_SETTING)
    public HttpResponse modifyPlayMargain(@RequestBody TournamentTemplatePlayMargainParam param) {
        try {
            log.info("::{}::更新赛事模板,玩法数据:{}",CommonUtil.getRequestId(param.getMatchId(),param.getPlayId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getTemplateId(), "模板id不能为空");
            Assert.notNull(param.getPlayId(), "玩法id不能为空");

            RcsTournamentTemplatePlayMargain obj = playMargainMapper.selectById(param.getId());
            List<TournamentTemplatePlayMargainParam> playMargainParam = Lists.newArrayList();
            playMargainParam.add(param);
            rcsMatchTemplateModifyService.modifyPlayMargain(playMargainParam);
            //盘口数更改，根据有效分时valid_margin_id,更新ref表状态为3
            if (param.getMarketCount() != null) {
                if (!obj.getMarketCount().equals(param.getMarketCount()) || !obj.getViceMarketRatio().equals(param.getViceMarketRatio())) {
                    if (!ObjectUtils.isEmpty(obj.getValidMarginId())) {
                        RcsTournamentTemplatePlayMargainRef newObj = new RcsTournamentTemplatePlayMargainRef();
                        newObj.setStatus(3);
                        newObj.setId(obj.getValidMarginId());
                        playMargainRefMapper.updateById(newObj);
                    }
                }
            }
            //发送mq推送联赛模板玩法数据
            RcsTournamentTemplate template = templateMapper.selectById(param.getTemplateId());
            TournamentTemplateUpdateParam tournamentTemplateUpdateParam = new TournamentTemplateUpdateParam();
            tournamentTemplateUpdateParam.setPlayMargainList(playMargainParam);
            tournamentTemplateUpdateParam.setTypeVal(template.getTypeVal());
            tournamentTemplateUpdateParam.setMatchType(template.getMatchType());
            tournamentTemplateUpdateParam.setDataSourceCode(template.getDataSourceCode());
            tournamentTemplatePushService.putTournamentTemplatePlayData(tournamentTemplateUpdateParam);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板玩法数据:{}", CommonUtil.getRequestId(param.getTemplateId(),param.getPlayId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板玩法数据:{}", CommonUtil.getRequestId(param.getTemplateId(),param.getPlayId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 更新赛事模板分时margin数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/modifyMargainRef")
    @LogAnnotion(name = "更新赛事模板分时margin数据", keys = {"margainId", "timeVal"}, title = {"marginId", "timeVal"})
    @OperateLog
    public HttpResponse modifyMargainRef(@RequestBody TournamentTemplatePlayMargainRefParam param) {
        try {
            log.info("::{}::更新赛事模板,分时margin数据:{}",CommonUtil.getRequestId(param.getMargainId()), JSONObject.toJSONString(param));
            if(SportIdEnum.isFootball(param.getSportId())&& Arrays.asList(6,70,72,107,347).contains(param.getPlayId()) && Double.valueOf(param.getMargain()) < 200){
                return HttpResponse.error(HttpResponse.FAIL, "margin不能低于200");
            }
            Assert.notNull(param.getMargainId(), "marginId不能为空");
            Assert.notNull(param.getTimeVal(), "timeVal不能为空");
            rcsMatchTemplateModifyService.modifyMargainRef(param, NumberUtils.INTEGER_TWO);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板分时margin数据:{}", CommonUtil.getRequestId(param.getMargainId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板分时margin数据:{}", CommonUtil.getRequestId(param.getMargainId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 更新赛事模板，滚球结算审核事件数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/modifyEvent")
    @LogAnnotion(name = "更新赛事模板审核事件数据", keys = {"id", "templateId", "eventHandleTime", "settleHandleTime"}, title = {"事件id", "模板id", "审核时间", "结算时间"})
    @OperateLog(operateType = OperateLogEnum.OPERATE_SETTING)
    public HttpResponse modifyTemplateEvent(@RequestBody RcsTournamentTemplateEvent param) {
        try {
            log.info("::{}::更新赛事模板，滚球结算审核事件数据:{}",CommonUtil.getRequestId(param.getId(),param.getTemplateId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getTemplateId(), "模板id不能为空");
            List<RcsTournamentTemplateEvent> templateEvent = Lists.newArrayList();
            templateEvent.add(param);
            rcsMatchTemplateModifyService.modifyTemplateEvent(templateEvent);
            //发送mq推送联赛模板事件数据
            RcsTournamentTemplate template = templateMapper.selectById(param.getTemplateId());
            TournamentTemplateUpdateParam tournamentTemplateUpdateParam = new TournamentTemplateUpdateParam();
            tournamentTemplateUpdateParam.setTemplateEventList(templateEvent);
            tournamentTemplateUpdateParam.setTypeVal(template.getTypeVal());
            tournamentTemplateUpdateParam.setMatchType(template.getMatchType());
            tournamentTemplateUpdateParam.setDataSourceCode(template.getDataSourceCode());
            tournamentTemplateUpdateParam.setScoreSource(template.getScoreSource());
            tournamentTemplatePushService.putTournamentTemplateMatchEventData(tournamentTemplateUpdateParam);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板审核事件数据:{}", CommonUtil.getRequestId(param.getId(),param.getTemplateId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板审核事件数据:{}", CommonUtil.getRequestId(param.getId(),param.getTemplateId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 删除联赛模板和赛事模板分时margin数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/removeMargainRef")
    @LogAnnotion(name = "删除分时节点", keys = {"margainId", "timeVal"}, title = {"margainId", "时间值"})
    @OperateLog
    public HttpResponse removeMargainRef(@RequestBody TournamentTemplatePlayMargainRefParam param) {
        try {
            log.info("::{}::删除联赛模板和赛事模板分时margin数据:{}",CommonUtil.getRequestId(param.getMargainId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getMargainId(), "marginId不能为空");
            Assert.notNull(param.getTimeVal(), "timeVal不能为空");
            if (param.getTimeVal() == 0 || param.getTimeVal() == 2592000) {
                return HttpResponse.error(201, "首个节点不能删除");
            }
            rcsMatchTemplateModifyService.removeMarginRef(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::删除分时节点:{}", CommonUtil.getRequestId(param.getMargainId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::删除分时节点:{}", CommonUtil.getRequestId(param.getMargainId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * @param param:
     * @Description: 赛事模板，同步联赛模板数据
     * @Author carver
     * @Date 2020/10/27 17:51
     * @return: com.panda.sport.rcs.vo.HttpResponse
     **/
    @PostMapping("/modifyMatchTempByLevelTemp")
    @LogAnnotion(name = "赛事模板-同步联赛模板数据", keys = {"id", "copyTemplateId", "templateName"}, title = {"模板id", "刷新模板id", "刷新模板名称"})
    @OperateLog(operateType = OperateLogEnum.OPERATE_SETTING, operateParamter = OperateLogEnum.SYNC_MATCH_TEMPLATE)
    public HttpResponse modifyMatchTempByLevelTemp(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            CommonUtils.mdcPut();
            log.info("::{}::更新赛事模板同步联赛设置数据:{}",CommonUtil.getRequestId(param.getTypeVal(),param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getCopyTemplateId(), "刷新模板id不能为空");
            Assert.notNull(param.getTemplateName(), "刷新模板名称不能为空");
            rcsMatchTemplateModifyService.modifyMatchTempByLevelTemp(param);
            RcsTournamentTemplate template = templateMapper.selectById(param.getId());
            if (template != null) {
                //通知更新订单缓存限额
                noticeOrderUpdate(template);
            }
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::赛事模板-同步联赛模板数据:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::赛事模板-同步联赛模板数据:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    /**
     * @param param:
     * @Description: 赛事模板刷新功能，根据赛事所在联赛获取联赛模板
     * @Author carver
     * @Date 2020/10/27 17:51
     * @return: com.panda.sport.rcs.vo.HttpResponse
     **/
    @PostMapping("/findLevelTempByMatchId")
    public HttpResponse findLevelTempByMatchId(@RequestBody TournamentTemplateUpdateParam param, @RequestHeader("lang") String lang) {
        try {
            log.info("::{}::赛事模板刷新功能，根据赛事所在联赛获取联赛模板:{}",CommonUtil.getRequestId(param.getTypeVal(),param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getSportId(), "赛种id不能为空");
            Assert.notNull(param.getTypeVal(), "赛事id不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            Assert.notNull(lang, "国际化lang不能为空");
            List<TournamentLevelTemplateVo> list = rcsMatchTemplateModifyService.findLevelTempByMatchId(param, lang);
            return HttpResponse.success(list);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::赛事模板刷新功能，根据赛事所在联赛获取联赛模板:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::赛事模板刷新功能，根据赛事所在联赛获取联赛模板:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * @Description: 线上联赛设置滚球接拒单参数配置
     * 根据1级联赛接拒单配置，刷新其他联赛等级下的接拒单配置
     * @Author carver
     * @Date 2020/10/27 17:51
     * @return: com.panda.sport.rcs.vo.HttpResponse
     **/
    @GetMapping("/processTemplateByOneLevel")
    public HttpResponse processTemplateByOneLevel() {
        try {
            rcsMatchTemplateModifyService.processTemplateByOneLevel();
            return HttpResponse.success();
        } catch (Exception e) {
            log.error("::{}::线上联赛设置滚球接拒单参数配置:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * @Description: 由于篮球联赛设置参数调整，线上已开篮球赛事需同步联赛模板数据
     * @Author carver
     * @Date 2020/12/10 17:51
     * @return: com.panda.sport.rcs.vo.HttpResponse
     **/
    @GetMapping("/processBasketballMatchTemplate")
    public HttpResponse processBasketballMatchTemplate(Long matchId) {
        try {
            CommonUtils.mdcPut();
            List<RcsTournamentTemplate> list = templateMapper.queryBasketballMatchTemplate(matchId);
            log.info("::{}::线上已开篮球赛事需同步联赛数据:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(list));
            if (!CollectionUtils.isEmpty(list)) {
                for (RcsTournamentTemplate temp : list) {
                    try {
                        TournamentTemplateUpdateParam param = BeanCopyUtils.copyProperties(temp, TournamentTemplateUpdateParam.class);
                        rcsMatchTemplateModifyService.modifyMatchTempByLevelTemp(param);
                        //通知更新订单缓存限额
                        noticeOrderUpdate(temp);
                    } catch (Exception e) {
                        log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
                    }
                }
            }
            return HttpResponse.success();
        } catch (Exception e) {
            log.error("::{}::线上已开篮球赛事需同步联赛模板数据:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    /**
     * @Description: 更新赛事玩法赔率源数据
     * @Author carver
     * @Date 2021/02/13 17:51
     * @return: com.panda.sport.rcs.vo.HttpResponse
     **/
    @PostMapping("/modifyPlayOddsConfig")
    @LogAnnotion(name = "赛事模板-更新玩法赔率源", keys = {"matchId", "matchType", "playOddsConfigs"}, title = {"赛事id", "类型", "玩法赔率源"})
    @OperateLog(operateType = OperateLogEnum.DATA_SOURCE_CHANGE)
    public HttpResponse modifyPlayOddsConfig(@RequestBody RcsTournamentTemplatePlayOddsConfigParam param) {
        HttpResponse response;
        String linkId = CommonUtils.mdcPut();
        try {
            // 列表主页面做权限控制
            if (!ObjectUtils.isEmpty(param.getIsMainPage()) && param.getIsMainPage() == 1){
                RcsMatchMarketConfig config = new RcsMatchMarketConfig();
                config.setMatchId(param.getMatchId());
                config.setPlayId(param.getPlayOddsConfigs().get(0).getPlayIds().get(0));
                rcsTradeConfigService.isMtsTrade(config, SportIdEnum.FOOTBALL.getId().intValue());
                boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
                if (!b) {
                    return HttpResponse.failToMsg("您没有该操作权限！");
                }
            }
            log.info("::{}::更新赛事玩法赔率源数据:{}",CommonUtil.getRequestId(param.getMatchId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getMatchId(), "赛事id不能为空");
            Assert.notNull(param.getMatchType(), "类型不能为空");
            if (CollectionUtils.isEmpty(param.getPlayOddsConfigs())) {
                throw new IllegalArgumentException("玩法赔率源设置不能为空");
            }
            rcsMatchTemplateModifyService.updatePlayOddsConfig(param);
            response = HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::赛事模板-更新玩法赔率源:{}", CommonUtil.getRequestId(param.getMatchId(),param.getTemplateId()), ex.getMessage(), ex);
            response = HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::赛事模板-更新玩法赔率源:{}", CommonUtil.getRequestId(param.getMatchId(),param.getTemplateId()), e.getMessage(), e);
            response = HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
        response.setLinkId(linkId);
        return response;
    }

    /**
     * @Description: 更新赛事百家赔数据
     * @Author kir
     * @Date 2022/02/06 17:51
     * @return: com.panda.sport.rcs.vo.HttpResponse
     **/
    @PostMapping("/modifyBaiJiaConfig")
    @LogAnnotion(name = "赛事模板-更新赛事百家赔数据", keys = {"matchId", "matchType"}, title = {"赛事id", "类型"})
    @OperateLog
    public HttpResponse modifyBaiJiaConfig(@RequestBody RcsTournamentTemplateBaijiaConfigParam param) {
        try {
            log.info("::{}::更新赛事百家赔数据:{}",CommonUtil.getRequestId(param.getMatchId(),param.getTemplateId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getMatchId(), "赛事id不能为空");
            Assert.notNull(param.getMatchType(), "赛事状态不能为空");
            Assert.notNull(param.getTemplateId(), "模板id不能为空");
            rcsMatchTemplateModifyService.modifyBaiJiaConfig(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::赛事模板-更新赛事百家赔数据:{}", CommonUtil.getRequestId(param.getMatchId(),param.getTemplateId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::赛事模板-更新赛事百家赔数据:{}", CommonUtil.getRequestId(param.getMatchId(),param.getTemplateId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 篮球操盘更新玩法跳水和跳盘最大值
     *
     * @author carver
     * @date 2021-4-15
     */
    @PostMapping("/modifyOddsMarketMaxValue")
    @LogAnnotion(name = "更新玩法跳水和跳盘最大值", keys = {"id", "templateId", "playId", "oddsMaxValue", "marketMaxValue"},
            title = {"玩法模板id", "模板id", "玩法id", "跳水最大值", "跳盘最大值"})
    public HttpResponse modifyOddsMarketMaxValue(@RequestBody TournamentTemplatePlayMargainParam param) {
        try {
            log.info("::{}::更新玩法跳水和跳盘最大值:{}",CommonUtil.getRequestId(param.getTemplateId(),param.getPlayId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getTemplateId(), "模板id不能为空");
            Assert.notNull(param.getPlayId(), "玩法id不能为空");
            rcsMatchTemplateModifyService.updateOddsMarketMaxValue(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新玩法跳水和跳盘最大值:{}", CommonUtil.getRequestId(param.getTemplateId(),param.getPlayId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新玩法跳水和跳盘最大值:{}", CommonUtil.getRequestId(param.getTemplateId(),param.getPlayId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 更新赛事模板特殊抽水
     *
     * @author carver
     * @date 2021-08-11
     */
    @PostMapping("/modifySpecialInterval")
    @LogAnnotion(name = "更新赛事模板玩法数据", keys = {"id", "templateId", "playId", "isSpecialPumping", "specialOddsInterval"}, title = {"id", "模板id", "玩法id", "是否特殊抽水", "特殊抽水赔率区间"})
    @OperateLog(operateType = OperateLogEnum.OPERATE_SETTING)
    public HttpResponse modifySpecialInterval(@RequestBody TournamentTemplatePlayMargainParam param) {
        try {
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getTemplateId(), "模板id不能为空");

            RcsTournamentTemplatePlayMargain obj = playMargainMapper.selectById(param.getId());
            TournamentTemplatePlayMargainParam margin = BeanCopyUtils.copyProperties(obj, TournamentTemplatePlayMargainParam.class);
            log.info("::{}::更新赛事模板特殊抽水数据:{}",CommonUtil.getRequestId(param.getMatchId(),param.getPlayId(),param.getTemplateId()), JSONObject.toJSONString(margin));
            //发送mq推送联赛模板玩法数据,通知融合
            RcsTournamentTemplate template = templateMapper.selectById(param.getTemplateId());
            TournamentTemplateUpdateParam tournamentTemplateUpdateParam = new TournamentTemplateUpdateParam();
            tournamentTemplateUpdateParam.setPlayMargainList(Arrays.asList(margin));
            tournamentTemplateUpdateParam.setTypeVal(template.getTypeVal());
            tournamentTemplateUpdateParam.setMatchType(template.getMatchType());
            tournamentTemplateUpdateParam.setDataSourceCode(template.getDataSourceCode());
            tournamentTemplatePushService.putTournamentTemplatePlayData(tournamentTemplateUpdateParam);
            //发送mq，风控处理逻辑
            SpecialSpreadCalculatePlayVO playVO = new SpecialSpreadCalculatePlayVO();
            playVO.setPlayId(margin.getPlayId().longValue());
            playVO.setIsSpecialPumping(margin.getIsSpecialPumping());
            playVO.setSpecialOddsInterval(margin.getSpecialOddsInterval());
            SpecialSpreadCalculateVO spreadCalculateVO = new SpecialSpreadCalculateVO();
            spreadCalculateVO.setMatchId(template.getTypeVal());
            spreadCalculateVO.setMatchType(template.getMatchType());
            spreadCalculateVO.setPlays(Arrays.asList(playVO));
            String linkId = CommonUtils.getLinkId();
            producerSendMessageUtils.sendMessage("TRADE_SPECIAL_SPREAD_CALCULATE", linkId + "_http", template.getTypeVal().toString(), spreadCalculateVO);
            //发送mq，ws推送至前端
            Request req = new Request();
            req.setLinkId(linkId);
            req.setData(spreadCalculateVO);
            producerSendMessageUtils.sendMessage("TRADE_SPECIAL_SPREAD_CALCULATE_WS", linkId, template.getTypeVal().toString(), req);
            //根据有效分时valid_margin_id,更新ref表状态为3，调用融合接口，从新下发赔率数据
            if (!ObjectUtils.isEmpty(obj.getValidMarginId())) {
                RcsTournamentTemplatePlayMargainRef newObj = new RcsTournamentTemplatePlayMargainRef();
                newObj.setStatus(3);
                newObj.setId(obj.getValidMarginId());
                playMargainRefMapper.updateById(newObj);
            }
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板玩法数据:{}", CommonUtil.getRequestId(param.getMatchId(),param.getPlayId(),param.getTemplateId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板玩法数据:{}", CommonUtil.getRequestId(param.getMatchId(),param.getPlayId(),param.getTemplateId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 网球操盘更新盘口出涨预警、相邻盘口赔率分差、赔率（水差）变动幅度、跳盘最大值、拒单盘口变动阈值、跳水最大值、拒单赔率变动阈值的数值
     * 修改时均显示三个按钮：确定、确定（全部玩法）、取消。点击“确定（全部玩法）”，会在参数下面显示下图所示对话框，点击”是“，对赛事模板内有该参数的全部玩法生效。
     *
     * @author carver
     * @date 2021-4-15
     */
    @PostMapping("/modifyTennisAllPlayValue")
    @LogAnnotion(name = "更新网球其他玩法全部应用", keys = {"id", "templateId", "oddsMaxValue", "marketWarn", "marketNearOddsDiff", "oddsAdjustRange"},
            title = {"玩法模板id", "模板id", "跳水最大值", "盘口出涨预警", "相邻盘口赔率差值", "赔率（水差）变动幅度"})
    public HttpResponse modifyTennisAllPlayValue(@RequestBody TournamentTemplatePlayMargainParam param) {
        try {
            log.info("::{}::更新网球其他玩法全部应用:{}",CommonUtil.getRequestId(param.getMatchId(),param.getPlayId(),param.getTemplateId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getTemplateId(), "模板id不能为空");
            rcsMatchTemplateModifyService.updateTennisAllPlayValue(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新网球其他玩法全部应用:{}", CommonUtil.getRequestId(param.getMatchId(),param.getPlayId(),param.getTemplateId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::更新网球其他玩法全部应用:{}", CommonUtil.getRequestId(param.getMatchId(),param.getPlayId(),param.getTemplateId()), ex.getMessage(), ex);
            return HttpResponse.error(HttpResponse.FAIL, ex.getMessage());
        }
    }

    /**
     * 更新赛事模板赔率变动接拒开关
     *
     * @author Kir
     * @date 2021-12-08
     */
    @PostMapping("/modifyOddsChangeStatus")
    @LogAnnotion(name = "更新赛事模板赔率变动接拒开关", keys = {"id", "oddsChangeStatus"}, title = {"模板id", "赔率变动接拒开关"})
    public HttpResponse modifyOddsChangeStatus(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            log.info("::{}::更新赛事模板赔率变动接拒开关:{}",CommonUtil.getRequestId(param.getMatchId(),param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getOddsChangeStatus(), "赔率变动接拒开关不能为空");
            rcsMatchTemplateModifyService.modifyOddsChangeStatus(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板赔率变动接拒开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板赔率变动接拒开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 更新赛事模板是否出涨自动封盘开关
     *
     * @author Kir
     * @date 2021-12-08
     */
    @PostMapping("/modifyWarnSuspended")
    @LogAnnotion(name = "更新赛事模板是否出涨自动封盘开关", keys = {"id", "ifWarnSuspended"}, title = {"模板id", "是否出涨自动封盘开关"})
    public HttpResponse modifyWarnSuspended(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            log.info("::{}::更新赛事模板是否出涨自动封盘开关:{}",CommonUtil.getRequestId(param.getMatchId(),param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getIfWarnSuspended(), "是否出涨自动封盘开关不能为空");
            rcsMatchTemplateModifyService.modifyWarnSuspended(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板是否出涨自动封盘开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板是否出涨自动封盘开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     *  预约投注开关接口
     * @return
     */
    @PostMapping("/modifyPendingOderStauts")
    @LogAnnotion(name = "更新赛事模板预约投注开关", keys = {"id", "pendingOrderStatus"}, title = {"模板id", "预约投注开关"})
    public HttpResponse modifyPendingOderStauts(@RequestBody TournamentTemplateUpdateParam param){
        try {
            log.info("::{}::更新赛事模板预约投注开关:{}",CommonUtil.getRequestId(param.getMatchId(),param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getPendingOrderStatus(), "预约投注开关不能为空");
            TournamentTemplateUpdateParam updateParam=new TournamentTemplateUpdateParam();
            updateParam.setId(param.getId());
            updateParam.setPendingOrderStatus(param.getPendingOrderStatus());
            rcsMatchTemplateModifyService.modifyPendingOrderStatus(updateParam);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板预约投注开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板预约投注开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 更新赛事模板提前结算开关
     *
     * @author carver
     * @date 2021-10-14
     */
    @PostMapping("/modifySettleSwitch")
    @LogAnnotion(name = "更新赛事模板提前结算开关", keys = {"id", "matchPreStatus"}, title = {"模板id", "提前结算开关"})
    @OperateLog(operateType = OperateLogEnum.OPERATE_SETTING, operateParamter = OperateLogEnum.MATCH_PRE_STATUS)
    public HttpResponse modifySettleSwitch(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            log.info("::{}::更新赛事模板提前结算开关:{}",CommonUtil.getRequestId(param.getMatchId(),param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getMatchPreStatus(), "提前结算开关不能为空");
            rcsMatchTemplateModifyService.modifySettleSwitch(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事模板提前结算开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新赛事模板提前结算开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 更新赛事模板提前结算开关
     *
     * @author carver
     * @date 2021-10-14
     */
    @PostMapping("/modifyMtsSwitchConfig")
    @LogAnnotion(name = "更新赛事mts接距配置", keys = {"id", "mtsConfigValue"}, title = {"模板id", "mts接距配置"})
    public HttpResponse modifyMtsSwitchConfig(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            log.info("::{}::更新赛事mts接距配置:{}",CommonUtil.getRequestId(param.getMatchId(),param.getId()), JSONObject.toJSONString(param));
            Assert.notNull(param.getId(), "id不能为空");
            Assert.notNull(param.getMtsConfigValue(), "mts接距配置不能为空");
            rcsMatchTemplateModifyService.modifyMtsSwitchConfig(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新赛事mts接距配置:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::更新赛事mts接距配置:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(HttpResponse.FAIL, ex.getMessage());
        }
    }


    @PostMapping("/modifyDistanceSwitch")
    @LogAnnotion(name = "修改接距开关", keys = {"id", "mtsConfigValue"}, title = {"模板id", "1.0/2.0接距开关"})
    public HttpResponse modifyDistanceSwitch(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            log.info("::{}::更新接距开关:{}",CommonUtil.getRequestId(param.getMatchId(),param.getId()), JSONObject.toJSONString(param));
            if (param.getSportId() == 1) {
                Assert.notNull(param.getId(), "模板id不能为空");
                Assert.notNull(param.getTypeVal(), "赛事ID不能为空");
                Assert.notNull(param.getDistanceSwitch(), "接距开关不能为空");
                rcsMatchTemplateModifyService.modifyDistanceSwitch(param);
            }
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::修改接距开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::修改接距开关:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(HttpResponse.FAIL, ex.getMessage());
        }
    }

    /**
     * 提供接口给业务拉取接拒结算事件数据
     *
     * @author carver
     * @date 2021-10-14
     */
    @GetMapping("/getTourTempAcceptSettleConfig")
    public HttpResponse initTournament(Long matchId) {
        try {
            Assert.notNull(matchId, "赛事id不能为空");
            // 根据参数条件，判断是否存在模板数据
            QueryWrapper<RcsTournamentTemplate> tempWrapper = new QueryWrapper<>();
            tempWrapper.lambda().eq(RcsTournamentTemplate::getSportId, NumberUtils.INTEGER_ONE)
                    .eq(RcsTournamentTemplate::getMatchType, NumberUtils.INTEGER_ZERO)
                    .eq(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId())
                    .eq(RcsTournamentTemplate::getTypeVal, matchId);
            RcsTournamentTemplate template = templateMapper.selectOne(tempWrapper);
            if (ObjectUtils.isEmpty(template)) {
                return HttpResponse.fail("赛事模板不存在");
            }
            MatchTournamentTemplateVo vo = tournamentTemplatePushService.getTourTempAcceptSettleConfig(template);
            return HttpResponse.success(vo);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::提供接口给业务拉取接拒结算事件数据:{}", CommonUtil.getRequestId(matchId), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::提供接口给业务拉取接拒结算事件数据:{}", CommonUtil.getRequestId(matchId), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }
}
