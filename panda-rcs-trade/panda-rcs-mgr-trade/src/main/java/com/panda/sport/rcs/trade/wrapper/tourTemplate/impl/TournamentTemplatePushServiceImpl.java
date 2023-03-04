package com.panda.sport.rcs.trade.wrapper.tourTemplate.impl;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateAcceptConfigSettleMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateAcceptEventSettleMapper;
import com.panda.sport.rcs.pojo.tourTemplate.*;
import com.panda.sport.rcs.trade.enums.ManagerCodeEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem.*;
import com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness.MatchTournamentTemplateVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness.TournamentTemplateAcceptConfigSettleVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness.TournamentTemplateAcceptEventSettleVo;
import com.panda.sport.rcs.utils.CommonUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.panda.merge.dto.Response;
import com.panda.sport.manager.api.IMarketCategorySellApi;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateEventMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.trade.param.TournamentTemplateUpdateParam;
import com.panda.sport.rcs.trade.param.UpdateTournamentLevelParam;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.TournamentTemplatePushService;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class TournamentTemplatePushServiceImpl implements TournamentTemplatePushService {
    @Autowired
    private ProducerSendMessageUtils sendMessage;
    @Autowired
    private RcsTournamentTemplateEventMapper templateEventMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper playMargainMapper;
    @Autowired
    private RcsTournamentTemplateAcceptConfigSettleMapper configSettleMapper;
    @Autowired
    private RcsTournamentTemplateAcceptEventSettleMapper eventSettleMapper;
    @Reference(retries = 1, lazy = true, check = false, timeout = 100000)
    IMarketCategorySellApi marketCategorySellApi;

    @Override
    public void putTournamentLevel(UpdateTournamentLevelParam param) {
        //给融合推送变更联赛级别
        DataRealtimeApiUtils.handleApi(param, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(com.panda.merge.dto.Request request) {
                return marketCategorySellApi.setTournamentLevel(param.getId(), param.getLevel(), param.getIsPopular());
            }
        });
    }

    @Override
    public void putTournamentTemplateMatchScoreSourceData(TournamentTemplateUpdateParam param) {
        try {
            TournamentTemplateMatchVo matchVo = getTournamentTemplateMatchVo(param);
            matchVo.setScoreSource(param.getScoreSource());
            matchVo.setSportId(param.getSportId());
            String linkId = CommonUtils.getLinkId("template_score");
            Request request = new Request();
            request.setData(matchVo);
            request.setGlobalId(linkId);
            log.info("::{}::发送mq推送联赛模板比分源切换:Message:{}", linkId, JSONObject.toJSON(request));
            sendMessage.sendMessage("Tournament_Template_Match", linkId, String.valueOf(matchVo.getStandardMatchId()), JSONObject.toJSON(request));
            sendMessage.sendMessage("RCS_TRANSFER_SCORE_SOURCE", linkId, String.valueOf(matchVo.getStandardMatchId()), JSONObject.toJSON(request));
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(param.getMatchId()), ex.getMessage(), ex);
        }
    }

    @Override
    public void putTournamentTemplateMatchEventData(TournamentTemplateUpdateParam param) {
        try {
            TournamentTemplateMatchVo matchVo = getTournamentTemplateMatchVo(param);
            matchVo.setScoreSource(param.getScoreSource());
            String linkId;
            if (!CollectionUtils.isEmpty(param.getTemplateEventList())) {
                //获取赛事参数更新的事件
                List<TournamentTemplateEventVo> eventVoList = BeanCopyUtils.copyPropertiesList(param.getTemplateEventList(), TournamentTemplateEventVo.class);
                matchVo.setTemplateEventList(eventVoList);
                linkId = CommonUtils.getLinkId("match_template_update");
            } else {
                //初始化赛事参数配置的所有事件
                Map<String, Object> map = Maps.newHashMap();
                map.put("template_id", param.getId());
                List<RcsTournamentTemplateEvent> eventList = templateEventMapper.selectByMap(map);
                if (!CollectionUtils.isEmpty(eventList)) {
                    List<TournamentTemplateEventVo> eventVoList = BeanCopyUtils.copyPropertiesList(eventList, TournamentTemplateEventVo.class);
                    matchVo.setTemplateEventList(eventVoList);
                }
                linkId = CommonUtils.getLinkId("match_template_init");
            }
            Request request = new Request();
            request.setData(matchVo);
            request.setGlobalId(linkId);
            log.info("::{}::发送mq推送联赛模板赛事和事件数据:Message:{}", linkId, JSONObject.toJSON(request));
            sendMessage.sendMessage("Tournament_Template_Match", linkId, String.valueOf(matchVo.getStandardMatchId()), JSONObject.toJSON(request));
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(param.getMatchId()), ex.getMessage(), ex);
        }
    }

    @Override
    public void putTournamentTemplatePlayData(TournamentTemplateUpdateParam param) {
        try {
            TournamentTemplatePlayVo playVo = getTournamentTemplatePlayVo(param);
            String linkId;
            if (!CollectionUtils.isEmpty(param.getPlayMargainList())) {
                //获取赛事参数更新的玩法
                List<TournamentTemplateCategoryVo> categoryList = BeanCopyUtils.copyPropertiesList(param.getPlayMargainList(), TournamentTemplateCategoryVo.class);
                playVo.setCategoryList(categoryList);
                linkId = CommonUtils.getLinkId("play_template_update");
            } else {
                //初始化赛事参数配置的所有玩法
                Map<String, Object> map = Maps.newHashMap();
                map.put("template_id", param.getId());
                map.put("match_type", param.getMatchType());
                List<RcsTournamentTemplatePlayMargain> playMarginList = playMargainMapper.selectByMap(map);
                if (!CollectionUtils.isEmpty(playMarginList)) {
                    List<TournamentTemplateCategoryVo> categoryList = BeanCopyUtils.copyPropertiesList(playMarginList, TournamentTemplateCategoryVo.class);
                    playVo.setCategoryList(categoryList);
                    //将该赛事模板下的玩法是否开售字段is_sell,全部重置为0,赛事模板只会显示已开售的玩法，和联赛模板共用一个字段(注意：在确认开售那里，将玩法的is_sell改为1)
                    if (playVo.getRiskManagerCode().equals(ManagerCodeEnum.PA.getId())) {
                        for (RcsTournamentTemplatePlayMargain margain : playMarginList) {
                            margain.setIsSell(0);
                            playMargainMapper.updateById(margain);
                        }
                    }
                }
                linkId = CommonUtils.getLinkId("play_template_init");
            }
            Request request = new Request();
            request.setData(playVo);
            request.setGlobalId(linkId);
            log.info("::{}::发送mq推送联赛模板玩法数据:Message:{}", linkId, JSONObject.toJSON(request));
            sendMessage.sendMessage("Tournament_Template_Play", linkId, String.valueOf(playVo.getStandardMatchId()), JSONObject.toJSON(request));
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(param.getMatchId()), ex.getMessage(), ex);
        }
    }

    @Override
    public void putMatchSyncTourTempPlayData(TournamentTemplateUpdateParam param) {
        try {
            TournamentTemplatePlayVo playVo = getTournamentTemplatePlayVo(param);
            if (!CollectionUtils.isEmpty(param.getPlayMargainList())) {
                //获取赛事参数更新的玩法
                List<TournamentTemplateCategoryVo> categoryList = BeanCopyUtils.copyPropertiesList(param.getPlayMargainList(), TournamentTemplateCategoryVo.class);
                playVo.setCategoryList(categoryList);
                String linkId = CommonUtils.getLinkId("template_add_play");
                Request request = new Request();
                request.setData(playVo);
                request.setGlobalId(linkId);
                log.info("::{}::发送mq推送联赛模板玩法数据:Message:{}", linkId, JSONObject.toJSON(request));
                sendMessage.sendMessage("Refresh_Market_Category", linkId, String.valueOf(playVo.getStandardMatchId()), JSONObject.toJSON(request));
            }
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(param.getMatchId()), ex.getMessage(), ex);
        }
    }

    @Override
    public void putTournamentTemplateSettleData(TournamentTemplateUpdateParam param) {
        MatchTournamentTemplateVo rtnVo = getMatchTournamentTemplateVo(param);
        if (rtnVo == null) return;
        String linkId = CommonUtils.getLinkId("template_settle_init");
        Request request = new Request();
        request.setData(rtnVo);
        request.setGlobalId(linkId);
        log.info("::{}::发送mq推送联赛模板结算数据:Message:{}", linkId, JSONObject.toJSON(request));
        sendMessage.sendMessage("Tournament_Template_Settle", linkId, String.valueOf(param.getTypeVal()), JSONObject.toJSON(request));
    }

    private MatchTournamentTemplateVo getMatchTournamentTemplateVo(TournamentTemplateUpdateParam param) {
        MatchTournamentTemplateVo rtnVo = new MatchTournamentTemplateVo();
        List<TournamentTemplateAcceptConfigSettleVo> acceptConfigList = Lists.newArrayList();
        rtnVo.setSportId(param.getSportId());
        rtnVo.setMatchId(param.getTypeVal());
        rtnVo.setTemplateId(param.getId());
        rtnVo.setAcceptConfigList(acceptConfigList);
        //初始化赛事参数配置的所有玩法
        Map<String, Object> map = Maps.newHashMap();
        map.put("template_id", param.getId());
        List<RcsTournamentTemplateAcceptConfigSettle> configSettleList = configSettleMapper.selectByMap(map);
        if (CollectionUtils.isEmpty(configSettleList)) {
            log.info("::{}::未找到赛事模板接拒结算数据:{}",CommonUtil.getRequestId(param.getMatchId()), JSONObject.toJSON(rtnVo));
            return null;
        }
        for (RcsTournamentTemplateAcceptConfigSettle configSettle : configSettleList) {
            TournamentTemplateAcceptConfigSettleVo vo = new TournamentTemplateAcceptConfigSettleVo();
            vo.setId(configSettle.getId());
            vo.setCategorySetId(configSettle.getCategorySetId());
            vo.setDataSourceCode(configSettle.getDataSource());
            vo.setNormalDelayTime(configSettle.getNormal());
            vo.setDelayTime(configSettle.getMinWait());
            vo.setMaxDelayTime(configSettle.getMaxWait());
            Map<String, Object> eventSettleMap = Maps.newHashMap();
            eventSettleMap.put("accept_config_settle_id", configSettle.getId());
            List<RcsTournamentTemplateAcceptEventSettle> eventSettleList = eventSettleMapper.selectByMap(eventSettleMap);
            if (!CollectionUtils.isEmpty(eventSettleList)) {
                List<TournamentTemplateAcceptEventSettleVo> eventSettleVo = BeanCopyUtils.copyPropertiesList(eventSettleList, TournamentTemplateAcceptEventSettleVo.class);
                vo.setAcceptEventList(eventSettleVo);
            }
            acceptConfigList.add(vo);
        }
        return rtnVo;
    }

    @Override
    public MatchTournamentTemplateVo getTourTempAcceptSettleConfig(RcsTournamentTemplate template) {
        TournamentTemplateUpdateParam param = BeanCopyUtils.copyProperties(template, TournamentTemplateUpdateParam.class);
        MatchTournamentTemplateVo rtnVo = getMatchTournamentTemplateVo(param);
        return rtnVo;
    }

    @Override
    public void putTournamentTemplateSettleDataByIncrement(RcsTournamentTemplateAcceptConfigSettle param) {
        //初始化赛事参数配置的所有玩法
        Map<String, Object> map = Maps.newHashMap();
        map.put("template_id", param.getTemplateId());
        List<RcsTournamentTemplateAcceptConfigSettle> configSettleList = configSettleMapper.selectByMap(map);
        if (CollectionUtils.isEmpty(configSettleList)) {
            log.info("::{}::未找到赛事模板接拒结算数据:{}", CommonUtil.getRequestId(), JSONObject.toJSON(param));
            return;
        }
        MatchTournamentTemplateVo rtnVo = new MatchTournamentTemplateVo();
        List<TournamentTemplateAcceptConfigSettleVo> acceptConfigList = Lists.newArrayList();
        rtnVo.setSportId(NumberUtils.INTEGER_ONE);
        rtnVo.setMatchId(param.getMatchId());
        rtnVo.setTemplateId(param.getTemplateId());
        rtnVo.setAcceptConfigList(acceptConfigList);
        TournamentTemplateAcceptConfigSettleVo vo = new TournamentTemplateAcceptConfigSettleVo();
        vo.setId(param.getId());
        vo.setCategorySetId(param.getCategorySetId());
        vo.setDataSourceCode(param.getDataSource());
        vo.setNormalDelayTime(param.getNormal());
        vo.setDelayTime(param.getMinWait());
        vo.setMaxDelayTime(param.getMaxWait());
        List<TournamentTemplateAcceptEventSettleVo> eventSettleVo = BeanCopyUtils.copyPropertiesList(param.getEvents(), TournamentTemplateAcceptEventSettleVo.class);
        vo.setAcceptEventList(eventSettleVo);
        acceptConfigList.add(vo);
        String linkId = CommonUtils.getLinkId("template_settle_update");
        Request request = new Request();
        request.setData(rtnVo);
        request.setGlobalId(linkId);
        log.info("::{}::发送mq推送联赛模板结算数据: Message:{}", linkId, JSONObject.toJSON(request));
        sendMessage.sendMessage("Tournament_Template_Settle", linkId, String.valueOf(param.getMatchId()), JSONObject.toJSON(request));
    }

    private TournamentTemplateMatchVo getTournamentTemplateMatchVo(RcsTournamentTemplate param) {
        TournamentTemplateMatchVo matchVo = new TournamentTemplateMatchVo();
        DataSourceCodeVo weight = JSONObject.parseObject(param.getDataSourceCode(), DataSourceCodeVo.class);
        matchVo.setStandardMatchId(param.getTypeVal());
        matchVo.setMatchType(param.getMatchType());
        matchVo.setBcWeight(weight.getBc());
        matchVo.setBgWeight(weight.getBg());
        matchVo.setSrWeight(weight.getSr());
        matchVo.setTxWeight(weight.getTx());
        matchVo.setRbWeight(weight.getRb());
        return matchVo;
    }

    private TournamentTemplatePlayVo getTournamentTemplatePlayVo(TournamentTemplateUpdateParam param) {
        TournamentTemplatePlayVo playVo = new TournamentTemplatePlayVo();
        DataSourceCodeVo weight = JSONObject.parseObject(param.getDataSourceCode(), DataSourceCodeVo.class);
        playVo.setStandardMatchId(param.getTypeVal());
        playVo.setMatchType(param.getMatchType());
        playVo.setBcWeight(weight.getBc());
        playVo.setBgWeight(weight.getBg());
        playVo.setSrWeight(weight.getSr());
        playVo.setTxWeight(weight.getTx());
        playVo.setRbWeight(weight.getRb());
        playVo.setPdWeight(weight.getPd());
        playVo.setAoWeight(weight.getAo());
        playVo.setPiWeight(weight.getPi());
        playVo.setLsWeight(weight.getLs());
        if (!StringUtils.isEmpty(param.getTemplateName())) {
            if (param.getTemplateName().contains(ManagerCodeEnum.MTS.getId())) {
                playVo.setRiskManagerCode(ManagerCodeEnum.MTS.getId());
            } else {
                playVo.setRiskManagerCode(ManagerCodeEnum.PA.getId());
            }
        }
        //传给融合的操盘模式  PA or MTS
        if(!StringUtils.isEmpty(param.getRiskManagerCode())){
            playVo.setRiskManagerCode(param.getRiskManagerCode());
        }
        return playVo;
    }
}
