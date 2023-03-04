package com.panda.sport.rcs.trade.wrapper.tourTemplate.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.RcsTournamentTemplateAcceptConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportTournamentMapper;
import com.panda.sport.rcs.mapper.tourTemplate.*;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.pojo.TournamentLevelTemporaryLog;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TemplateMenuListDto;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TournamentTemplateDto;
import com.panda.sport.rcs.pojo.tourTemplate.*;
import com.panda.sport.rcs.pojo.vo.ThirdDataSourceCodeVo;
import com.panda.sport.rcs.trade.enums.DataSourceWeightEnum;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.enums.TempTypeEnum;
import com.panda.sport.rcs.trade.param.*;
import com.panda.sport.rcs.trade.service.*;
import com.panda.sport.rcs.trade.service.TradeModeService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.tourTemplate.*;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsMatchTemplateModifyService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplateService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.TournamentTemplatePushService;
import com.panda.sport.rcs.utils.StringUtils;
import com.panda.sport.rcs.wrapper.IRcsOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.tourTemplate
 * @Description :  联赛模板
 * @Date: 2020-05-10 20:39
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsTournamentTemplateServiceImpl extends ServiceImpl<RcsTournamentTemplateMapper, RcsTournamentTemplate> implements IRcsTournamentTemplateService {
    @Autowired
    private RcsTournamentTemplateMapper templateMapper;
    @Autowired
    private RcsTournamentTemplateRefMapper templateRefMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper playMargainMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainRefMapper playMargainRefMapper;
    @Autowired
    private RcsTournamentTemplateEventMapper templateEventMapper;
    @Autowired
    private StandardSportTournamentMapper standardSportTournamentMapper;
    @Autowired
    private RcsTournamentEventTemplateMapper eventTemplateMapper;
    @Autowired
    private RcsTournamentPlayMarginTemplateMapper rcsTournamentPlayMarginTemplateMapper;
    @Autowired
    private RcsMatchEventTypeInfoServiceImpl rcsMatchEventTypeInfoService;
    @Autowired
    private TournamentTemplatePushService tournamentTemplatePushService;
    @Autowired
    private MarketCategorySetService marketCategorySetService;
    @Autowired
    private IRcsMatchTemplateModifyService rcsMatchTemplateModifyService;
    @Autowired
    private RcsTournamentTemplateAcceptConfigMapper rcsTournamentTemplateAcceptConfigMapper;
    @Autowired
    private RcsTournamentTemplateAcceptEventMapper rcsTournamentTemplateAcceptEventMapper;
    @Autowired
    private IRcsOperationLogService logService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private RcsTournamentTemplateAcceptConfigSettleMapper configSettleMapper;
    @Autowired
    private RcsTournamentTemplateAcceptEventSettleMapper eventSettleMapper;

    @Autowired
    private TradeModeService tradeModeService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;
    /**
     * 首次初始化模板数据
     *
     * @throws Exception
     */
    @Override
    public void initTournament(Integer sportId, int num) {
        Integer[] matchTypes = {MatchTypeEnum.EARLY.getId(), MatchTypeEnum.LIVE.getId()};
        for (Long i = 0L; i <= num; i++) {
            for (Integer matchType : matchTypes) {
                TournamentTemplateParam param = new TournamentTemplateParam();
                param.setSportId(sportId);
                param.setMatchType(matchType);
                param.setType(1);
                param.setTypeVal(i);

                QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
                templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getType, param.getType())
                        .eq(RcsTournamentTemplate::getTypeVal, param.getTypeVal())
                        .eq(RcsTournamentTemplate::getMatchType, param.getMatchType())
                        .eq(RcsTournamentTemplate::getSportId, param.getSportId());
                int count = templateMapper.selectCount(templateQueryWrapper).intValue();
                if (count == 0) {
                    initTournamentTemplate(param);
                }
            }
        }
    }

    /**
     * 新增模板数据
     *
     * @param param
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long initTournamentTemplate(TournamentTemplateParam param) {
        // 初始化模板基表
        RcsTournamentTemplate rcsTournamentTemplate = new RcsTournamentTemplate();
        rcsTournamentTemplate.setSportId(param.getSportId());
        rcsTournamentTemplate.setType(param.getType());
        rcsTournamentTemplate.setTypeVal(param.getTypeVal());
        rcsTournamentTemplate.setMatchType(param.getMatchType());
        rcsTournamentTemplate.setDataSourceCode(DataSourceWeightEnum.SR.getName());
        rcsTournamentTemplate.setBusinesMatchPayVal(1000000L);
        rcsTournamentTemplate.setUserMatchPayVal(200000L);
        rcsTournamentTemplate.setTemplateName(param.getTemplateName());
        rcsTournamentTemplate.setOddsChangeStatus(1);
        rcsTournamentTemplate.setIfWarnSuspended(1);
        rcsTournamentTemplate.setAoConfigValue("{\"perId\":45,\"oneInjTime\":2,\"twoInjTime\":4,\"htDrawAdj\":5,\"ftDrawAdj\":10,\"refresh\":30,\"zeroOneFive\":0.295,\"oneFiveThree\":0.325,\"threeHt\":0.38,\"htSix\":0.3,\"sixSevenFive\":0.305,\"sevenFiveFt\":0.395}");
        rcsTournamentTemplate.setMtsConfigValue("{\"mtsSwitch\":0,\"contactPercentage\":4}");
        rcsTournamentTemplate.setUserPendingOrderPayVal(200000L);
        rcsTournamentTemplate.setBusinesPendingOrderPayVal(1000000L);
        rcsTournamentTemplate.setPendingOrderRate(100);
        rcsTournamentTemplate.setUserPendingOrderCount(10);
        rcsTournamentTemplate.setPendingOrderStatus(0);
        templateMapper.insertBatch(rcsTournamentTemplate);
        // 获取玩法参数和margin盘口参数默认值
        RcsTournamentPlayMarginTemplate rcsTournamentPlayMarginTemplate = new RcsTournamentPlayMarginTemplate();
        rcsTournamentPlayMarginTemplate.setMatchType(param.getMatchType());
        rcsTournamentPlayMarginTemplate.setSportId(param.getSportId());
        rcsTournamentPlayMarginTemplate.setLevel(param.getTypeVal());
        List<RcsTournamentPlayMarginTemplate> rcsTournamentPlayMarginTemplates = rcsTournamentPlayMarginTemplateMapper.queryPlayTemplateInitData(rcsTournamentPlayMarginTemplate);
        if (!CollectionUtils.isEmpty(rcsTournamentPlayMarginTemplates)) {
            // 构建玩法参数数据
            for (RcsTournamentPlayMarginTemplate margainTemplate : rcsTournamentPlayMarginTemplates) {
                RcsTournamentTemplatePlayMargain margain = new RcsTournamentTemplatePlayMargain();
                BeanCopyUtils.copyProperties(margainTemplate, margain);
                margain.setTemplateId(rcsTournamentTemplate.getId());
                margain.setMatchType(param.getMatchType());
                margain.setPlayId(margainTemplate.getPlayId());
                margain.setIfWarnSuspended(1);
                //初始化默认数据
                margain.setOddsChangeStatus(1);
                margain.setOddsChangeValue(new BigDecimal(4));
                //篮球模板玩法里最大盘口数移入分时节点，所以将模板里玩法最大盘口数设置为空
                if(param.getSportId()==2) {
                    margain.setMarketCount(null);
                    margain.setViceMarketRatio(null);
                }
                playMargainMapper.insertBatch(Arrays.asList(margain));

                RcsTournamentTemplatePlayMargainRef margainRef = new RcsTournamentTemplatePlayMargainRef();
                BeanCopyUtils.copyProperties(margainTemplate, margainRef);
                if (param.getMatchType() == MatchTypeEnum.EARLY.getId().intValue()) {
                    //早盘初始化第一个时间节点为30天（30*60*60*24）
                    margainRef.setTimeVal(2592000L);
                } else if (param.getMatchType().equals(MatchTypeEnum.LIVE.getId())) {
                    //滚球初始化第一个时间节点为0
                    if(param.getSportId().equals(7)){
                        margainRef.setTimeVal(1L);
                    }else{
                        margainRef.setTimeVal(0L);
                    }
                }
                //篮球模板玩法里最大盘口数移入分时节点，其他球种保持原样，所以设置分时节点数据为空
                if(param.getSportId()!=2) {
                    margainRef.setMarketCount(null);
                    margainRef.setViceMarketRatio(null);
                }
                margainRef.setMargainId(margain.getId());
                margainRef.setStatus(1);
                playMargainRefMapper.insertBatch(Arrays.asList(margainRef));
            }
        }
        // 滚球数据单独处理
        if (param.getMatchType().equals(MatchTypeEnum.LIVE.getId())) {
            // 初始化事件结算/审核时间
            List<RcsTournamentTemplateEvent> templateEventList = new ArrayList<>();
            QueryWrapper<RcsTournamentEventTemplate> eventTemplateQueryWrapper = new QueryWrapper<>();
            eventTemplateQueryWrapper.lambda().eq(RcsTournamentEventTemplate::getSportId, param.getSportId()).orderByAsc(RcsTournamentEventTemplate::getOrderNo);
            List<RcsTournamentEventTemplate> list = eventTemplateMapper.selectList(eventTemplateQueryWrapper);
            for (RcsTournamentEventTemplate templateEvent : list) {
                RcsTournamentTemplateEvent event = new RcsTournamentTemplateEvent();
                event.setTemplateId(rcsTournamentTemplate.getId());
                event.setEventCode(templateEvent.getEventCode());
                event.setEventDesc(templateEvent.getTemplateText());
                event.setEventHandleTime(templateEvent.getAuditTime());
                event.setSettleHandleTime(templateEvent.getBillTime());
                event.setSortNo(templateEvent.getOrderNo());
                templateEventList.add(event);
            }
            if (templateEventList.size() > 0) {
                templateEventMapper.insertBatch(templateEventList);
            }
            // 初始化滚球接拒单参数数据
            /*QueryWrapper<RcsMarketCategorySet> rcsMarketCategorySetWrapper = new QueryWrapper<>();
            rcsMarketCategorySetWrapper.lambda().eq(RcsMarketCategorySet::getSportId, param.getSportId())
                    .eq(RcsMarketCategorySet::getType, 1)
                    .eq(RcsMarketCategorySet::getStatus, 2);
            List<RcsMarketCategorySet> categorySetIds = marketCategorySetService.list(rcsMarketCategorySetWrapper);
            if (!CollectionUtils.isEmpty(categorySetIds)) {
                for (RcsMarketCategorySet set : categorySetIds) {
                    RcsTournamentTemplateAcceptConfig config = new RcsTournamentTemplateAcceptConfig();
                    config.setTemplateId(rcsTournamentTemplate.getId());
                    config.setCategorySetId(Integer.valueOf(set.getId() + ""));
                    config.setDataSource("SR");
                    config.setNormal(3);
                    config.setMinWait(10);
                    config.setMaxWait(120);
                    config.setCreateTime(new Date());
                    config.setUpdateTime(new Date());
                    rcsMatchEventTypeInfoService.insertEventList(config);
                }
            }
            //初始化其他玩法集id -1
            RcsTournamentTemplateAcceptConfig config = new RcsTournamentTemplateAcceptConfig();
            config.setTemplateId(rcsTournamentTemplate.getId());
            config.setCategorySetId(-1);
            config.setDataSource("SR");
            config.setNormal(3);
            config.setMinWait(10);
            config.setMaxWait(120);
            config.setCreateTime(new Date());
            config.setUpdateTime(new Date());
            rcsMatchEventTypeInfoService.insertEventList(config);*/
        }
        return rcsTournamentTemplate.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTournamentTemplatePlay(TournamentTemplateUpdateParam param) {
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
        templateQueryWrapper.lambda().notIn(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId());
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId());
        if (!ObjectUtils.isEmpty(param.getMatchType())) {
            templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getMatchType, param.getMatchType());
        }
        if (!ObjectUtils.isEmpty(param.getTypeVal())) {
            templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getTypeVal, param.getTypeVal());
        }
        List<RcsTournamentTemplate> list = templateMapper.selectList(templateQueryWrapper);
        log.info("::{}::本次需要增加玩法的模板：{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(list));
        if (CollectionUtils.isEmpty(list)) {
            throw new IllegalArgumentException("未找到联赛模板数据！");
        }
        for (RcsTournamentTemplate template : list) {
            for (TournamentTemplatePlayMargainParam playMarginParam : param.getPlayMargainList()) {
                QueryWrapper<RcsTournamentTemplatePlayMargain> playWrapper = new QueryWrapper();
                playWrapper.lambda().eq(RcsTournamentTemplatePlayMargain::getTemplateId, template.getId())
                        .eq(RcsTournamentTemplatePlayMargain::getPlayId, playMarginParam.getPlayId());
                RcsTournamentTemplatePlayMargain playMargin = playMargainMapper.selectOne(playWrapper);
                if (ObjectUtils.isEmpty(playMargin)) {
                    playMarginParam.setId(null);
                    playMarginParam.setTemplateId(template.getId());
                    playMarginParam.setMatchType(template.getMatchType());
                    playMargainMapper.insertBatch(Arrays.asList(playMarginParam));

                    if(!ObjectUtils.isEmpty(playMargin) ){
                        //kir-1255需求 赔率接拒变动范围
                        //开关打开才需录入缓存
                        if(playMargin.getOddsChangeStatus() != null && playMargin.getOddsChangeStatus().equals(1)){
                            String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                            redisUtils.set(String.format(redisKey, param.getTypeVal(), playMargin.getPlayId(), playMargin.getMatchType()), String.valueOf(playMargin.getOddsChangeValue()));
                        }else{
                            String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                            redisUtils.del(String.format(redisKey, param.getTypeVal(), playMargin.getPlayId(), playMargin.getMatchType()));
                        }
                    }

                    Long newMarginId = playMarginParam.getId();

                    Long timeVal = template.getMatchType() == 1 ? 2592000L : 0L;
                    QueryWrapper<RcsTournamentTemplatePlayMargainRef> playMarginRefWrapper = new QueryWrapper();
                    playMarginRefWrapper.lambda().eq(RcsTournamentTemplatePlayMargainRef::getMargainId, newMarginId)
                            .eq(RcsTournamentTemplatePlayMargainRef::getTimeVal, timeVal);
                    RcsTournamentTemplatePlayMargainRef playMarginRef = playMargainRefMapper.selectOne(playMarginRefWrapper);
                    if (ObjectUtils.isEmpty(playMarginRef) && !CollectionUtils.isEmpty(playMarginParam.getPlayMargainRefParamList())) {
                        TournamentTemplatePlayMargainRefParam playMarginRefParam = playMarginParam.getPlayMargainRefParamList().get(0);
                        playMarginRefParam.setId(null);
                        playMarginRefParam.setMargainId(newMarginId);
                        playMarginRefParam.setTimeVal(timeVal);
                        playMarginRefParam.setStatus(1);
                        playMargainRefMapper.insertBatch(Arrays.asList(playMarginRefParam));
                    }
                }
            }
            //kir-1368
            //rcsMatchTemplateModifyService.updateMarketConfig(Long.valueOf(template.getSportId()), template.getTypeVal(), TradeLevelEnum.MATCH.getLevel(), template, null);

            //kir-1368
            //List<RcsTournamentTemplatePlayMargain> margains = JsonFormatUtils.fromJsonArray(JSONObject.toJSONString(param.getPlayMargainList()), RcsTournamentTemplatePlayMargain.class);
            //rcsMatchTemplateModifyService.updateMarketConfig(Long.valueOf(template.getSportId()), template.getTypeVal(), TradeLevelEnum.CASH_OUT.getLevel(), null, margains);
        }
    }

    @Override
    public void addTournamentTemplateLiveEvent(TournamentTemplateAddEventParam param) {
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId());
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getMatchType, NumberUtils.INTEGER_ZERO.intValue());
        templateQueryWrapper.lambda().notIn(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId());
        if (!ObjectUtils.isEmpty(param.getTypeVal())) {
            templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getTypeVal, param.getTypeVal());
        }
        List<RcsTournamentTemplate> list = templateMapper.selectList(templateQueryWrapper);
        log.info("::{}::本次需要增加模板的接拒单事件：{}",CommonUtil.getRequestId(param.getTypeVal(),param.getId()), JsonFormatUtils.toJson(list));
        if (CollectionUtils.isEmpty(list)) {
            throw new IllegalArgumentException("未找到联赛模板数据！");
        }
        //获取有效的玩法集
        QueryWrapper<RcsMarketCategorySet> rcsMarketCategorySetWrapper = new QueryWrapper<>();
        rcsMarketCategorySetWrapper.lambda().eq(RcsMarketCategorySet::getSportId, param.getSportId())
                .eq(RcsMarketCategorySet::getType, 1)
                .eq(RcsMarketCategorySet::getStatus, 2);
        List<RcsMarketCategorySet> categorySetList = marketCategorySetService.list(rcsMarketCategorySetWrapper);
        List<Integer> categorySetIds = categorySetList.stream().map(map -> map.getId().intValue()).collect(Collectors.toList());
        categorySetIds.add(-1); //添加其他玩法集
        for (RcsTournamentTemplate template : list) {
            QueryWrapper<RcsTournamentTemplateAcceptConfig> configQueryWrapper = new QueryWrapper();
            configQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getTemplateId, template.getId());
            configQueryWrapper.lambda().in(RcsTournamentTemplateAcceptConfig::getCategorySetId, categorySetIds);
            List<RcsTournamentTemplateAcceptConfig> configList = rcsTournamentTemplateAcceptConfigMapper.selectList(configQueryWrapper);
            List<Integer> configCategorySetIds = configList.stream().map(map -> map.getCategorySetId()).collect(Collectors.toList());
            //根据模板和玩法集，添加新增事件
            for (RcsTournamentTemplateAcceptConfig config : configList) {
                addNewEvent(param, config);
            }
            //玩法管理集与接拒单配置的玩法集数量不一致处理
            if (categorySetIds.size() != configCategorySetIds.size()) {
                categorySetIds.removeAll(configCategorySetIds);
                //log.info("::{}::本次需要增加模板的接拒单事件-需要新增的玩法:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(categorySetIds));
                for (Integer setId : categorySetIds) {
                    RcsTournamentTemplateAcceptConfig config = new RcsTournamentTemplateAcceptConfig();
                    config.setTemplateId(template.getId());
                    config.setCategorySetId(setId);
                    config.setDataSource("SR");
                    config.setNormal(3);
                    config.setMinWait(10);
                    config.setMaxWait(120);
                    config.setCreateTime(new Date());
                    config.setUpdateTime(new Date());
                    rcsTournamentTemplateAcceptConfigMapper.insert(config);
                    addNewEvent(param, config);
                }
            }
        }
    }

    private void addNewEvent(TournamentTemplateAddEventParam param, RcsTournamentTemplateAcceptConfig config) {
        List<RcsTournamentTemplateAcceptEvent> insertConfig = Lists.newArrayList();
        for (TournamentTemplateAddEventParam.EventTypeConfig acceptConfig : param.getEventList()) {
            String eventType = acceptConfig.getEventType();
            List<TournamentTemplateAddEventParam.EventConfig> eventConfig = acceptConfig.getEventConfig();
            for (TournamentTemplateAddEventParam.EventConfig event : eventConfig) {
                if (ObjectUtils.isEmpty(event.getIsPenalty())) {
                    RcsTournamentTemplateAcceptEvent addEvent = new RcsTournamentTemplateAcceptEvent();
                    addEvent.setAcceptConfigId(config.getId());
                    addEvent.setEventType(eventType);
                    addEvent.setEventCode(event.getEventCode());
                    addEvent.setEventName(event.getEventName());
                    addEvent.setStatus(NumberUtils.INTEGER_ONE.intValue());
                    insertConfig.add(addEvent);
                }
                //如果是点球大战，只加入到点球类玩法集中的封盘事件中
                if (config.getCategorySetId().intValue() == 134 && !ObjectUtils.isEmpty(event.getIsPenalty()) && event.getIsPenalty() == NumberUtils.INTEGER_ONE.intValue()) {
                    RcsTournamentTemplateAcceptEvent addEvent = new RcsTournamentTemplateAcceptEvent();
                    addEvent.setAcceptConfigId(config.getId());
                    addEvent.setEventType(eventType);
                    addEvent.setEventCode(event.getEventCode());
                    addEvent.setEventName(event.getEventName());
                    addEvent.setStatus(NumberUtils.INTEGER_ONE.intValue());
                    insertConfig.add(addEvent);
                }
            }
        }
        log.info("::{}::本次需要增加模板的接拒单事件：{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(insertConfig));
        rcsTournamentTemplateAcceptEventMapper.updateMatchEventConfig(insertConfig);
    }

    /**
     * 查询等级模板和专用模板
     *
     * @param map
     * @return
     */
    @Override
    public List<TemplateMenuListDto> menuList(Map<String, Object> map) {
        return templateMapper.menuList(map);
    }

    /**
     * 修改联赛等级
     *
     * @param param
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTournamentLevel(UpdateTournamentLevelParam param) {
        String linkId = "updateTournamentLevel_"+param.getId();
        StandardSportTournament standardSportTournament = standardSportTournamentMapper.selectById(param.getId());
        if (standardSportTournament != null) {
            Integer oldTournamentLevel = standardSportTournament.getTournamentLevel();
            // 通知融合更新联赛等级和是否热门
            log.info("::{}::变更联赛等级，通知融合:{}",linkId,JSONObject.toJSONString(param));
            tournamentTemplatePushService.putTournamentLevel(param);
            log.info("::{}::变更联赛等级，通知融合完成",linkId);
            // 更新联赛等级基础数据表
            standardSportTournament.setTournamentLevel(param.getLevel());
            standardSportTournament.setTargetProfitRate(param.getTargetProfitRate());
            standardSportTournament.setModifyTime(System.currentTimeMillis());
            standardSportTournament.setMtsOddsChangeValue(param.getMtsOddsChangeValue());
            standardSportTournament.setOddsChangeStatus(param.getOddsChangeStatus());
            standardSportTournamentMapper.updateById(standardSportTournament);
            // 将变更等级的联赛绑定到新等级模板上
            Long preTemplateId = null;
            Long liveTemplateId = null;
            if (oldTournamentLevel != param.getLevel()) {
                QueryWrapper<RcsTournamentTemplate> tournamentTemplateQueryWrapper = new QueryWrapper<>();
                tournamentTemplateQueryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.LEVEL.getId())
                        .eq(RcsTournamentTemplate::getTypeVal, param.getLevel())
                        .eq(RcsTournamentTemplate::getSportId, standardSportTournament.getSportId());
                List<RcsTournamentTemplate> tournamentTemplateList = templateMapper.selectList(tournamentTemplateQueryWrapper);
                if (!CollectionUtils.isEmpty(tournamentTemplateList)) {
                    for (RcsTournamentTemplate rcsTournamentTemplate : tournamentTemplateList) {
                        if (rcsTournamentTemplate.getMatchType().equals(MatchTypeEnum.EARLY.getId())) {
                            preTemplateId = rcsTournamentTemplate.getId();
                        } else if (rcsTournamentTemplate.getMatchType().equals(MatchTypeEnum.LIVE.getId())) {
                            liveTemplateId = rcsTournamentTemplate.getId();
                        }
                    }
                }
            }
            UpdateTournamentTemplateParam updateTournamentTemplateParam = new UpdateTournamentTemplateParam();
            updateTournamentTemplateParam.setId(param.getId());
            updateTournamentTemplateParam.setTimestamp(System.currentTimeMillis());
            updateTournamentTemplateParam.setPreLemplateId(preTemplateId);
            updateTournamentTemplateParam.setLiveLemplateId(liveTemplateId);
            updateTournamentTemplateParam.setUserName(param.getUserName());
            updateTournamentTemplateParam.setIsPopular(param.getIsPopular());
            updateTournamentTemplateParam.setOrderDelayTime(param.getOrderDelayTime());
            updateTournamentTemplateRelationConfig(updateTournamentTemplateParam);

            TournamentLevelTemporaryLog log = new TournamentLevelTemporaryLog();
            log.setTournamentId(param.getId());
            log.setReqParam(JSONObject.toJSONString(param));
            standardSportTournamentMapper.insertTournamentLevelUpdateLog(log);

            //kir-1309-所有没有自己操盘的赛种默认读取该配置
            redisUtils.hset(String.format("rcs:tournament:property:%s", String.valueOf(param.getId())), "MTSOddsChangeValue", String.valueOf(param.getMtsOddsChangeValue()));
            redisUtils.hset(String.format("rcs:tournament:property:%s", String.valueOf(param.getId())), "orderDelayTime", String.valueOf(param.getOrderDelayTime()));
            redisUtils.hset(String.format("rcs:tournament:property:%s", String.valueOf(param.getId())), "oddsChangeStatus", String.valueOf(param.getOddsChangeStatus()));
        }
    }

    /**
     * 更新绑定有关系
     *
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTournamentTemplateRelationConfig(UpdateTournamentTemplateParam param) {
        // 将变更等级的联赛绑定到新等级模板上
        RcsTournamentTemplateRef newRef = new RcsTournamentTemplateRef();
        newRef.setTemplateId(param.getPreLemplateId());
        newRef.setLiveTemplateId(param.getLiveLemplateId());
        newRef.setTournamentId(param.getId());
        newRef.setIsPopular(param.getIsPopular());
        newRef.setUpdateTime(new Date());
        newRef.setOrderDelayTime(param.getOrderDelayTime());
        templateRefMapper.insertOrUpdate(newRef);
    }

    /**
     * 将多个单词首字母大写
     * @param str
     * @return
     */
    private String wordToUpperCase(String str) {
        String[] s = str.split(" ");
        //用于接收转成大写的单词
        String str2 = new String();
        for (int i = 0; i < s.length; i++) {
            s[i] = s[i].substring(0, 1).toUpperCase()+s[i].substring(1);
            //将取到的第一个字符转换成大写，在在其后面拼接上其余部分
            if(i==s.length-1){
                str2 = str2+s[i];
            }else{
                str2 = str2+s[i]+" ";
            }
        }
        return str2;
    }

    /**
     * 获取模板和玩法参数数据
     *
     * @param param
     * @return
     */
    @Override
    public TournamentTemplateVo queryTournamentTemplateAndPlay(TournamentTemplateParam param, String lang) {
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId())
                .eq(RcsTournamentTemplate::getType, param.getType())
                .eq(RcsTournamentTemplate::getTypeVal, param.getTypeVal())
                .eq(RcsTournamentTemplate::getMatchType, param.getMatchType());
        RcsTournamentTemplate tournamentTemplate = templateMapper.selectOne(templateQueryWrapper);
        log.info("::{}::AO数据源查询模板信息:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(tournamentTemplate));
        if (tournamentTemplate == null) {
            return null;
        }
        TournamentTemplateVo resultVo = new TournamentTemplateVo();
        BeanCopyUtils.copyProperties(tournamentTemplate, resultVo);
        log.info("::{}::AO数据源查询模板信息转换后:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(resultVo));
        // 滚球单独处理事件
        if (param.getMatchType().equals(MatchTypeEnum.LIVE.getId())) {
            List<RcsTournamentTemplateEvent> list = templateEventMapper.getTournamentTemplateEventList(tournamentTemplate.getId(), tournamentTemplate.getSportId());
            list.forEach(obj -> {
                if (obj.getId() == null) {
                    RcsTournamentTemplateEvent event = new RcsTournamentTemplateEvent();
                    BeanCopyUtils.copyProperties(obj, event);
                    event.setTemplateId(tournamentTemplate.getId());
                    event.setCreateTime(new Date());
                    event.setUpdateTime(new Date());
                    templateEventMapper.insert(event);
                    obj.setId(event.getId());
                    obj.setTemplateId(tournamentTemplate.getId());
                }

                //国际化
                if(lang.equals("en")){
                    obj.setEventDesc(wordToUpperCase(obj.getEventCode().replace("_", " ")));
                }
            });
            List<TournamentTemplateEventVo> listVo = BeanCopyUtils.copyPropertiesList(list, TournamentTemplateEventVo.class);
            resultVo.setTemplateEventList(listVo); 
        }
        // 获取风控型玩法集
        QueryWrapper<RcsMarketCategorySet> setWrapper = new QueryWrapper();
        setWrapper.lambda().eq(RcsMarketCategorySet::getSportId, param.getSportId())
                .eq(RcsMarketCategorySet::getType, 1)
                .eq(RcsMarketCategorySet::getStatus, 2)
                .orderByAsc(RcsMarketCategorySet::getDisplaySort);
        List<RcsMarketCategorySet> setList = marketCategorySetService.list(setWrapper);
        if (!CollectionUtils.isEmpty(setList)) {
            List<TournamentTemplateCategorySetVo> setVoList = BeanCopyUtils.copyPropertiesList(setList, TournamentTemplateCategorySetVo.class);
            List<Integer> playIds = Lists.newArrayList();
            for (TournamentTemplateCategorySetVo set : setVoList) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("categorySetId", set.getId());
                map.put("templateId", tournamentTemplate.getId());
                map.put("matchType", tournamentTemplate.getMatchType());
                map.put("playName", param.getPlayName());
                map.put("type", tournamentTemplate.getType());
                map.put("sportId", tournamentTemplate.getSportId());
                map.put("lang","$."+lang);
                List<RcsTournamentTemplatePlayMargain> list = playMargainMapper.searchSellPlay(map);
                List<TournamentTemplatePlayMargainVo> playMargainVo = BeanCopyUtils.copyPropertiesList(list, TournamentTemplatePlayMargainVo.class);
                Long sellNum = playMargainVo.stream().filter(e -> e.getIsSell() != null && e.getIsSell() == 1).count();
                
                set.setCategoryList(playMargainVo);
                set.setTotalNum(playMargainVo.size());
                set.setSellNum(Integer.valueOf(sellNum + ""));
                String namecode = set.getNameCode();
                I18nBean i18nBean=rcsLanguageInternationService.getPlayerLanguage(namecode);
                if(lang.equals("en") && !i18nBean.getEn().toString().equals("")){
                    set.setName(i18nBean.getEn());
                }

                // 获取所有已关联玩法集的玩法id
        		playMargainVo.forEach(obj -> {
        					playIds.add(obj.getPlayId());
                        });
            }
            // 处理没有配置玩法集的玩法，归类到其他玩法集
            Map<String, Object> map = Maps.newHashMap();
            map.put("templateId", tournamentTemplate.getId());
            map.put("matchType", tournamentTemplate.getMatchType());
            map.put("playName", param.getPlayName());
            map.put("playIds", playIds);
            map.put("type", tournamentTemplate.getType());
            map.put("sportId", tournamentTemplate.getSportId());
            map.put("lang","$."+lang);
            List<RcsTournamentTemplatePlayMargain> otherPlaysList = playMargainMapper.searchOtherSellPlay(map);
            if (!CollectionUtils.isEmpty(otherPlaysList)) {
                List<TournamentTemplatePlayMargainVo> otherPlayMargainVo = BeanCopyUtils.copyPropertiesList(otherPlaysList, TournamentTemplatePlayMargainVo.class);
                Long otherPlaySellNum = otherPlayMargainVo.stream().filter(e -> e.getIsSell() != null && e.getIsSell() == 1).count();
                TournamentTemplateCategorySetVo otherSet = new TournamentTemplateCategorySetVo();
                otherSet.setId(-1L);
                otherSet.setName(lang.equals("en")?"other":"其他玩法");
                otherSet.setCategoryList(otherPlayMargainVo);
                otherSet.setTotalNum(otherPlayMargainVo.size());
                otherSet.setSellNum(Integer.valueOf(otherPlaySellNum + ""));
                otherSet.setCategoryList(otherPlayMargainVo);
                setVoList.add(otherSet);
            }
            // 设置玩法总数量，已开售总数量
            Integer totalNum = setVoList.stream().mapToInt(TournamentTemplateCategorySetVo::getTotalNum).sum();
            Integer sellNum = setVoList.stream().mapToInt(TournamentTemplateCategorySetVo::getSellNum).sum();
            resultVo.setTotalNum(totalNum);
            resultVo.setSellNum(sellNum);
            resultVo.setCategorySetList(setVoList);
        }

        //获取玩法赔率源设置数据
        Map<String, Object> map = Maps.newHashMap();
        map.put("template_id", tournamentTemplate.getId());
        List<RcsTournamentTemplatePlayMargain> playOddsConfig = playMargainMapper.selectByMap(map);
        if (!CollectionUtils.isEmpty(playOddsConfig)) {
            Map<String, List<RcsTournamentTemplatePlayMargain>> playOddsConfigMap = playOddsConfig.stream().filter(filter -> null != filter.getDataSource() && !"".equals(filter.getDataSource())).collect(Collectors.groupingBy(RcsTournamentTemplatePlayMargain::getDataSource, LinkedHashMap::new, Collectors.toList()));
            //赛事模板，过滤未关联商业数据源，玩法赔率源
            if (param.getType() == TempTypeEnum.MATCH.getId()) {
                StandardMatchInfo info = standardMatchInfoMapper.selectById(param.getTypeVal());
                if (StringUtils.isNotBlank(info.getThirdMatchListStr())) {
                    List<ThirdDataSourceCodeVo> list = JSONArray.parseArray(info.getThirdMatchListStr(), ThirdDataSourceCodeVo.class);
                    List<String> dataSourceList = list.stream().filter(filter -> null != filter.getCommerce() && !"RB".equals(filter.getDataSourceCode())
                            && !"BT".equals(filter.getDataSourceCode()) && filter.getCommerce().equals(String.valueOf(org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE)))
                            .map(ThirdDataSourceCodeVo::getDataSourceCode).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(dataSourceList)) {
                        Map<String, List<RcsTournamentTemplatePlayMargain>> matchPlayOddsConfigMap = Maps.newHashMap();
                        resultVo.setCategoryOddsConfig(matchPlayOddsConfigMap);
                        for (String dataSource : dataSourceList) {
                            matchPlayOddsConfigMap.put(dataSource, playOddsConfigMap.get(dataSource));
                        }
                    }
                }
            } else {
                //设置联赛模板玩法赔率源
                if (!CollectionUtils.isEmpty(playOddsConfigMap)) {
                    resultVo.setCategoryOddsConfig(playOddsConfigMap);
                }
            }
        }

        //标记子联赛是否包含父联赛专用模板
        if (param.getType() == TempTypeEnum.TOUR.getId() && StringUtils.isNotBlank(param.getFatherTournamentId())) {
            QueryWrapper<RcsTournamentTemplate> fatherWrapper = new QueryWrapper();
            fatherWrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId())
                    .eq(RcsTournamentTemplate::getType, TempTypeEnum.TOUR.getId())
                    .eq(RcsTournamentTemplate::getTypeVal, param.getFatherTournamentId())
                    .eq(RcsTournamentTemplate::getMatchType, param.getMatchType());
            RcsTournamentTemplate fatherTemplate = templateMapper.selectOne(fatherWrapper);
            if (!ObjectUtils.isEmpty(fatherTemplate)) {
                resultVo.setFatherTemplateId(fatherTemplate.getId());
            }
        }
        log.info("::{}::AO数据源查询模板信息返回:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(resultVo));
        return resultVo;
    }


    /**
     * 获取分时margin节点信息
     *
     * @param tournamentTemplateParam
     * @return
     */
    @Override
    public TournamentTemplatePlayMargainRefVo queryTournamentTemplatePlayMargin
    (TournamentTemplatePlayMargainRefParam tournamentTemplateParam) {
        QueryWrapper<RcsTournamentTemplatePlayMargainRef> wrapper = new QueryWrapper();
        wrapper.lambda().eq(RcsTournamentTemplatePlayMargainRef::getMargainId, tournamentTemplateParam.getMargainId())
                .eq(RcsTournamentTemplatePlayMargainRef::getTimeVal, tournamentTemplateParam.getTimeVal());
        RcsTournamentTemplatePlayMargainRef margainRef = playMargainRefMapper.selectOne(wrapper);
        if (margainRef != null) {
            TournamentTemplatePlayMargainRefVo margainRefVo = BeanCopyUtils.copyProperties(margainRef, TournamentTemplatePlayMargainRefVo.class);
            return margainRefVo;
        } else {
            RcsTournamentTemplatePlayMargainRef lastMargainRef = null;
            if (tournamentTemplateParam.getMatchType().equals(MatchTypeEnum.EARLY.getId())) {
                lastMargainRef = playMargainRefMapper.selectPreLastPlayMargainRef(tournamentTemplateParam);
            } else if (tournamentTemplateParam.getMatchType().equals(MatchTypeEnum.LIVE.getId())) {
                lastMargainRef = playMargainRefMapper.selectLiveLastPlayMargainRef(tournamentTemplateParam);
            }
            if (lastMargainRef != null) {
                lastMargainRef.setId(null);
                lastMargainRef.setTimeVal(tournamentTemplateParam.getTimeVal());
                TournamentTemplatePlayMargainRefVo newMargainRefVo = BeanCopyUtils.copyProperties(lastMargainRef, TournamentTemplatePlayMargainRefVo.class);
                return newMargainRefVo;
            }
        }
        return null;
    }

    /**
     * @return java.lang.Boolean
     * @Description 更新模板配置信息
     * @Param [param]updateByPrimaryKeySelective
     * @Author toney
     * @Date 19:57 2020/5/12
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(TournamentTemplateUpdateParam param) {
        checkTemplateName(param);
        // 修改模板数据
        rcsMatchTemplateModifyService.modifyTemplate(param);
        // 修改玩法数据
        List<TournamentTemplatePlayMargainParam> playMargain = param.getPlayMargainList();
        if (!CollectionUtils.isEmpty(playMargain)) {
            rcsMatchTemplateModifyService.modifyPlayMargain(playMargain);
        }
        // 修改滚球结算审核事件
        List<RcsTournamentTemplateEvent> eventList = param.getTemplateEventList();
        if (!CollectionUtils.isEmpty(eventList)) {
            rcsMatchTemplateModifyService.modifyTemplateEvent(eventList);
        }
        // 修改滚球接拒单事件数据
        List<RcsTournamentTemplateAcceptConfig> acceptConfig = param.getAcceptConfigList();
        if (!CollectionUtils.isEmpty(acceptConfig)) {
            rcsMatchTemplateModifyService.modifyAcceptConfig(acceptConfig);
        }
    }

    public void copyTemplateData(Long templateId, Long newTemplateId) {
        // 根据模板id，查询等级模板事件数据，生成专用模板事件数据
        Map<String, Object> eventMap = Maps.newHashMap();
        eventMap.put("template_id", templateId);
        List<RcsTournamentTemplateEvent> eventList = templateEventMapper.selectByMap(eventMap);
        if (!CollectionUtils.isEmpty(eventList)) {
            for (RcsTournamentTemplateEvent event : eventList) {
                event.setId(null);
                event.setTemplateId(newTemplateId);
            }
            templateEventMapper.insertBatch(eventList);
        }
        // 根据模板id，查询等级模板玩法数据，生成专用模板玩法数据
        Map<String, Object> playMargainMap = Maps.newHashMap();
        playMargainMap.put("template_id", templateId);
        List<RcsTournamentTemplatePlayMargain> playMargainList = playMargainMapper.selectByMap(playMargainMap);
        if (!CollectionUtils.isEmpty(playMargainList)) {
            List<RcsTournamentTemplatePlayMargainRef> newPlayMargainRefList = new ArrayList<>();
            for (RcsTournamentTemplatePlayMargain playMargain : playMargainList) {
                // 获取旧玩法和分时margin数据
                Long oldMargainId = playMargain.getId();
                Map<String, Object> playMargainRefMap = Maps.newHashMap();
                playMargainRefMap.put("margain_id", oldMargainId);
                List<RcsTournamentTemplatePlayMargainRef> playMargainRefList = playMargainRefMapper.selectByMap(playMargainRefMap);
                // copy生成赛事玩法数据
                playMargain.setId(null);
                playMargain.setTemplateId(newTemplateId);
                playMargainMapper.insertBatch(Arrays.asList(playMargain));
                Long newMargainId = playMargain.getId();
                for (RcsTournamentTemplatePlayMargainRef playMargainRef : playMargainRefList) {
                    playMargainRef.setId(null);
                    playMargainRef.setMargainId(newMargainId);
                    newPlayMargainRefList.add(playMargainRef);
                }
            }
            if (!CollectionUtils.isEmpty(newPlayMargainRefList)) {
                // 生成赛事分时margin数据
                playMargainRefMapper.insertBatch(newPlayMargainRefList);
            }
        }
        // 根据模板id，生成专用模板接拒单事件数据
        Map<String, Object> acceptConfigMap = Maps.newHashMap();
        acceptConfigMap.put("template_id", templateId);
        List<RcsTournamentTemplateAcceptConfig> acceptConfigList = rcsTournamentTemplateAcceptConfigMapper.selectByMap(acceptConfigMap);
        if (!CollectionUtils.isEmpty(acceptConfigList)) {
            for (RcsTournamentTemplateAcceptConfig acceptConfig : acceptConfigList) {
                // 获取旧接拒单数据
                Long oldAcceptConfigId = acceptConfig.getId();
                Map<String, Object> acceptEventMap = Maps.newHashMap();
                acceptEventMap.put("accept_config_id", oldAcceptConfigId);
                List<RcsTournamentTemplateAcceptEvent> newAcceptEventList = Lists.newArrayList();
                List<RcsTournamentTemplateAcceptEvent> acceptEventList = rcsTournamentTemplateAcceptEventMapper.selectByMap(acceptEventMap);
                for (RcsTournamentTemplateAcceptEvent acceptEvent : acceptEventList) {
                    acceptEvent.setId(null);
                    acceptEvent.setAcceptConfigId(null);
                    acceptEvent.setCreateTime(new Date());
                    acceptEvent.setUpdateTime(new Date());
                    newAcceptEventList.add(acceptEvent);
                }
                // copy生成专用模板接拒单事件数据
                acceptConfig.setId(null);
                acceptConfig.setTemplateId(newTemplateId);
                acceptConfig.setEvents(newAcceptEventList);
                rcsMatchEventTypeInfoService.insertEventList(acceptConfig);
            }
        }

        Map<String, Object> configSettleMap = com.google.common.collect.Maps.newHashMap();
        configSettleMap.put("template_id", templateId);
        List<RcsTournamentTemplateAcceptConfigSettle> configSettleList = configSettleMapper.selectByMap(configSettleMap);
        if (!CollectionUtils.isEmpty(configSettleList)) {
            for (RcsTournamentTemplateAcceptConfigSettle configSettle : configSettleList) {
                // 获取旧接拒单结算数据
                Long oldConfigSettleId = configSettle.getId();
                // copy生成赛事接拒单结算事件数据
                configSettle.setId(null);
                configSettle.setTemplateId(newTemplateId);
                configSettleMapper.insert(configSettle);
                Long newConfigSettleId = configSettle.getId();

                Map<String, Object> eventSettleMap = com.google.common.collect.Maps.newHashMap();
                eventSettleMap.put("accept_config_settle_id", oldConfigSettleId);
                List<RcsTournamentTemplateAcceptEventSettle> eventSettleList = eventSettleMapper.selectByMap(eventSettleMap);
                for (RcsTournamentTemplateAcceptEventSettle eventSettle : eventSettleList) {
                    eventSettle.setId(null);
                    eventSettle.setAcceptConfigSettleId(newConfigSettleId);
                    eventSettleMapper.insert(eventSettle);
                }
            }
        }
    }

    /**
     * 查询配置
     *
     * @param matchInfo
     * @return
     */
    @Override
    public List<TournamentTemplateDto> query(StandardMatchInfo matchInfo, Integer matchType) {
        List<TournamentTemplateDto> list = templateMapper.queryByTournamentId(matchInfo.getStandardTournamentId(), matchInfo.getSportId().intValue(), matchType);
        if (list == null || list.size() == 0) {
            Integer tournamentLevel = 0;
            if (matchInfo.getTournamentLevel() == null) {
                StandardSportTournament standardSportTournament = standardSportTournamentMapper.selectById(matchInfo.getStandardTournamentId());
                if (standardSportTournament != null) {
                    tournamentLevel = standardSportTournament.getTournamentLevel();
                    list = templateMapper.queryByTournamentLevel(tournamentLevel, matchInfo.getSportId().intValue(), matchType);
                }
            } else {
                list = templateMapper.queryByTournamentLevel(matchInfo.getTournamentLevel(), matchInfo.getSportId().intValue(), matchType);
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSpecialTemplate(RcsTournamentTemplate param) {
        if (!ObjectUtils.isEmpty(param)) {
            Long templateId = param.getId();
            //删除模板基础表
            templateMapper.deleteById(templateId);
            log.info("::{}::删除专用模板-删除模板基础表:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(param));
            UpdateWrapper<RcsTournamentTemplateRef> updateWrapper = new UpdateWrapper<>();
            RcsTournamentTemplateRef ref = new RcsTournamentTemplateRef();
            if (param.getMatchType().intValue() == MatchTypeEnum.EARLY.getId()) {
                updateWrapper.eq("template_id", templateId);
                ref.setTemplateId(NumberUtils.LONG_ZERO);
            } else if (param.getMatchType().intValue() == MatchTypeEnum.LIVE.getId()) {
                updateWrapper.eq("live_template_id", templateId);
                ref.setLiveTemplateId(NumberUtils.LONG_ZERO);
            }
            templateRefMapper.update(ref, updateWrapper);
            removeTemplateData(templateId);
        }
    }

    public void removeTemplateData(Long templateId) {
        //删除结算审核事件
        QueryWrapper<RcsTournamentTemplateEvent> event = new QueryWrapper<>();
        event.lambda().eq(RcsTournamentTemplateEvent::getTemplateId, templateId);
        List<RcsTournamentTemplateEvent> eventList = templateEventMapper.selectList(event);
        if (!CollectionUtils.isEmpty(eventList)) {
            List<Long> eventIdsList = Lists.newArrayList();
            eventList.forEach(obj -> {
                eventIdsList.add(obj.getId());
            });
            templateEventMapper.deleteBatchIds(eventIdsList);
            log.info("::{}::删除专用模板-删除结算审核事件:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(eventList));
        }
        //删除玩法数据
        QueryWrapper<RcsTournamentTemplatePlayMargain> playMargin = new QueryWrapper<>();
        playMargin.lambda().eq(RcsTournamentTemplatePlayMargain::getTemplateId, templateId);
        List<RcsTournamentTemplatePlayMargain> playMarginList = playMargainMapper.selectList(playMargin);
        List<Long> playMarginId = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(playMarginList)) {
            playMarginList.forEach(obj -> {
                playMarginId.add(obj.getId());
            });
            playMargainMapper.deleteBatchIds(playMarginId);
            log.info("::{}::删除专用模板-删除玩法数据:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(playMarginList));
        }
        //删除margin数据
        if (!CollectionUtils.isEmpty(playMarginId)) {
            QueryWrapper<RcsTournamentTemplatePlayMargainRef> playMarginRef = new QueryWrapper<>();
            playMarginRef.lambda().in(RcsTournamentTemplatePlayMargainRef::getMargainId, playMarginId);
            List<RcsTournamentTemplatePlayMargainRef> playMarginRefList = playMargainRefMapper.selectList(playMarginRef);
            if (!CollectionUtils.isEmpty(playMarginRefList)) {
                List<Long> playMarginRefIds = Lists.newArrayList();
                playMarginRefList.forEach(obj -> {
                    playMarginRefIds.add(obj.getId());
                });
                playMargainRefMapper.deleteBatchIds(playMarginRefIds);
                log.info("::{}::删除专用模板-删除margin数据:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(playMarginRefList));
            }
        }
        //删除滚球接拒单
        RcsTournamentTemplateAcceptConfig config = new RcsTournamentTemplateAcceptConfig();
        config.setTemplateId(templateId);
        rcsMatchEventTypeInfoService.deleteEventList(config);
        log.info("::{}::删除专用模板-删除滚球接拒单:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(config));
        //删除滚球接拒单-结算
        QueryWrapper<RcsTournamentTemplateAcceptConfigSettle> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfigSettle::getTemplateId, templateId);
        List<RcsTournamentTemplateAcceptConfigSettle> configSettleList = configSettleMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(configSettleList)) {
            List<Long> configSettleIds = configSettleList.stream().map(RcsTournamentTemplateAcceptConfigSettle::getId).collect(Collectors.toList());
            QueryWrapper<RcsTournamentTemplateAcceptEventSettle> wrapper = new QueryWrapper<>();
            wrapper.lambda().in(RcsTournamentTemplateAcceptEventSettle::getAcceptConfigSettleId, configSettleIds);
            List<RcsTournamentTemplateAcceptEventSettle> eventSettleList = eventSettleMapper.selectList(wrapper);
            List<Long> eventSettleIds = eventSettleList.stream().map(RcsTournamentTemplateAcceptEventSettle::getId).collect(Collectors.toList());
            configSettleMapper.deleteBatchIds(configSettleIds);
            eventSettleMapper.deleteBatchIds(eventSettleIds);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void copyFatherSpecialTemplate(TournamentTemplateParam param) {
        Long templateId = param.getId();
        Long fatherTemplateId = Long.valueOf(param.getFatherTournamentId());
        //删除模板相关的数据
        removeTemplateData(templateId);
        //复制父联赛专用模板数据
        RcsTournamentTemplate tournamentTemplate = templateMapper.selectById(fatherTemplateId);
        if (!ObjectUtils.isEmpty(tournamentTemplate)) {
            tournamentTemplate.setId(templateId);
            templateMapper.updateTemplateById(tournamentTemplate);
            copyTemplateData(fatherTemplateId, templateId);
        }
    }

    @Override
    public void updatePlayOddsConfig(RcsTournamentTemplatePlayOddsConfigParam param) {
        List<RcsTournamentTemplatePlayOddsConfigParam.PlaysOddsConfig> list = param.getPlayOddsConfigs();
        for (RcsTournamentTemplatePlayOddsConfigParam.PlaysOddsConfig config : list) {
            if (StringUtils.isNotBlank(config.getDataSource())) {
                //处理联赛模板，先将配置了赔率源玩法清空，在进行设置赔率源
                if (ObjectUtils.isEmpty(param.getMatchId())) {
                    UpdateWrapper<RcsTournamentTemplatePlayMargain> queryWrapper = new UpdateWrapper<>();
                    queryWrapper.eq("template_id", param.getTemplateId());
                    queryWrapper.eq("data_source", config.getDataSource());
                    RcsTournamentTemplatePlayMargain playMargin = new RcsTournamentTemplatePlayMargain();
                    playMargin.setDataSource(null);
                    playMargin.setUpdateTime(new Date());
                    playMargainMapper.update(playMargin, queryWrapper);
                }
                //联赛和赛事模板公用
                if (!CollectionUtils.isEmpty(config.getPlayIds())) {
                    UpdateWrapper<RcsTournamentTemplatePlayMargain> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("template_id", param.getTemplateId());
                    updateWrapper.in("play_id", config.getPlayIds());
                    RcsTournamentTemplatePlayMargain updatePlayMargin = new RcsTournamentTemplatePlayMargain();
                    updatePlayMargin.setDataSource(config.getDataSource());
                    updatePlayMargin.setUpdateTime(new Date());
                    playMargainMapper.update(updatePlayMargin, updateWrapper);
                }
            }
        }
    }

    @Override
    public void matchSyncTourTempPlay(TournamentTemplateParam param) {
        QueryWrapper<RcsTournamentTemplate> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId())
                .eq(RcsTournamentTemplate::getMatchType, param.getMatchType())
                .eq(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId())
                .eq(RcsTournamentTemplate::getTypeVal, param.getTypeVal());
        RcsTournamentTemplate template = templateMapper.selectOne(wrapper);
        if (ObjectUtils.isEmpty(template)) {
            throw new IllegalArgumentException("未找到赛事模板数据！");
        }
        //获取赛事模板玩法
        Map<String, Object> matchMap = Maps.newHashMap();
        matchMap.put("template_id", template.getId());
        List<RcsTournamentTemplatePlayMargain> matchList = playMargainMapper.selectByMap(matchMap);
        //获取赛事对应的联赛模板玩法
        Map<String, Object> tourMap = Maps.newHashMap();
        tourMap.put("template_id", template.getCopyTemplateId());
        if (param.getMatchType().equals(NumberUtils.INTEGER_ZERO)) {
            tourMap.put("is_sell", NumberUtils.INTEGER_ONE);
        }
        List<RcsTournamentTemplatePlayMargain> tourList = playMargainMapper.selectByMap(tourMap);
        if (matchList.size() == tourList.size()) {
            throw new IllegalArgumentException("没有新玩法！");
        }
        //获取需要新增的玩法
        List<Integer> matchPlayIds = matchList.stream().map(map -> map.getPlayId()).collect(Collectors.toList());
        List<Integer> tourPlayIds = tourList.stream().map(map -> map.getPlayId()).collect(Collectors.toList());
        tourPlayIds.removeAll(matchPlayIds);
        log.info("::{}::赛事模板同步联赛模板新增玩法到开售列表-新增的玩法:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(tourPlayIds));
        //判断新增的玩法，是否在玩法管理中关闭
        List<Integer> closePlay = playMargainMapper.listClosePlayIdBySportId(param.getSportId());
        log.info("::{}::赛事模板同步联赛模板新增玩法到开售列表-玩法管理关闭的玩法:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(closePlay));
        if (!CollectionUtils.isEmpty(closePlay)) {
            tourPlayIds.removeAll(closePlay);
            log.info("::{}::赛事模板同步联赛模板新增玩法到开售列表-过滤玩法管理关闭中的玩法:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(tourPlayIds));
        }
        if (CollectionUtils.isEmpty(tourPlayIds)) {
            throw new IllegalArgumentException("没有新玩法！");
        }
        //处理新增玩法
        List<RcsTournamentTemplatePlayMargain> addPlayList = tourList.stream().filter(x -> tourPlayIds.contains(x.getPlayId())).collect(Collectors.toList());
        log.info("::{}::赛事模板同步联赛模板新增玩法到开售列表-联赛模板新增玩法信息：{}", CommonUtil.getRequestId(), JsonFormatUtils.toJson(addPlayList));
        List<RcsTournamentTemplatePlayMargainRef> newPlayMarginRefList = new ArrayList<>();
        for (RcsTournamentTemplatePlayMargain playMargin : addPlayList) {
            // 获取旧marginId
            Long oldMarginId = playMargin.getId();
            playMargin.setId(null);
            playMargin.setTemplateId(template.getId());
            if (param.getMatchType().equals(NumberUtils.INTEGER_ONE)) {
                playMargin.setIsSell(0);
            }
            playMargainMapper.insertBatch(Arrays.asList(playMargin));
            Map<String, Object> playMarginRefMap = Maps.newHashMap();
            playMarginRefMap.put("margain_id", oldMarginId);
            List<RcsTournamentTemplatePlayMargainRef> playMarginRefList = playMargainRefMapper.selectByMap(playMarginRefMap);
            for (RcsTournamentTemplatePlayMargainRef playMarginRef : playMarginRefList) {
                playMarginRef.setId(null);
                playMarginRef.setMargainId(playMargin.getId());
                newPlayMarginRefList.add(playMarginRef);
            }
        }
        if (!CollectionUtils.isEmpty(newPlayMarginRefList)) {
            playMargainRefMapper.insertBatch(newPlayMarginRefList);
        }
        // 通知融合新增玩法
        TournamentTemplateUpdateParam templateUpdateParam = BeanCopyUtils.copyProperties(template, TournamentTemplateUpdateParam.class);
        List<TournamentTemplatePlayMargainParam> marginParamList = BeanCopyUtils.copyPropertiesList(addPlayList, TournamentTemplatePlayMargainParam.class);
        templateUpdateParam.setPlayMargainList(marginParamList);
        tournamentTemplatePushService.putMatchSyncTourTempPlayData(templateUpdateParam);
        List<Long> playIdList = addPlayList.stream().map(bean -> bean.getPlayId().longValue()).collect(Collectors.toList());
        tradeModeService.basketballPlaySaleSwitchLinkage(param.getSportId().longValue(), param.getTypeVal(), playIdList, LinkedTypeEnum.SYNC_PLAY_SALE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RcsTournamentTemplate saveSpecialTemplate(TournamentTemplateUpdateParam param) {
        QueryWrapper<RcsTournamentTemplate> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId())
                .eq(RcsTournamentTemplate::getMatchType, param.getMatchType())
                .eq(RcsTournamentTemplate::getType, param.getType())
                .eq(RcsTournamentTemplate::getTypeVal, param.getTypeVal());
        RcsTournamentTemplate temp = templateMapper.selectOne(wrapper);
        Long templateId = temp.getId();
        RcsTournamentTemplate tournamentTemplate = BeanCopyUtils.copyProperties(temp, RcsTournamentTemplate.class);
        tournamentTemplate.setId(null);
        tournamentTemplate.setType(TempTypeEnum.TOUR.getId());
        tournamentTemplate.setTypeVal(System.currentTimeMillis());
        tournamentTemplate.setTemplateName("无");
        templateMapper.insertBatch(tournamentTemplate);
        Long newTemplateId = tournamentTemplate.getId();
        //先保存数据
        copyTemplateData(templateId, newTemplateId);
        return tournamentTemplate;
    }

    @Override
    public List<RcsTournamentTemplate> getSpecialTemplateDetail(TournamentTemplateParam param) {
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId());
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.TOUR.getId());
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getMatchType, param.getMatchType());
        templateQueryWrapper.lambda().orderByAsc(RcsTournamentTemplate::getTemplateName);
        List<RcsTournamentTemplate> List = templateMapper.selectList(templateQueryWrapper);
        return List;
    }

    /**
     * 检查专用模板名称是否重复
     *
     * @param param
     */
    private void checkTemplateName(TournamentTemplateUpdateParam param) {
        if (StringUtils.isNotBlank(param.getTemplateName())) {
            QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
            templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId())
                    .eq(RcsTournamentTemplate::getType, TempTypeEnum.TOUR.getId())
                    .eq(RcsTournamentTemplate::getMatchType, param.getMatchType())
                    .eq(RcsTournamentTemplate::getTemplateName, param.getTemplateName());
            List<RcsTournamentTemplate> templateList = templateMapper.selectList(templateQueryWrapper);
            if (!CollectionUtils.isEmpty(templateList)) {
                if (templateList.size() > 1) {
                    throw new IllegalArgumentException("请修改模板名称。");
                }
                RcsTournamentTemplate template = templateList.get(0);
                if ("无".equals(template.getTemplateName())) {
                    throw new IllegalArgumentException("请修改模板名称。");
                }
                if (!param.getId().equals(template.getId())) {
                    throw new IllegalArgumentException(param.getTemplateName() + ":专用模板已存在。");
                }
            }
        }
    }
}
