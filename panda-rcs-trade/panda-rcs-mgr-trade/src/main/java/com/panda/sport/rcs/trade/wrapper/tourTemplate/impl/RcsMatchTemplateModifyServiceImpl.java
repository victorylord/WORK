package com.panda.sport.rcs.trade.wrapper.tourTemplate.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.utils.StringUtils;
import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.*;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.constants.MqConstant;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsTournamentTemplateAcceptConfigAutoChangeMapper;
import com.panda.sport.rcs.mapper.RcsSysUserMapper;
import com.panda.sport.rcs.mapper.RcsTournamentTemplateAcceptConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.tourTemplate.*;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.pojo.RcsSysUser;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.tourTemplate.*;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.enums.TempTypeEnum;
import com.panda.sport.rcs.trade.init.AoDataSourceInit;
import com.panda.sport.rcs.trade.param.*;
import com.panda.sport.rcs.trade.service.*;
import com.panda.sport.rcs.trade.service.impl.OnSaleCommonServer;
import com.panda.sport.rcs.trade.service.impl.TradeModeServiceImpl;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.NumberConventer;
import com.panda.sport.rcs.trade.util.ThreadUtil;
import com.panda.sport.rcs.trade.vo.tourTemplate.PeningOrderCacheClearVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem.DataSourceCodeVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem.TournamentLevelTemplateVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem.TournamentTemplateCategoryVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem.TournamentTemplatePlayVo;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsMatchTemplateModifyService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplateService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.TournamentTemplatePushService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.mq.PlayOddsConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.trade.service.RcsTournamentTemplateApiImpl.SPECIAL_PLAY_ID;

/**
 * 更新模板和赛事模板信息
 * 接口进行拆分处理
 */
@Service
@Slf4j
public class RcsMatchTemplateModifyServiceImpl implements IRcsMatchTemplateModifyService {
    @Autowired
    private RcsTournamentTemplateMapper templateMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper playMargainMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainRefMapper playMargainRefMapper;
    @Autowired
    private RcsTournamentTemplateEventMapper templateEventMapper;
    @Autowired
    private RcsTournamentTemplateAcceptConfigAutoChangeMapper templateAcceptConfigAutoChangeMapper;
    @Autowired
    private RcsMatchEventTypeInfoServiceImpl rcsMatchEventTypeInfoService;
    @Autowired
    private RcsTournamentTemplateAcceptConfigMapper rcsTournamentTemplateAcceptConfigMapper;
    @Autowired
    private RcsTournamentTemplateAcceptEventMapper rcsTournamentTemplateAcceptEventMapper;
    @Autowired
    private TournamentTemplatePushService tournamentTemplatePushService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private RcsTournamentTemplateRefMapper templateRefMapper;
    @Autowired
    private RcsStandardSportMarketSellService rcsStandardSportMarketSellService;
    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Autowired
    private IRcsTournamentTemplateService rcsTournamentTemplateService;
    @Autowired
    private TradeModeServiceImpl tradeModeService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    AoDataSourceInit aoDataSourceInit;
    @Autowired
    RcsSysUserMapper rcsSysUserMapper;
    @Autowired
    DistanceSwitchServer distanceSwitchServerImpl;
    @Autowired
    private OnSaleCommonServer onSaleCommonServer;

    private static String MATCH_EVENT_REDIS_KEY = "rcs:match:event:play:collection:%s";

    @Override
    public void modifyTemplate(TournamentTemplateUpdateParam param) {
        templateMapper.updateTemplateById(param);
        if (Objects.nonNull(param.getSportId()) && param.getSportId() == 1) {
            //根据等级模板玩法集id,或者赛事玩法集接拒单配置
            this.updateContactConfig(param);
        }
    }

    @Override
    public void modifyTemplateEvent(List<RcsTournamentTemplateEvent> param) {
        for (RcsTournamentTemplateEvent templateEvent : param) {
            templateEventMapper.updateTemplateEventById(templateEvent);
        }
    }

    @Override
    public void modifyPlayMargain(List<TournamentTemplatePlayMargainParam> param) {
        RcsTournamentTemplate rcsTournamentTemplate = templateMapper.selectById(param.get(0).getTemplateId());

        log.info("::{}::kir-1255-玩法-对应赛事模板数据为:{}", CommonUtil.getRequestId(rcsTournamentTemplate.getTypeVal()), JSONObject.toJSONString(rcsTournamentTemplate));
        log.info("::{}::kir-1255-玩法-对应赛事模板玩法数据为:{}", CommonUtil.getRequestId(rcsTournamentTemplate.getTypeVal()), JSONObject.toJSONString(param));
        for (TournamentTemplatePlayMargainParam playMargain : param) {
            playMargainMapper.updatePlayMargainById(playMargain);

            //1467需求代码-设置redis值
            log.info("::{}::kir-1467-修改模板:{}", CommonUtil.getRequestId(rcsTournamentTemplate.getTypeVal()), playMargain.getPlayId());
            setRedisForSpecialOddsInterVal(playMargain, rcsTournamentTemplate.getTypeVal(), playMargain.getMatchType());

            //1547需求代码-设置redis值
            log.info("::{}::kir-1547-修改模板:{}", CommonUtil.getRequestId(rcsTournamentTemplate.getTypeVal()), playMargain.getPlayId());
            setRedisForIfWarnSuspended(playMargain, rcsTournamentTemplate.getTypeVal(), playMargain.getMatchType());

            //kir-1255需求 赔率接拒变动范围
            if (!ObjectUtils.isEmpty(rcsTournamentTemplate.getTypeVal())) {
                if (!ObjectUtils.isEmpty(playMargain.getOddsChangeStatus())) {
                    //开关打开才需录入缓存
                    if (playMargain.getOddsChangeStatus().equals(1)) {
                        log.info("::{}::kir-1255-玩法-开始录入缓存:{},{}", CommonUtil.getRequestId(rcsTournamentTemplate.getTypeVal()), playMargain.getPlayId(), playMargain.getMatchType());
                        String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                        redisUtils.set(String.format(redisKey, rcsTournamentTemplate.getTypeVal(), playMargain.getPlayId(), playMargain.getMatchType()), String.valueOf(playMargain.getOddsChangeValue()));
                        log.info("::{}::kir-1255-玩法-录入缓存结束:{},{},{}", CommonUtil.getRequestId(rcsTournamentTemplate.getTypeVal()), playMargain.getPlayId(), playMargain.getMatchType());
                    } else {
                        log.info("::{}::kir-1255-玩法-开始删除缓存:{},{}", CommonUtil.getRequestId(rcsTournamentTemplate.getTypeVal()), playMargain.getPlayId(), playMargain.getMatchType());
                        String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                        redisUtils.del(String.format(redisKey, rcsTournamentTemplate.getTypeVal(), playMargain.getPlayId(), playMargain.getMatchType()));
                        log.info("::{}::kir-1255-玩法-删除缓存成功:{},{}", CommonUtil.getRequestId(rcsTournamentTemplate.getTypeVal()), playMargain.getPlayId(), playMargain.getMatchType());
                    }
                }
            }
        }
        //kir-1368
        //List<RcsTournamentTemplatePlayMargain> margains = JsonFormatUtils.fromJsonArray(JSONObject.toJSONString(param), RcsTournamentTemplatePlayMargain.class);
        //updateMarketConfig(Long.valueOf(rcsTournamentTemplate.getSportId()), rcsTournamentTemplate.getTypeVal(), TradeLevelEnum.CASH_OUT.getLevel(), null, margains);
    }

    /**
     * 1547需求代码-设置redis值
     *
     * @param playMargin
     * @param standardMatchId
     * @param matchType
     */
    void setRedisForIfWarnSuspended(RcsTournamentTemplatePlayMargain playMargin, Long standardMatchId, Integer matchType) {
        if (!ObjectUtils.isEmpty(playMargin.getIfWarnSuspended())) {
            String redisKey = RedisKey.Config.getChuZhangSwitchKey(standardMatchId, matchType);
            log.info("::{}::kir-1547-玩法开关-开始录入缓存:{},{},{}", CommonUtil.getRequestId(standardMatchId), playMargin.getPlayId(), matchType, playMargin.getIfWarnSuspended());
            redisUtils.hset(redisKey, String.valueOf(playMargin.getPlayId()), String.valueOf(playMargin.getIfWarnSuspended()));
            log.info("::{}::kir-1547-玩法开关-录入缓存结束:{},{},{}", CommonUtil.getRequestId(standardMatchId), playMargin.getPlayId(), matchType, playMargin.getIfWarnSuspended());
        }
    }

    /**
     * 1467需求代码-设置redis值
     *
     * @param playMargin
     * @param standardMatchId
     * @param matchType
     */
    void setRedisForSpecialOddsInterVal(RcsTournamentTemplatePlayMargain playMargin, Long standardMatchId, Integer matchType) {
        if (!ObjectUtils.isEmpty(playMargin.getIsSpecialPumping())) {
            log.info("::{}::kir-1467-设置赛事:{},玩法:{},的特殊抽水缓存 总开关:{},特殊抽水赔率区间:{},高赔:{},低赔:{},区间开关:{}", CommonUtil.getRequestId(standardMatchId),
                    playMargin.getPlayId(), playMargin.getIsSpecialPumping(), playMargin.getSpecialOddsInterval(),
                    playMargin.getSpecialOddsIntervalHigh(), playMargin.getSpecialOddsIntervalLow(), playMargin.getSpecialOddsIntervalStatus());
            //总开关 1.开 0.关
            String pumpingKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:status";
            if (playMargin.getIsSpecialPumping().equals(1)) {
                //总开关:1为开
                redisUtils.set(String.format(pumpingKey, standardMatchId, matchType, playMargin.getPlayId()), "1");
                log.info("::{}::kir-1467-设置总开关缓存设置完毕:{},{},{},值为:{}", CommonUtil.getRequestId(standardMatchId), standardMatchId, matchType, playMargin.getPlayId(), "1");

                //区间开关
                if (!ObjectUtils.isEmpty(playMargin.getSpecialOddsIntervalStatus())) {
                    Map map = JSON.parseObject(playMargin.getSpecialOddsIntervalStatus(), Map.class);
                    log.info("::{}::kir-1467-区间开关值为:{}", CommonUtil.getRequestId(standardMatchId), JSONObject.toJSONString(map));
                    map.forEach((k, v) -> {
                        //区间开关 1.开 0.关
                        String statusKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:between:%s:status";
                        redisUtils.set(String.format(statusKey, standardMatchId, matchType, playMargin.getPlayId(), k), String.valueOf(v));
                        log.info("::{}::kir-1467-区间开关设置完毕:{},{}", CommonUtil.getRequestId(standardMatchId), String.format(statusKey, standardMatchId, matchType, playMargin.getPlayId(), k), v);
                    });
                }

                //高赔
                if (!ObjectUtils.isEmpty(playMargin.getSpecialOddsIntervalHigh())) {
                    Map map = JSON.parseObject(playMargin.getSpecialOddsIntervalHigh(), Map.class);
                    log.info("::{}::kir-1467-高赔区间值为:{}", CommonUtil.getRequestId(standardMatchId), JSONObject.toJSONString(map));
                    map.forEach((k, v) -> {
                        //高赔 type标识高低赔率 1高 0低
                        String highKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:type:%s:between:%s";
                        redisUtils.set(String.format(highKey, standardMatchId, matchType, playMargin.getPlayId(), 1, k), String.valueOf(v));
                        log.info("::{}::kir-1467-高赔区间值设置完毕:{},{}", CommonUtil.getRequestId(standardMatchId), String.format(highKey, standardMatchId, matchType, playMargin.getPlayId(), 1, k), v);
                    });
                }

                //高赔：保底投注限额
                if (!ObjectUtils.isEmpty(playMargin.getSpecialBettingIntervalHigh())) {
                    Map map = JSON.parseObject(playMargin.getSpecialBettingIntervalHigh(), Map.class);
                    log.info("::{}::dev-1888-高赔保底投注区间值为:{}", CommonUtil.getRequestId(standardMatchId), JSONObject.toJSONString(map));
                    map.forEach((k, v) -> {
                        //高赔 type标识高低赔率 1高 0低
                        String highKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:type:%s:between:%s:betting";
                        redisUtils.set(String.format(highKey, standardMatchId, matchType, playMargin.getPlayId(), 1, k), String.valueOf(v));
                        log.info("::{}::dev-1888-高赔投注区间值设置完毕:{},{}", CommonUtil.getRequestId(playMargin.getTimeVal()), String.format(highKey, standardMatchId, matchType, playMargin.getPlayId(), 1, k), v);
                    });
                }

                //低赔
                if (!ObjectUtils.isEmpty(playMargin.getSpecialOddsIntervalLow())) {
                    Map map = JSON.parseObject(playMargin.getSpecialOddsIntervalLow(), Map.class);
                    log.info("::{}::kir-1467-低赔区间值为:{}", CommonUtil.getRequestId(standardMatchId), JSONObject.toJSONString(map));
                    map.forEach((k, v) -> {
                        //低赔 type标识高低赔率 1高 0低
                        String lowKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:type:%s:between:%s";
                        redisUtils.set(String.format(lowKey, standardMatchId, matchType, playMargin.getPlayId(), 0, k), String.valueOf(v));
                        log.info("::{}::kir-1467-低赔区间值设置完毕:{},{}", CommonUtil.getRequestId(standardMatchId), String.format(lowKey, standardMatchId, matchType, playMargin.getPlayId(), 0, k), v);
                    });
                }
            } else {
                //总开关:0为关
                redisUtils.set(String.format(pumpingKey, standardMatchId, matchType, playMargin.getPlayId()), "0");
                log.info("::{}::kir-1467-设置总开关缓存设置完毕:{},{},{},值为:{}", CommonUtil.getRequestId(standardMatchId), matchType, playMargin.getPlayId(), "0");
            }
        }
    }

    @Override
    public void modifyMargainRef(TournamentTemplatePlayMargainRefParam param, Integer type) throws Exception {
        QueryWrapper<RcsTournamentTemplatePlayMargainRef> playMargainRef = new QueryWrapper<>();
        playMargainRef.lambda().eq(RcsTournamentTemplatePlayMargainRef::getMargainId, param.getMargainId())
                .eq(RcsTournamentTemplatePlayMargainRef::getTimeVal, param.getTimeVal());
        RcsTournamentTemplatePlayMargainRef playMarginRefList = playMargainRefMapper.selectOne(playMargainRef);
        //不属于特殊玩法的，无法赋值
        if (param.getOrderSingleBetVal() != null) {
            RcsTournamentTemplatePlayMargain playMargain = playMargainMapper.selectOne(new QueryWrapper<RcsTournamentTemplatePlayMargain>().lambda().eq(RcsTournamentTemplatePlayMargain::getId, param.getMargainId()));
            if (!SPECIAL_PLAY_ID.contains(playMargain.getPlayId())) {
                throw new Exception("不是特定玩法，getOrderSingleBetVal赋值,无法增改");
            }
        }
        //设置赛事模板当前分时节点状态为3，定时任务扫描及时生效处理
        if (type == NumberUtils.INTEGER_TWO) {
            param.setStatus(3);
        } else {
            param.setStatus(1);
        }
        if (!ObjectUtils.isEmpty(playMarginRefList)) {
            playMargainRefMapper.updatePlayMargainRefById(param);
        } else {
            playMarginRefList = BeanCopyUtils.copyProperties(param, RcsTournamentTemplatePlayMargainRef.class);
            playMargainRefMapper.insertBatch(Arrays.asList(playMarginRefList));
        }

        PeningOrderCacheClearVo peningOrderCacheClearVo = new PeningOrderCacheClearVo();
        peningOrderCacheClearVo.setMarginRef("marginRef");
        peningOrderCacheClearVo.setMatchId(String.valueOf(param.getMatchId()));
        peningOrderCacheClearVo.setSportId(param.getSportId());
        peningOrderCacheClearVo.setPlayId(param.getPlayId());
        //通知缓存清理
        producerSendMessageUtils.sendMessage("PENDING_ORDER_DELETECACHE", peningOrderCacheClearVo);
        log.info("::{}::PENDING_ORDER_DELETECACHE缓存通知:{}", CommonUtil.getRequestId(param.getMatchId()), JSONObject.toJSONString(peningOrderCacheClearVo));
    }


    @Override
    public void modifyAcceptConfig(List<RcsTournamentTemplateAcceptConfig> param) {
        for (RcsTournamentTemplateAcceptConfig acceptConfig : param) {
            rcsMatchEventTypeInfoService.updateEventAndTimeConfig(acceptConfig);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMarginRef(TournamentTemplatePlayMargainRefParam param) {
        RcsTournamentTemplatePlayMargain playMargin = playMargainMapper.selectById(param.getMargainId());
        QueryWrapper<RcsTournamentTemplatePlayMargainRef> playMarginRefWrapper = new QueryWrapper<>();
        playMarginRefWrapper.lambda().eq(RcsTournamentTemplatePlayMargainRef::getMargainId, param.getMargainId())
                .eq(RcsTournamentTemplatePlayMargainRef::getTimeVal, param.getTimeVal());
        RcsTournamentTemplatePlayMargainRef playMarginRef = playMargainRefMapper.selectOne(playMarginRefWrapper);
        if (playMargin != null && playMarginRef != null) {
            if (playMargin.getValidMarginId() != null) {
                //如果有效分时marginId和要删除得marginId一致，就更新margin为失效状态
                if (playMargin.getValidMarginId().equals(playMarginRef.getId())) {
                    playMarginRef.setStatus(2);
                    playMargainRefMapper.updatePlayMargainRefById(playMarginRef);
                } else {
                    playMargainRefMapper.deleteById(playMarginRef.getId());
                }
            } else {
                playMargainRefMapper.deleteById(playMarginRef.getId());
            }
        }
    }

    /**
     * @param param:
     * @Description: 赛事模板，同步联赛模板数据
     * @Author carver
     * @Date 2020/10/27 17:52
     * @return: void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyMatchTempByLevelTemp(TournamentTemplateUpdateParam param) {
        RcsTournamentTemplate rcsTournamentTemplate = templateMapper.selectById(param.getCopyTemplateId());

        //kir-1467 用于查询赛事ID
        RcsTournamentTemplate templateInfo = templateMapper.selectById(param.getId());
        if (rcsTournamentTemplate != null) {
            Long levelTemplateId = rcsTournamentTemplate.getId();
            log.info("::{}::更新赛事模板同步联赛设置数据-等级模板数据:{}", CommonUtil.getRequestId(param.getMatchId()), JsonFormatUtils.toJson(rcsTournamentTemplate));
            // 修改模板数据
            rcsTournamentTemplate.setId(param.getId());
            rcsTournamentTemplate.setCopyTemplateId(param.getCopyTemplateId());
            rcsTournamentTemplate.setTemplateName(param.getTemplateName());
            //注：此处设置为空，防止联赛权重优先级覆盖赛事优先级配置
            rcsTournamentTemplate.setDataSourceCode(null);
            //bug-34363:同步联赛模版时，AO赛事模版参数不作同步，用原来的老参数
            rcsTournamentTemplate.setAoConfigValue(templateInfo.getAoConfigValue());
            templateMapper.updateTemplateById(rcsTournamentTemplate);

            //kir-1453-百家赔缓存修改
            if (!ObjectUtils.isEmpty(rcsTournamentTemplate) && !ObjectUtils.isEmpty(rcsTournamentTemplate.getTypeVal()) && !ObjectUtils.isEmpty(rcsTournamentTemplate.getMatchType())
                    && !ObjectUtils.isEmpty(param.getId()) && !ObjectUtils.isEmpty(rcsTournamentTemplate.getCautionValue()) && !ObjectUtils.isEmpty(rcsTournamentTemplate.getBaijiaConfigValue())) {
                RcsTournamentTemplateBaijiaConfigParam baijiaConfigParam = new RcsTournamentTemplateBaijiaConfigParam();
                baijiaConfigParam.setMatchId(templateInfo.getTypeVal());
                baijiaConfigParam.setMatchType(Long.valueOf(rcsTournamentTemplate.getMatchType()));
                baijiaConfigParam.setTemplateId(param.getId());
                baijiaConfigParam.setSportId(rcsTournamentTemplate.getSportId());
                baijiaConfigParam.setCautionValue(rcsTournamentTemplate.getCautionValue());
                baijiaConfigParam.setBaijiaConfigs(JsonFormatUtils.fromJsonArray(rcsTournamentTemplate.getBaijiaConfigValue(), RcsTournamentTemplateBaijiaConfigParam.BaijiaConfig.class));
                setBaijiaConfig(baijiaConfigParam);
                //1631同步模板发送AO数据给业务
                if (!ObjectUtils.isEmpty(rcsTournamentTemplate) && StringUtils.isNotBlank(rcsTournamentTemplate.getAoConfigValue())
                        && aoDataSourceInit.checkIfAoSport(rcsTournamentTemplate.getSportId())) {
                    aoDataSourceInit.sendAoDataSourceMessage(rcsTournamentTemplate, baijiaConfigParam.getMatchId());
                }
            }

            //kir-1547需求 是否出涨自动封盘开关（0.关 1.开）
            String redisKeyForIfWarnSuspended = RedisKey.Config.getChuZhangSwitchKey(templateInfo.getTypeVal(), templateInfo.getMatchType());
            log.info("::{}::kir-1547-同步联赛模板-开始录入缓存:{},{}", CommonUtil.getRequestId(templateInfo.getTypeVal()), templateInfo.getMatchType(), templateInfo.getIfWarnSuspended());
            redisUtils.hset(redisKeyForIfWarnSuspended, String.valueOf(templateInfo.getTypeVal()), String.valueOf(templateInfo.getIfWarnSuspended()));
            log.info("::{}::kir-1547-同步联赛模板-录入缓存结束:{},{}", CommonUtil.getRequestId(templateInfo.getTypeVal()), templateInfo.getMatchType(), templateInfo.getIfWarnSuspended());

            if (templateInfo.getSportId() == 1) {
                //koala-1682需求
                String mtsConfigKey = "rcs:redis:mts:contact:config:matchId:%s:matchType:%s";
                redisUtils.set(String.format(mtsConfigKey, templateInfo.getTypeVal(), templateInfo.getMatchType()), templateInfo.getMtsConfigValue());
                redisUtils.expire(String.format(mtsConfigKey, templateInfo.getTypeVal(), templateInfo.getMatchType()), 10, TimeUnit.HOURS);
            }

            // 获取等级模板玩法数据
            List<Integer> closePlayIds = Lists.newArrayList();
            Map<String, Object> levelPlayMarginMap = Maps.newHashMap();
            levelPlayMarginMap.put("template_id", levelTemplateId);
            List<RcsTournamentTemplatePlayMargain> levelPlayMarginList = playMargainMapper.selectByMap(levelPlayMarginMap);
            if (!CollectionUtils.isEmpty(levelPlayMarginList)) {
                Map<Integer, List<RcsTournamentTemplatePlayMargain>> levelPlayMap = levelPlayMarginList.stream().collect(Collectors.groupingBy(RcsTournamentTemplatePlayMargain::getPlayId, LinkedHashMap::new, Collectors.toList()));
                // 获取赛事模板玩法数据
                Map<String, Object> matchPlayMarginMap = Maps.newHashMap();
                matchPlayMarginMap.put("template_id", param.getId());
                List<RcsTournamentTemplatePlayMargain> matchPlayMarginList = playMargainMapper.selectByMap(matchPlayMarginMap);
                if (!CollectionUtils.isEmpty(matchPlayMarginList)) {
                    //获取玩法4全场让分模板的id;
                    RcsTournamentTemplatePlayMargain has = matchPlayMarginList.stream().filter(data -> data.getPlayId().equals(4)).findFirst().orElse(null);
                    Map<Long, Long> orderSingleBetValMap = new HashMap<>();
                    if (has != null) {
                        Map<String, Object> playMarginRefMap = Maps.newHashMap();
                        playMarginRefMap.put("margain_id", has.getId());
                        List<RcsTournamentTemplatePlayMargainRef> playMarginRefList = playMargainRefMapper.selectByMap(playMarginRefMap);
                        //获取开售和开赛下单注投注/赔付限额的值
                        orderSingleBetValMap = playMarginRefList.stream().filter(data -> Arrays.asList(0L, 2592000L).contains(data.getTimeVal()))
                                .collect(Collectors.toMap(RcsTournamentTemplatePlayMargainRef::getTimeVal, RcsTournamentTemplatePlayMargainRef::getOrderSinglePayVal));
                    }

                    //bug-1383
                    Map<Long, Long> finalOrderSingleBetValMap1 = orderSingleBetValMap;
                    //老逻辑，过滤
                    List<RcsTournamentTemplatePlayMargain> matchPlayMarginListnew = new ArrayList<>();
                    for (RcsTournamentTemplatePlayMargain matchPlayMargin : matchPlayMarginList) {
                        if (levelPlayMap.containsKey(matchPlayMargin.getPlayId())) {
                            matchPlayMarginListnew.add(matchPlayMargin);
                        }
                    }
                    //多线程处理入库
                    List<CompletableFuture<String>> executeFutures =
                            matchPlayMarginListnew.parallelStream().map(matchPlayMargin -> CompletableFuture.supplyAsync(() -> {
                                Long id = matchPlayMargin.getId();
                                //用于保存当前赛事模板的开售状态，下面copyProperties之后需要重新赋值
                                Integer isSell = matchPlayMargin.getIsSell();
                                RcsTournamentTemplatePlayMargain levelPlayMargin = levelPlayMap.get(matchPlayMargin.getPlayId()).get(0);
                                BeanCopyUtils.copyProperties(levelPlayMargin, matchPlayMargin);
                                matchPlayMargin.setId(id);
                                matchPlayMargin.setIsSell(isSell);
                                //篮球L模式优化，获取联赛模板关闭的玩法，操盘关盘处理
                                if (matchPlayMargin.getIsSell().equals(NumberUtils.INTEGER_ZERO) && matchPlayMargin.getPlayId() != null) {
                                    closePlayIds.add(matchPlayMargin.getPlayId());
                                }

                                final String linkId = CommonUtils.getLinkIdByMdc() + "_" + matchPlayMargin.getPlayId();
                                //插入需要耗时20多秒，所以这边使用异步多线程
                                Map<Long, Long> finalOrderSingleBetValMap = finalOrderSingleBetValMap1;
                                CommonUtils.mdcPut(linkId);
                                try {
                                    if (levelPlayMap.containsKey(matchPlayMargin.getPlayId())) {
                                        BeanCopyUtils.copyProperties(levelPlayMargin, matchPlayMargin);
                                        matchPlayMargin.setId(id);
                                        matchPlayMargin.setIsSell(isSell);
                                        //篮球L模式优化，获取联赛模板关闭的玩法，操盘关盘处理
                                        if (matchPlayMargin.getIsSell().equals(NumberUtils.INTEGER_ZERO)) {
                                            closePlayIds.add(matchPlayMargin.getPlayId());
                                        }
                                        log.info("::{}::更新赛事模板同步联赛设置数据-玩法模板数据:{},{}", CommonUtil.getRequestId(matchPlayMargin.getId()), matchPlayMargin.getPlayId(), JsonFormatUtils.toJson(matchPlayMargin));
                                        playMargainMapper.updatePlayMargainById(matchPlayMargin);

                                        //1467需求代码-设置redis值
                                        log.info("::{}::kir-1467-同步联赛模板:{}",CommonUtil.getRequestId(matchPlayMargin.getId()), matchPlayMargin.getPlayId());
                                        setRedisForSpecialOddsInterVal(matchPlayMargin, templateInfo.getTypeVal(), templateInfo.getMatchType());

                                        //kir-1255需求 赔率接拒变动范围
                                        log.info("::{}::kir-1255-同步联赛模板-对应联赛模板数据为:{}",CommonUtil.getRequestId(matchPlayMargin.getId()), JSONObject.toJSONString(matchPlayMargin));
                                        //开关打开才需录入缓存
                                        if (!ObjectUtils.isEmpty(matchPlayMargin.getOddsChangeStatus())) {
                                            if (matchPlayMargin.getOddsChangeStatus().equals(1)) {
                                                log.info("::{}::kir-1255-同步联赛模板-开始录入缓存:{},{}",CommonUtil.getRequestId(templateInfo.getTypeVal()), matchPlayMargin.getPlayId(), matchPlayMargin.getMatchType());
                                                String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                                                redisUtils.set(String.format(redisKey, templateInfo.getTypeVal(), matchPlayMargin.getPlayId(), matchPlayMargin.getMatchType()), String.valueOf(matchPlayMargin.getOddsChangeValue()));
                                                log.info("::{}::kir-1255-同步联赛模板-录入缓存结束:{},{}",CommonUtil.getRequestId(templateInfo.getTypeVal()), matchPlayMargin.getPlayId(), matchPlayMargin.getMatchType());
                                            } else {
                                                log.info("::{}::kir-1255-同步联赛模板-开始删除缓存:{},{}",CommonUtil.getRequestId(templateInfo.getTypeVal()), templateInfo.getTypeVal(), matchPlayMargin.getPlayId(), matchPlayMargin.getMatchType());
                                                String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                                                redisUtils.del(String.format(redisKey, templateInfo.getTypeVal(), matchPlayMargin.getPlayId(), matchPlayMargin.getMatchType()));
                                                log.info("::{}::kir-1255-同步联赛模板-删除缓存成功:{},{}",CommonUtil.getRequestId(templateInfo.getTypeVal()), matchPlayMargin.getPlayId(), matchPlayMargin.getMatchType());
                                            }
                                        }

                                        //1547需求代码-设置redis值
                                        log.info("::{}::kir-1547-同步联赛模板:{}",CommonUtil.getRequestId(templateInfo.getTypeVal()), matchPlayMargin.getPlayId());
                                        setRedisForIfWarnSuspended(matchPlayMargin, templateInfo.getTypeVal(), templateInfo.getMatchType());

                                        //删除赛事模板margin数据
                                        Map<String, Object> deleteMatchMarginRef = Maps.newHashMap();
                                        deleteMatchMarginRef.put("margain_id", id);
                                        playMargainRefMapper.deleteByMap(deleteMatchMarginRef);
                                        //获取等级模板margin数据
                                        QueryWrapper<RcsTournamentTemplatePlayMargainRef> levelPlayMarginRefWrapper = new QueryWrapper<>();
                                        levelPlayMarginRefWrapper.lambda().in(RcsTournamentTemplatePlayMargainRef::getMargainId, levelPlayMargin.getId());
                                        List<RcsTournamentTemplatePlayMargainRef> levelPlayMarginRefList = playMargainRefMapper.selectList(levelPlayMarginRefWrapper);
                                        if (!CollectionUtils.isEmpty(levelPlayMarginRefList)) {
                                            if (SPECIAL_PLAY_ID.contains(matchPlayMargin.getPlayId())) {
                                                specialPlaySetOrderSingleBetVal(matchPlayMargin.getPlayId(), finalOrderSingleBetValMap, levelPlayMarginRefList);
                                            }
                                            for (RcsTournamentTemplatePlayMargainRef ref : levelPlayMarginRefList) {
                                                //将此赛事有效分时margin的状态更新为3,定时任务同步刷新配置
                                                ref.setId(null);
                                                ref.setMargainId(id);
                                                ref.setStatus(3);
                                            }
                                            playMargainRefMapper.insertBatch(levelPlayMarginRefList);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("同步联赛模板多线程报错:" + e);
                                } finally {
                                    CommonUtils.mdcRemove();
                                }
                                return "";
                            })).collect(Collectors.toList());
                    executeFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
                }
            }

            if (MatchTypeEnum.LIVE.getId().equals(rcsTournamentTemplate.getMatchType())) {
                // 修改滚球结算审核事件
                Map<String, Object> levelEventMap = Maps.newHashMap();
                levelEventMap.put("template_id", levelTemplateId);
                List<RcsTournamentTemplateEvent> levelEventList = templateEventMapper.selectByMap(levelEventMap);
                if (!CollectionUtils.isEmpty(levelEventList)) {
                    Map<String, List<RcsTournamentTemplateEvent>> eventMap = levelEventList.stream().collect(Collectors.groupingBy(RcsTournamentTemplateEvent::getEventCode, LinkedHashMap::new, Collectors.toList()));
                    Map<String, Object> matchEventMap = Maps.newHashMap();
                    matchEventMap.put("template_id", param.getId());
                    List<RcsTournamentTemplateEvent> matchEventList = templateEventMapper.selectByMap(matchEventMap);
                    if (!CollectionUtils.isEmpty(matchEventList)) {
                        log.info("::{}::更新赛事模板同步联赛设置数据-结算审核模板数据:", CommonUtil.getRequestId(param.getMatchId()), JsonFormatUtils.toJson(matchEventList));
                        for (RcsTournamentTemplateEvent matchEvent : matchEventList) {
                            if (eventMap.containsKey(matchEvent.getEventCode())) {
                                Long id = matchEvent.getId();
                                RcsTournamentTemplateEvent levelEvent = eventMap.get(matchEvent.getEventCode()).get(0);
                                BeanCopyUtils.copyProperties(levelEvent, matchEvent);
                                matchEvent.setId(id);
                                templateEventMapper.updateTemplateEventById(matchEvent);
                            }
                        }
                    }
                }
                // 修改滚球接拒单事件数据
                liveTemplateAccept(rcsTournamentTemplate.getTypeVal(), levelTemplateId, param.getId());

                //kir-1788-同步联赛模板
                Map<String, Object> autoChangeMap = Maps.newHashMap();
                autoChangeMap.put("template_id", param.getId());
                List<RcsTournamentTemplateAcceptConfigAutoChange> autoChangeList = templateAcceptConfigAutoChangeMapper.selectByMap(autoChangeMap);
                if (CollectionUtils.isEmpty(autoChangeList)) {
                    //如果表里为空则插入
                    Map<String, Object> levelAutoChangeMap = Maps.newHashMap();
                    levelAutoChangeMap.put("template_id", param.getCopyTemplateId());
                    List<RcsTournamentTemplateAcceptConfigAutoChange> levelAutoChangeList = templateAcceptConfigAutoChangeMapper.selectByMap(levelAutoChangeMap);
                    if (!CollectionUtils.isEmpty(levelAutoChangeList)) {
                        //根据联赛等级模板的数据重新插入一次
                        for (RcsTournamentTemplateAcceptConfigAutoChange rcsTournamentTemplateAcceptConfigAutoChange : levelAutoChangeList) {
                            rcsTournamentTemplateAcceptConfigAutoChange.setId(null);
                            rcsTournamentTemplateAcceptConfigAutoChange.setTemplateId(param.getId());
                            templateAcceptConfigAutoChangeMapper.insert(rcsTournamentTemplateAcceptConfigAutoChange);

                            //赛事模板自动接拒开关（0.关 1.开）
                            String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getRcsTournamentTemplateAcceptAutoChangeKey(templateInfo.getTypeVal(), rcsTournamentTemplateAcceptConfigAutoChange.getCategorySetId());
                            log.info("::{}::kir-1788-同步联赛模板-开始录入缓存:{}", CommonUtil.getRequestId(templateInfo.getTypeVal()), rcsTournamentTemplateAcceptConfigAutoChange.getCategorySetId());
                            redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, String.valueOf(rcsTournamentTemplateAcceptConfigAutoChange.getIsOpen()), 3600 * 24L);
                        }
                    }
                }
            }

            //推送同步后的模板数据,发送mq给下游消费
            RcsTournamentTemplate template = templateMapper.selectById(param.getId());
            if (template != null) {
                TournamentTemplateUpdateParam tournamentTemplateParam = BeanCopyUtils.copyProperties(template, TournamentTemplateUpdateParam.class);
                //获取事件数据
                Map<String, Object> templateEventMap = Maps.newHashMap();
                templateEventMap.put("template_id", param.getId());
                List<RcsTournamentTemplateEvent> eventList = templateEventMapper.selectByMap(templateEventMap);
                log.info("::{}::更新赛事模板同步联赛设置数据-同步后结算审核模板数据:{}", CommonUtil.getRequestId(template.getTypeVal()), JsonFormatUtils.toJson(eventList));
                if (!CollectionUtils.isEmpty(eventList)) {
                    tournamentTemplateParam.setTemplateEventList(eventList);
                    // 发送联赛模板赛事数据
                    tournamentTemplatePushService.putTournamentTemplateMatchEventData(tournamentTemplateParam);
                }
                //获取玩法数据
                Map<String, Object> matchPlayMarginMap = Maps.newHashMap();
                matchPlayMarginMap.put("template_id", param.getId());
                List<RcsTournamentTemplatePlayMargain> playList = playMargainMapper.selectByMap(matchPlayMarginMap);
                log.info("::{}::更新赛事模板同步联赛设置数据-同步后玩法模板数据:{}", CommonUtil.getRequestId(param.getMatchId()), JsonFormatUtils.toJson(playList));
                if (!CollectionUtils.isEmpty(playList)) {
                    List<TournamentTemplatePlayMargainParam> marginParamList = BeanCopyUtils.copyPropertiesList(playList, TournamentTemplatePlayMargainParam.class);
                    tournamentTemplateParam.setPlayMargainList(marginParamList);
                    // 发送联赛模板赛玩法数据
                    tournamentTemplatePushService.putTournamentTemplatePlayData(tournamentTemplateParam);
                }
                //联赛模板未启用的玩法，操盘关盘处理
                log.info("::{}::更新赛事模板同步联赛设置数据-模板未启用的玩法:{}", CommonUtil.getRequestId(template.getTypeVal()), JsonFormatUtils.toJson(closePlayIds));
                if (!CollectionUtils.isEmpty(closePlayIds)) {
                    JSONObject json = new JSONObject()
                            .fluentPut("tradeLevel", TradeLevelEnum.BATCH_PLAY.getLevel())
                            .fluentPut("matchId", template.getTypeVal())
                            .fluentPut("playIdList", closePlayIds)
                            .fluentPut("status", TradeStatusEnum.CLOSE.getStatus())
                            .fluentPut("linkedType", LinkedTypeEnum.TEMPLATE_CHANGE.getCode())
                            .fluentPut("matchType", template.getMatchType())
                            .fluentPut("sourceCloseFlag", YesNoEnum.Y.getValue());
                    Request<JSONObject> r = new Request<>();
                    String linkId = CommonUtils.getLinkId() + "_temp_sync_playSeal";
                    r.setLinkId(linkId);
                    r.setData(json);
                    log.info("::{}::更新赛事模板同步联赛设置数据-模板未启用的玩法:matchId={},Message:{}", linkId, template.getTypeVal(), JSONObject.toJSON(r));
                    producerSendMessageUtils.sendMessage(MqConstant.Topic.RCS_TRADE_UPDATE_MARKET_STATUS, linkId, String.valueOf(template.getTypeVal()), r);
                }

                //kir-开售赛事时也需要同步最新的（赛事级别的提前结算开关）状态给融合
                RcsTournamentTemplate forMatchId = templateMapper.selectById(param.getId());
                TradeMarketUiConfigDTO dto1 = onSaleCommonServer.getCommonClass(forMatchId);
                //kir-1368-同步赛事级别提前结算给enzo
                producerSendMessageUtils.sendMessage("RCS_TOUR_TEMPLATE_CASHOUT_TOPIC", "matchs", dto1.getStandardMatchInfoId() + "_" + forMatchId.getMatchType(), dto1.getConfigCashOutTradeItemDTO());
                if (forMatchId.getSportId().equals(1)) {
                    DataRealtimeApiUtils.handleApi(dto1, new DataRealtimeApiUtils.ApiCall() {
                        @Override
                        public <R> Response<R> callApi(Request request) {
                            return tradeMarketConfigApi.putTradeMarketUiConfig(request);
                        }
                    });
                    //1852发送开关状态给融合
                    distanceSwitchServerImpl.sendDistanceSwitch(forMatchId);
                }
            }
        } else {
            throw new RcsServiceException("未找到等级模板数据，请联系风控处理！");
        }
    }

    /**
     * @Description: 复制接拒单事件配置
     * @Author carver
     * @Date 2021/1/15 15:25
     * @return: void
     **/
    @Override
    public void liveTemplateAccept(Long matchId, Long levelTemplateId, Long templateId) {
        Map<String, Object> levelConfigMap = Maps.newHashMap();
        levelConfigMap.put("template_id", levelTemplateId);
        List<RcsTournamentTemplateAcceptConfig> levelConfigList = rcsTournamentTemplateAcceptConfigMapper.selectByMap(levelConfigMap);
        if (!CollectionUtils.isEmpty(levelConfigList)) {
            for (RcsTournamentTemplateAcceptConfig levelConfig : levelConfigList) {
                //获取等级模板接拒单事件配置数据
                Map<String, Object> levelAcceptEventMap = Maps.newHashMap();
                levelAcceptEventMap.put("accept_config_id", levelConfig.getId());
                List<RcsTournamentTemplateAcceptEvent> levelAcceptEventList = rcsTournamentTemplateAcceptEventMapper.selectByMap(levelAcceptEventMap);

                //根据等级模板玩法集id,或者赛事玩法集接拒单配置
                QueryWrapper<RcsTournamentTemplateAcceptConfig> matchConfigWrapper = new QueryWrapper<>();
                matchConfigWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getTemplateId, templateId)
                        .eq(RcsTournamentTemplateAcceptConfig::getCategorySetId, levelConfig.getCategorySetId());
                RcsTournamentTemplateAcceptConfig matchConfig = rcsTournamentTemplateAcceptConfigMapper.selectOne(matchConfigWrapper);
                if (!ObjectUtils.isEmpty(matchConfig)) {
                    if (!CollectionUtils.isEmpty(levelAcceptEventList)) {
                        Map<String, List<RcsTournamentTemplateAcceptEvent>> levelAcceptEventTypeMap = levelAcceptEventList.stream().collect(Collectors.groupingBy(RcsTournamentTemplateAcceptEvent::getEventType, LinkedHashMap::new, Collectors.toList()));
                        for (Map.Entry<String, List<RcsTournamentTemplateAcceptEvent>> entry : levelAcceptEventTypeMap.entrySet()) {
                            String eventType = entry.getKey();
                            List<RcsTournamentTemplateAcceptEvent> levelAcceptEventListByType = entry.getValue();

                            Map<String, Object> matchAcceptEventMap = Maps.newHashMap();
                            matchAcceptEventMap.put("accept_config_id", matchConfig.getId());
                            matchAcceptEventMap.put("event_type", eventType);
                            matchAcceptEventMap.put("status", 1);
                            List<RcsTournamentTemplateAcceptEvent> matchAcceptEventList = rcsTournamentTemplateAcceptEventMapper.selectByMap(matchAcceptEventMap);
                            if (CollectionUtils.isEmpty(matchAcceptEventList)) {
                                QueryWrapper<RcsTournamentTemplateAcceptEvent> matchAcceptEventWrapper = new QueryWrapper<>();
                                matchAcceptEventWrapper.lambda().eq(RcsTournamentTemplateAcceptEvent::getAcceptConfigId, matchConfig.getId())
                                        .eq(RcsTournamentTemplateAcceptEvent::getStatus, 1);
                                List<RcsTournamentTemplateAcceptEvent> acceptEventList = rcsTournamentTemplateAcceptEventMapper.selectList(matchAcceptEventWrapper);
                                List<String> eventCodes = acceptEventList.stream().map(RcsTournamentTemplateAcceptEvent::getEventCode).collect(Collectors.toList());
                                for (RcsTournamentTemplateAcceptEvent levelAcceptEvent : levelAcceptEventListByType) {
                                    if (!eventCodes.contains(levelAcceptEvent.getEventCode())) {
                                        levelAcceptEvent.setAcceptConfigId(matchConfig.getId());
                                        log.info("::{}::更新赛事模板同步联赛设置数据-接拒单模板数据:{},{}", CommonUtil.getRequestId(matchConfig.getMatchId()), matchConfig.getId(), JsonFormatUtils.toJson(levelAcceptEvent));
                                        rcsTournamentTemplateAcceptEventMapper.updateMatchEventConfig(Arrays.asList(levelAcceptEvent));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    //获取当前赛事主事件源
                    RcsStandardSportMarketSell marketSell = rcsStandardSportMarketSellService.selectStandardMarketSellVo(matchId);
                    //新增赛事玩法集
                    RcsTournamentTemplateAcceptConfig matchTemplateConfig = new RcsTournamentTemplateAcceptConfig();
                    matchTemplateConfig.setTemplateId(templateId);
                    matchTemplateConfig.setCategorySetId(levelConfig.getCategorySetId());
                    if (ObjectUtil.isNotNull(marketSell)) {
                        matchTemplateConfig.setDataSource(marketSell.getBusinessEvent());
                    } else {
                        matchTemplateConfig.setDataSource("SR");
                    }
                    matchTemplateConfig.setNormal(levelConfig.getNormal());
                    matchTemplateConfig.setMinWait(levelConfig.getMinWait());
                    matchTemplateConfig.setMaxWait(levelConfig.getMaxWait());
                    matchTemplateConfig.setCreateTime(new Date());
                    log.info("::{}::更新赛事模板同步联赛设置数据-插入接拒单模板数据:{}====={}", CommonUtil.getRequestId(matchId), templateId, JsonFormatUtils.toJson(matchTemplateConfig));
                    rcsTournamentTemplateAcceptConfigMapper.insert(matchTemplateConfig);
                    if (!CollectionUtils.isEmpty(levelAcceptEventList)) {
                        for (RcsTournamentTemplateAcceptEvent levelAcceptEvent : levelAcceptEventList) {
                            levelAcceptEvent.setAcceptConfigId(matchTemplateConfig.getId());
                        }
                        rcsTournamentTemplateAcceptEventMapper.updateMatchEventConfig(levelAcceptEventList);
                    }
                }
            }
        }
    }

    /**
     * @Description: 根据1级联赛接拒单配置，刷新其他联赛等级下的接拒单配置
     * @Author carver
     * @Date 2020/11/20 15:25
     * @return: void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTemplateByOneLevel() {
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper<>();
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, 1)
                .eq(RcsTournamentTemplate::getType, 1)
                .eq(RcsTournamentTemplate::getTypeVal, 1)
                .eq(RcsTournamentTemplate::getMatchType, 0);
        RcsTournamentTemplate rcsTournamentTemplate = templateMapper.selectOne(templateQueryWrapper);
        if (!ObjectUtils.isEmpty(rcsTournamentTemplate)) {
            Map<String, Object> templateConfigMap = Maps.newHashMap();
            templateConfigMap.put("template_id", rcsTournamentTemplate.getId());
            List<RcsTournamentTemplateAcceptConfig> oneTemplateConfigList = rcsTournamentTemplateAcceptConfigMapper.selectByMap(templateConfigMap);
            if (!CollectionUtils.isEmpty(oneTemplateConfigList)) {
                for (RcsTournamentTemplateAcceptConfig config : oneTemplateConfigList) {
                    Map<String, Object> templateAcceptEventMap = Maps.newHashMap();
                    templateAcceptEventMap.put("accept_config_id", config.getId());
                    List<RcsTournamentTemplateAcceptEvent> oneAcceptEventList = rcsTournamentTemplateAcceptEventMapper.selectByMap(templateAcceptEventMap);

                    //处理其他联赛等级模板接拒单参数
                    QueryWrapper<RcsTournamentTemplate> otherTemplateQueryWrapper = new QueryWrapper<>();
                    otherTemplateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, 1)
                            .eq(RcsTournamentTemplate::getType, 1)
                            .ne(RcsTournamentTemplate::getTypeVal, 1)
                            .eq(RcsTournamentTemplate::getMatchType, 0);
                    List<RcsTournamentTemplate> otherTemplateList = templateMapper.selectList(otherTemplateQueryWrapper);
                    if (!CollectionUtils.isEmpty(otherTemplateList)) {
                        for (RcsTournamentTemplate template : otherTemplateList) {
                            QueryWrapper<RcsTournamentTemplateAcceptConfig> otherConfigQueryWrapper = new QueryWrapper<>();
                            otherConfigQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getTemplateId, template.getId())
                                    .eq(RcsTournamentTemplateAcceptConfig::getCategorySetId, config.getCategorySetId());
                            RcsTournamentTemplateAcceptConfig otherTemplateConfig = rcsTournamentTemplateAcceptConfigMapper.selectOne(otherConfigQueryWrapper);
                            if (!CollectionUtils.isEmpty(oneAcceptEventList)) {
                                if (!ObjectUtils.isEmpty(otherTemplateConfig)) {
                                    otherTemplateConfig.setDataSource(config.getDataSource());
                                    otherTemplateConfig.setNormal(config.getNormal());
                                    otherTemplateConfig.setMinWait(config.getMinWait());
                                    otherTemplateConfig.setMaxWait(config.getMaxWait());
                                    rcsTournamentTemplateAcceptConfigMapper.updateMatchDataSourceAndTimeConfig(otherTemplateConfig);
                                    for (RcsTournamentTemplateAcceptEvent event : oneAcceptEventList) {
                                        event.setAcceptConfigId(otherTemplateConfig.getId());
                                    }
                                    rcsTournamentTemplateAcceptEventMapper.updateMatchEventConfig(oneAcceptEventList);
                                } else {
                                    //新增玩法集
                                    otherTemplateConfig = new RcsTournamentTemplateAcceptConfig();
                                    otherTemplateConfig.setTemplateId(template.getId());
                                    otherTemplateConfig.setCategorySetId(config.getCategorySetId());
                                    otherTemplateConfig.setDataSource("SR");
                                    otherTemplateConfig.setNormal(3);
                                    otherTemplateConfig.setMinWait(10);
                                    otherTemplateConfig.setMaxWait(90);
                                    otherTemplateConfig.setCreateTime(new Date());
                                    rcsTournamentTemplateAcceptConfigMapper.insert(otherTemplateConfig);
                                    if (!CollectionUtils.isEmpty(oneAcceptEventList)) {
                                        for (RcsTournamentTemplateAcceptEvent levelAcceptEvent : oneAcceptEventList) {
                                            levelAcceptEvent.setAcceptConfigId(otherTemplateConfig.getId());
                                        }
                                        rcsTournamentTemplateAcceptEventMapper.updateMatchEventConfig(oneAcceptEventList);
                                    }
                                }
                            }

                        }
                    }
                }
            } else {
                throw new RcsServiceException("未找到一级联赛，玩法集接拒单配置");
            }
        }
    }

    /**
     * @Description: 根据赛事所在联赛, 获取所使用的联赛模板和所有等级模板
     * @Author carver
     * @Date 2020/12/15 15:25
     * @return: void
     **/
    @Override
    public List<TournamentLevelTemplateVo> findLevelTempByMatchId(TournamentTemplateUpdateParam param, String lang) {
        //返回等级级和专用模板
        List<TournamentLevelTemplateVo> template = Lists.newArrayList();
        //根据赛种获取等级模板
        QueryWrapper<RcsTournamentTemplate> tournamentTemplateQueryWrapper = new QueryWrapper<>();
        tournamentTemplateQueryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.LEVEL.getId())
                .eq(RcsTournamentTemplate::getSportId, param.getSportId())
                .eq(RcsTournamentTemplate::getMatchType, param.getMatchType());
        List<RcsTournamentTemplate> tempList = templateMapper.selectList(tournamentTemplateQueryWrapper);
        if (!CollectionUtils.isEmpty(tempList)) {
            for (RcsTournamentTemplate obj : tempList) {
                TournamentLevelTemplateVo vo = new TournamentLevelTemplateVo();
                vo.setTemplateId(obj.getId());

                //国际化
                if (lang.equals("en")) {
                    if (param.getMatchType().equals(MatchTypeEnum.EARLY.getId())) {
                        vo.setTournamentName(StringUtils.isNotBlank(NumberConventer.GetEN(obj.getTypeVal().intValue())) ? NumberConventer.GetEN(obj.getTypeVal().intValue()) : "无");
                    } else if (param.getMatchType().equals(MatchTypeEnum.LIVE.getId())) {
                        vo.setTournamentName(StringUtils.isNotBlank(NumberConventer.GetEN(obj.getTypeVal().intValue())) ? NumberConventer.GetEN(obj.getTypeVal().intValue()) : "无");
                    }
                } else {
                    if (param.getMatchType().equals(MatchTypeEnum.EARLY.getId())) {
                        vo.setTournamentName(StringUtils.isNotBlank(NumberConventer.GetCH(obj.getTypeVal().intValue())) ? NumberConventer.GetCH(obj.getTypeVal().intValue()) + "级联赛早盘模板" : "无");
                    } else if (param.getMatchType().equals(MatchTypeEnum.LIVE.getId())) {
                        vo.setTournamentName(StringUtils.isNotBlank(NumberConventer.GetCH(obj.getTypeVal().intValue())) ? NumberConventer.GetCH(obj.getTypeVal().intValue()) + "级联赛滚球模板" : "无");
                    }
                }

                vo.setTypeVal(obj.getTypeVal());
                vo.setType(obj.getType());
                template.add(vo);
            }
        }

        //根据赛种获取专用模板
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId())
                .eq(RcsTournamentTemplate::getType, TempTypeEnum.TOUR.getId())
                .eq(RcsTournamentTemplate::getMatchType, param.getMatchType())
                .orderByAsc(RcsTournamentTemplate::getTemplateName);
        List<RcsTournamentTemplate> specialList = templateMapper.selectList(templateQueryWrapper);
        if (!CollectionUtils.isEmpty(specialList)) {
            for (RcsTournamentTemplate obj : specialList) {
                TournamentLevelTemplateVo vo = new TournamentLevelTemplateVo();
                vo.setTemplateId(obj.getId());
                vo.setTournamentName(obj.getTemplateName());
                vo.setTypeVal(obj.getTypeVal());
                vo.setType(obj.getType());
                template.add(vo);
            }
        }
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyBaiJiaConfig(RcsTournamentTemplateBaijiaConfigParam param) {
        //kir-1453-百家赔修改
        if (!ObjectUtils.isEmpty(param.getTemplateId()) && !ObjectUtils.isEmpty(param.getCautionValue()) && !ObjectUtils.isEmpty(param.getBaijiaConfigs())) {
            RcsTournamentTemplate template = new RcsTournamentTemplate();
            template.setId(param.getTemplateId());
            template.setCautionValue(param.getCautionValue());
            template.setBaijiaConfigValue(JSONObject.toJSONString(param.getBaijiaConfigs()));
            template.setSportId(param.getSportId());
            templateMapper.updateById(template);
            setBaijiaConfig(param);
        }
    }

    private void setBaijiaConfig(RcsTournamentTemplateBaijiaConfigParam param) {
        log.info("::{}::kir-1453-百家赔缓存修改入参:{}", CommonUtil.getRequestId(param.getMatchId()), JSONObject.toJSONString(param));
        String key = "rcs:tournament:template:baijia:config:matchId:%s:matchType:%s";
        if (!ObjectUtils.isEmpty(param.getCautionValue()) && !ObjectUtils.isEmpty(param.getBaijiaConfigs())) {
            redisUtils.del(String.format(key, param.getMatchId(), param.getMatchType()));
            redisUtils.hset(String.format(key, param.getMatchId(), param.getMatchType()), "cautionValue", String.valueOf(param.getCautionValue()));
            redisUtils.expire(String.format(key, param.getMatchId(), param.getMatchType()), 60L, TimeUnit.DAYS);
            producerSendMessageUtils.sendMessage("RCS_TOUR_MATCH_TEMPLATE_CONFIG_TOPIC", null, param.getMatchId() + "_" + param.getMatchType() + "_" + param.getSportId(), param.getMatchId() + "_" + param.getMatchType() + "_" + param.getSportId());
            for (RcsTournamentTemplateBaijiaConfigParam.BaijiaConfig baijiaConfig : param.getBaijiaConfigs()) {
                //状态为开则存缓存
                if (baijiaConfig.getStatus().equals(1)) {
                    redisUtils.hset(String.format(key, param.getMatchId(), param.getMatchType()), baijiaConfig.getName(), String.valueOf(baijiaConfig.getValue()));
                    log.info("::{}::kir-1453-百家赔缓存保存成功,key为:{},值为:{}", String.format(key, param.getMatchId(), param.getMatchType()), baijiaConfig.getName(), baijiaConfig.getName());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlayOddsConfig(RcsTournamentTemplatePlayOddsConfigParam param) {
        log.info("::{}::赛事玩法赔率源设置param={}", CommonUtil.getRequestId(param.getMatchId()), JSONObject.toJSONString(param));
        List<Long> categoryIdList = Lists.newArrayList();
        List<RcsMatchMarketConfig> marketConfigList = Lists.newArrayList();
        PlayOddsConfigVo playOddsConfigVo = new PlayOddsConfigVo();
        playOddsConfigVo.setMatchId(param.getMatchId());
        playOddsConfigVo.setMatchType(param.getMatchType().intValue());
        Map<String, List<Long>> map = Maps.newHashMap();
        playOddsConfigVo.setPlayDataSource(map);
        //玩法赔率源设置推送同步给融合
        RcsSysUser rcsSysUser = rcsSysUserMapper.selectById(TradeUserUtils.getUserIdNoException());
        List<UpdateMarketCategoryDataSourceCodeDTO> dtoList = Lists.newArrayList();
        for (RcsTournamentTemplatePlayOddsConfigParam.PlaysOddsConfig config : param.getPlayOddsConfigs()) {
            if (StringUtils.isNotBlank(config.getDataSource()) && !CollectionUtils.isEmpty(config.getPlayIds())) {
                map.put(config.getDataSource(), config.getPlayIds());
                for (Long playId : config.getPlayIds()) {
                    UpdateMarketCategoryDataSourceCodeDTO dto = new UpdateMarketCategoryDataSourceCodeDTO();
                    dto.setMatchId(param.getMatchId());
                    dto.setMarketType(String.valueOf(param.getMatchType()));
                    dto.setDataSourceCode(config.getDataSource());
                    dto.setMarketCategoryId(playId);
                    dto.setSellStatus("Sold");
                    dto.setUserName(rcsSysUser.getUserCode());
                    dtoList.add(dto);
                    //设置玩法和赛事id
                    categoryIdList.add(playId);
                    RcsMatchMarketConfig rcsMatchMarketConfig = new RcsMatchMarketConfig();
                    rcsMatchMarketConfig.setMatchId(param.getMatchId());
                    rcsMatchMarketConfig.setPlayId(playId);
                    marketConfigList.add(rcsMatchMarketConfig);
                }
            }
        }
        Response<Object> response = DataRealtimeApiUtils.handleApi(dtoList, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                Response rs = tradeMarketConfigApi.updateMarketCategoryDataSourceCode(request);
                return rs;
            }
        });
        //应融合要求 等结果返回成功后在进行封盘操作 否则在并发情况下导致bug
        if (response.isSuccess()) {
            //玩法封盘
            if (!CollectionUtils.isEmpty(categoryIdList)) {
                StandardMatchInfo info = standardMatchInfoMapper.selectById(param.getMatchId());
                oddsSourceSwitchAutoAndSeal(info, categoryIdList);

                //玩法清理水差，盘口差
                ArrayList<ClearSubDTO> objects = new ArrayList<>();
                for (Long plid : categoryIdList) {
                    ClearSubDTO clearSubDTO = new ClearSubDTO();
                    clearSubDTO.setPlayId(plid);
                    clearSubDTO.setMatchId(info.getId());
                    objects.add(clearSubDTO);
                }
                ClearDTO clearDTO = new ClearDTO();
                clearDTO.setType(0);
                clearDTO.setClearType(8);
                clearDTO.setMatchId(info.getId());
                clearDTO.setBeginTime(info.getBeginTime());
                clearDTO.setList(objects);
                producerSendMessageUtils.sendMessage("RCS_CLEAR_MATCH_MARKET_TAG", null, UuidUtils.generateUuid(), clearDTO);
            }
            //参数不为空的时候，更新玩法赔率源
            QueryWrapper<RcsTournamentTemplate> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId())
                    .eq(RcsTournamentTemplate::getTypeVal, param.getMatchId())
                    .eq(RcsTournamentTemplate::getMatchType, param.getMatchType());
            RcsTournamentTemplate template = rcsTournamentTemplateService.getOne(queryWrapper);
            if (ObjectUtil.isNotNull(template)) {
                //修改赛事模板玩法赔率源
                param.setTemplateId(template.getId());
                rcsTournamentTemplateService.updatePlayOddsConfig(param);
            }

            //数据源更改，发送mq推送至前端
            Request r = new Request();
            String linkId = UUID.randomUUID().toString().replace("-", "") + "_play_odds_config";
            r.setLinkId(linkId);
            r.setData(playOddsConfigVo);
            producerSendMessageUtils.sendMessage("RCS_CATEGORY_ODDS_CONFIG_TOPIC", linkId, String.valueOf(param.getMatchId()), r);

            if (ObjectUtil.isNotNull(template)) {
                log.info("::{}::-盘口修改数据源，更新模板分时节点状态为待生效，matchId:{},marketConfigList:{}",linkId,param.getMatchId(),JSONObject.toJSONString(marketConfigList));
                for(RcsMatchMarketConfig rcsMatchMarketConfig:marketConfigList){
                    QueryWrapper<RcsTournamentTemplatePlayMargain> qwMargain = new QueryWrapper<>();
                    qwMargain.lambda().eq(RcsTournamentTemplatePlayMargain::getTemplateId, template.getId())
                            .eq(RcsTournamentTemplatePlayMargain::getPlayId, rcsMatchMarketConfig.getPlayId())
                            .eq(RcsTournamentTemplatePlayMargain::getMatchType, param.getMatchType());
                    RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargain = playMargainMapper.selectOne(qwMargain);
                    log.info("::{}::-盘口修改数据源，更新模板分时节点状态为待生效，matchId:{},margain:{}",linkId,param.getMatchId(),JSONObject.toJSONString(rcsTournamentTemplatePlayMargain));
                    if (ObjectUtil.isNotNull(rcsTournamentTemplatePlayMargain)) {
                        //篮球切换盘口数据源后，推送最大盘口数给融合
                        if (template.getSportId()==2) {
                            sendBasketballMarketCountToData(template,rcsTournamentTemplatePlayMargain,linkId);
                        }
                        QueryWrapper<RcsTournamentTemplatePlayMargainRef> qwRef = new QueryWrapper<>();
                        qwRef.lambda().eq(RcsTournamentTemplatePlayMargainRef::getMargainId, rcsTournamentTemplatePlayMargain.getId())
                                .eq(RcsTournamentTemplatePlayMargainRef::getStatus,1);//生效状态
                        RcsTournamentTemplatePlayMargainRef rcsTournamentTemplatePlayMargainRef = new RcsTournamentTemplatePlayMargainRef();
                        rcsTournamentTemplatePlayMargainRef.setStatus(3);//设置盘口模板参数分时节点为变更状态
                        int result = playMargainRefMapper.update(rcsTournamentTemplatePlayMargainRef,qwRef);
                        log.info("::{}::-盘口修改数据源，更新模板分时节点状态为待生效，matchId:{},修改结果:{}",linkId,param.getMatchId(),result);
                    }
                }
            }
        }
    }

    /**
     * 推送篮球最大盘口数给融合
     *
     * @param template
     * @param margin
     */
    private void sendBasketballMarketCountToData(RcsTournamentTemplate template, RcsTournamentTemplatePlayMargain margin, String linkId) {
        TournamentTemplatePlayVo playVo = getTournamentTemplatePlayVo(template);
        TournamentTemplateCategoryVo tournamentTemplateCategoryVo = new TournamentTemplateCategoryVo();
        tournamentTemplateCategoryVo.setMarketCount(margin.getMarketCount());
        tournamentTemplateCategoryVo.setMarketNearDiff(margin.getMarketNearDiff());
        tournamentTemplateCategoryVo.setPlayId(margin.getPlayId());
        tournamentTemplateCategoryVo.setMarketNearOddsDiff(margin.getMarketNearOddsDiff());
        //加载赛事参数更新的玩法
        List<TournamentTemplateCategoryVo> categoryList = Lists.newArrayList();
        categoryList.add(tournamentTemplateCategoryVo);
        playVo.setCategoryList(categoryList);
        Request request = new Request();
        request.setData(playVo);
        request.setGlobalId(linkId);
        log.info("::{}::-篮球切换盘口数据源后，推送最大盘口数给融合，matchId:{},margin:{}", linkId, playVo.getStandardMatchId(), JSONObject.toJSONString(margin));
        producerSendMessageUtils.sendMessage("Tournament_Template_Play", linkId, String.valueOf(playVo.getStandardMatchId()), JSONObject.toJSON(request));
    }

    private TournamentTemplatePlayVo getTournamentTemplatePlayVo(RcsTournamentTemplate param) {
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
        return playVo;
    }

    private void oddsSourceSwitchAutoAndSeal(StandardMatchInfo info, List<Long> playIds) {
        Long sportId = info.getSportId();
        Long matchId = info.getId();
        int matchType = RcsConstant.isLive(info.getMatchStatus()) ? 0 : 1;
        boolean isMts;
        if (RcsConstant.isLive(info.getMatchStatus())) {
            isMts = !"PA".equalsIgnoreCase(info.getLiveRiskManagerCode());
        } else {
            isMts = !"PA".equalsIgnoreCase(info.getPreRiskManagerCode());
        }
        // 玩法切换成自动，并封盘，过滤L模式
        if (SportIdEnum.isBasketball(sportId)) {
            playIds = playIds.stream().filter(playId -> !tradeStatusService.isLinkage(matchId, playId)).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(playIds)) {
            return;
        }
        MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
        updateVO.setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel());
        updateVO.setSportId(sportId);
        updateVO.setMatchId(matchId);
        updateVO.setCategoryIdList(playIds);
        updateVO.setTradeType(TradeEnum.AUTO.getCode());
        // 封盘
        updateVO.setIsSeal(YesNoEnum.Y.getValue());
        updateVO.setLinkedType(LinkedTypeEnum.ODDS_SOURCE.getCode());
        // 不推送赔率
        updateVO.setIsPushOdds(YesNoEnum.N.getValue());
        updateVO.setMatchType(matchType);
        updateVO.setIsMts(isMts);
        String linkId = tradeModeService.updateTradeMode(updateVO);
        log.info("::{}::已开售赛事-赔率源切换-玩法切换成自动并封盘", linkId);
        CommonUtils.sleep(TimeUnit.MILLISECONDS, 500L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOddsMarketMaxValue(TournamentTemplatePlayMargainParam param) {
        if (ObjectUtils.isEmpty(param.getOddsMaxValue()) && ObjectUtils.isEmpty(param.getMarketMaxValue())) {
            throw new IllegalArgumentException("跳水或跳盘最大值不能为空！");
        }
        UpdateWrapper<RcsTournamentTemplatePlayMargain> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("template_id", param.getTemplateId());
        RcsTournamentTemplatePlayMargain playMargin = new RcsTournamentTemplatePlayMargain();
        if (!ObjectUtils.isEmpty(param.getOddsMaxValue())) {
            playMargin.setOddsMaxValue(param.getOddsMaxValue());
        } else if (!ObjectUtils.isEmpty(param.getMarketMaxValue())) {
            //跳盘最大值，只修改以下玩法
            List playIds = Arrays.asList(38, 39, 18, 19, 26, 143, 45, 46, 51, 52, 57, 58, 63, 64);
            updateWrapper.in("play_id", playIds);
            playMargin.setMarketMaxValue(param.getMarketMaxValue());
        }
        playMargainMapper.update(playMargin, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTennisAllPlayValue(TournamentTemplatePlayMargainParam param) {
        playMargainMapper.updatePlayMarginByTemplateId(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyPendingOrderStatus(TournamentTemplateUpdateParam param) {
        templateMapper.updateTemplateById(param);
        RcsTournamentTemplate temp = templateMapper.selectById(param.getId());
        TradeMarketUiConfigDTO dto = onSaleCommonServer.getCommonClass(temp);
        producerSendMessageUtils.sendMessage("RCS_TOUR_TEMPLATE_CASHOUT_TOPIC", "matchs", dto.getStandardMatchInfoId() + "_" + temp.getMatchType(), dto.getConfigCashOutTradeItemDTO());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifySettleSwitch(TournamentTemplateUpdateParam param) {
        templateMapper.updateTemplateById(param);
        RcsTournamentTemplate temp = templateMapper.selectById(param.getId());
        ConfigCashOutTradeItemDTO cashOutTradeItemDTO = new ConfigCashOutTradeItemDTO();
        cashOutTradeItemDTO.setMatchId(temp.getTypeVal());
        cashOutTradeItemDTO.setMatchPreStatus(temp.getMatchPreStatus());
        cashOutTradeItemDTO.setMarketType(temp.getMatchType());
        cashOutTradeItemDTO.setDataSourceCode(CommonUtil.getDataSourceCode(temp.getEarlySettStr()));
        TradeMarketUiConfigDTO dto = onSaleCommonServer.getCommonClass(temp);
        //kir-1368-同步赛事级别级别提前结算给enzo
        producerSendMessageUtils.sendMessage("RCS_TOUR_TEMPLATE_CASHOUT_TOPIC", "matchs", dto.getStandardMatchInfoId() + "_" + temp.getMatchType(), dto.getConfigCashOutTradeItemDTO());
        DataRealtimeApiUtils.handleApi(dto, new DataRealtimeApiUtils.ApiCall() {
            @Override
            public <R> Response<R> callApi(Request request) {
                return tradeMarketConfigApi.putTradeMarketUiConfig(request);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyOddsChangeStatus(TournamentTemplateUpdateParam param) {
        templateMapper.updateTemplateById(param);
        //kir-1255需求 赔率变动接拒开关（0.关 1.开）
        String redisKey = "rcs:risk:order:oddsScope:match.%s.match_type.%s";
        //log.info("::{}::kir-1255-赛事开关-开始录入缓存:{},{}",CommonUtil.getRequestId(), param.getTypeVal(), param.getMatchType());
        redisUtils.set(String.format(redisKey, param.getTypeVal(), param.getMatchType()), String.valueOf(param.getOddsChangeStatus()));
        //log.info("::{}::kir-1255-赛事开关-录入缓存结束:{},{}",CommonUtil.getRequestId(), param.getTypeVal(), param.getMatchType());
        //kir-1368
        //RcsTournamentTemplate template = JsonFormatUtils.fromJson(JSONObject.toJSONString(param), RcsTournamentTemplate.class);
        //updateMarketConfig(Long.valueOf(param.getSportId()), param.getTypeVal(), TradeLevelEnum.MATCH.getLevel(), template, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyWarnSuspended(TournamentTemplateUpdateParam param) {
        templateMapper.updateTemplateById(param);
        //kir-1547需求 是否出涨自动封盘开关（0.关 1.开）
        String redisKey = RedisKey.Config.getChuZhangSwitchKey(param.getTypeVal(), param.getMatchType());
        //log.info("::{}::kir-1547-赛事开关-开始录入缓存:{},{},{}",CommonUtil.getRequestId(), param.getTypeVal(), param.getMatchType(), param.getIfWarnSuspended());
        redisUtils.hset(redisKey, String.valueOf(param.getTypeVal()), String.valueOf(param.getIfWarnSuspended()));
        //log.info("::{}::kir-1547-赛事开关-录入缓存结束:{},{},{}",CommonUtil.getRequestId(), param.getTypeVal(), param.getMatchType(), param.getIfWarnSuspended());
    }

    @Override
    public void modifyMtsSwitchConfig(TournamentTemplateUpdateParam param) {
        int count = templateMapper.updateTemplateById(param);
        if (count > 0) {
            //设置下缓存
            RcsTournamentTemplate temp = templateMapper.selectById(param.getId());
            String key = String.format(RedisKey.REDIS_MTS_CONTACT_CONFIG_KEY, temp.getTypeVal(), temp.getMatchType());
            JSONObject json = new JSONObject();
            json.put("key", key);
            json.put("value", "1");
            producerSendMessageUtils.sendMessage("rcs_order_reject_cache_update", "", key, json);
        }
    }


    @Override
    public void modifyDistanceSwitch(TournamentTemplateUpdateParam param) {
        int count = templateMapper.updateTemplateById(param);
        if (count > 0) {
            RcsTournamentTemplate temp = templateMapper.queryTemplateById(param.getId());
            //根据等级模板玩法集id,或者赛事玩法集接拒单配置
            this.updateContactConfig(param);
            //删除缓存1852
            String key = String.format(MATCH_EVENT_REDIS_KEY, param.getTypeVal());
            redisUtils.del(key);
            //发送开关状态给融合
            distanceSwitchServerImpl.sendDistanceSwitch(temp);
        }


    }

    //一些特殊玩法。单注投注限额赋默认值
    private void specialPlaySetOrderSingleBetVal(Integer playid, Map<Long, Long> orderSingleBetValMap, List<RcsTournamentTemplatePlayMargainRef> playMarginRefList) {
        for (RcsTournamentTemplatePlayMargainRef ref : playMarginRefList) {
            if (Arrays.asList(0L, 2592000L).contains(ref.getTimeVal()) && ref.getOrderSingleBetVal() == null) {
                switch (playid) {
                    case 7:
                    case 20:
                    case 74:
                    case 341:
                    case 342:
                    case 344:
                        ref.setOrderSingleBetVal(orderSingleBetValMap.containsKey(ref.getTimeVal()) ? orderSingleBetValMap.get(ref.getTimeVal()) / 2L : null);
                        break;
                    case 8:
                    case 9:
                    case 14:
                    case 103:
                        ref.setOrderSingleBetVal(orderSingleBetValMap.containsKey(ref.getTimeVal()) ? orderSingleBetValMap.get(ref.getTimeVal()) / 4L : null);
                        break;
                }
            }

        }
    }

    private void updateContactConfig(TournamentTemplateUpdateParam param) {
        try {
            log.info("::操盘手{}::开始修改2.0封关盘操作:{}", TradeUserUtils.getUserId(), JSON.toJSONString(param));
        } catch (Exception e) {
        }
        //根据等级模板玩法集id,或者赛事玩法集接拒单配置
        QueryWrapper<RcsTournamentTemplateAcceptConfig> matchConfigWrapper = new QueryWrapper<>();
        matchConfigWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getTemplateId, param.getId());
        List<RcsTournamentTemplateAcceptConfig> configList = rcsTournamentTemplateAcceptConfigMapper.selectList(matchConfigWrapper);
        if (!configList.isEmpty()) {
            //修改接距发送删除key操作
            configList.forEach(s->{
                String redisKey = String.format(RedisKey.MATCH_EVENT_REDIS_KEY,param.getTypeVal(),s.getCategorySetId());
                this.sendCacheMsg(redisKey,"1");
            });
            Integer categorySetId = configList.get(0).getCategorySetId();
            List<Long> list = configList.stream().map(RcsTournamentTemplateAcceptConfig::getId).collect(Collectors.toList());
            if (Objects.nonNull(param.getDistanceSwitch()) && param.getDistanceSwitch() == 1) {
                //查询下
                QueryWrapper<RcsTournamentTemplateAcceptEvent> queryData = new QueryWrapper<>();
                queryData.lambda().in(RcsTournamentTemplateAcceptEvent::getAcceptConfigId, list).eq(RcsTournamentTemplateAcceptEvent::getEventType, "closing")
                        .eq(RcsTournamentTemplateAcceptEvent::getStatus, 1);
                List<RcsTournamentTemplateAcceptEvent> eventList = rcsTournamentTemplateAcceptEventMapper.selectList(queryData);
                if (!eventList.isEmpty()) {
                    //吧这个ID存入缓存
                    String key = String.format(RedisKey.REDIS_ACCEPT_CONFIG_EVENT_KEY, param.getId(), categorySetId);
                    redisUtils.set(key, JSON.toJSONString(eventList));
                    redisUtils.expire(key, 30, TimeUnit.DAYS);
                    eventList.forEach(rcsTournamentTemplateAcceptEvent -> {
                        rcsTournamentTemplateAcceptEvent.setEventType("danger");
                        rcsTournamentTemplateAcceptEventMapper.updateById(rcsTournamentTemplateAcceptEvent);
                    });
                }
            } else if (Objects.nonNull(param.getDistanceSwitch()) && param.getDistanceSwitch() == 0) {
                String eventKey = String.format(RedisKey.REDIS_ACCEPT_CONFIG_EVENT_KEY, param.getId(), categorySetId);
                String eventValue = redisUtils.get(eventKey);
                if (StringUtils.isNotBlank(eventValue)) {
                    log.info("::{}::playId={},1852需求获取缓存封盘事件:{}", CommonUtil.getRequestId(param.getMatchId()), categorySetId, eventValue);
                    List<RcsTournamentTemplateAcceptEvent> eventList = JSON.parseArray(eventValue, RcsTournamentTemplateAcceptEvent.class);
                    eventList.forEach(rcsTournamentTemplateAcceptEvent -> {
                        rcsTournamentTemplateAcceptEventMapper.updateById(rcsTournamentTemplateAcceptEvent);
                    });
                    redisUtils.del(eventKey);
                }
            }
        }
    }

    /**
     * 发送到接距服务做及时更新配置
     * @param key key
     * @param value val
     */
    private void sendCacheMsg(String key,Object value){
        JSONObject json = new JSONObject();
        json.put("key", key);
        json.put("value", value);
        producerSendMessageUtils.sendMessage("rcs_order_reject_cache_update", "", key, json);
    }



    /**
     * 盘口配置修改 消息
     * @param sportId
     * @param matchId
     * @param level
     * @param template
     * @param playMargainList
     */
//    @Override
//    public void updateMarketConfig(Long sportId, Long matchId, Integer level, RcsTournamentTemplate template, List<RcsTournamentTemplatePlayMargain> playMargainList){
//        if(ObjectUtils.isEmpty(sportId) || ObjectUtils.isEmpty(matchId) || ObjectUtils.isEmpty(level)){
//            log.info("kir-1368-消息发送失败,参数为:{}",sportId+"-"+matchId+"-"+level);
//            return;
//        }
//        //赛事
//        if(!ObjectUtils.isEmpty(template)){
//            MatchMarketTradeTypeVo vo = new MatchMarketTradeTypeVo(sportId, matchId, level, template.getOddsChangeStatus());
//            log.info("kir-1368-赛事级别-盘口配置修改{}", JSONObject.toJSON(vo));
//            String linkId = CommonUtils.getLinkId() + "_MARKET_CONGIG_UPDTAE_TOPIC";
//            producerSendMessageUtils.sendMessage(MqConstant.MARKET_CONGIG_UPDTAE_TOPIC, linkId, String.valueOf(matchId), vo);
//            return;
//        }
//        //玩法
//        if(!ObjectUtils.isEmpty(playMargainList)){
//            List<MatchMarketTradeTypeVo> playCashout = new ArrayList<>();
//            for (RcsTournamentTemplatePlayMargain margain : playMargainList) {
//                MatchMarketTradeTypeVo vo = new MatchMarketTradeTypeVo(Long.valueOf(margain.getPlayId()), margain.getOddsChangeStatus(), margain.getOddsChangeValue());
//                playCashout.add(vo);
//            }
//            MatchMarketTradeTypeVo vo = new MatchMarketTradeTypeVo(sportId, matchId, level, playCashout);
//            log.info("kir-1368-玩法级别-盘口配置修改{}", JSONObject.toJSON(vo));
//            String linkId = CommonUtils.getLinkId() + "_MARKET_CONGIG_UPDTAE_TOPIC";
//            producerSendMessageUtils.sendMessage(MqConstant.MARKET_CONGIG_UPDTAE_TOPIC, linkId, String.valueOf(matchId), vo);
//            return;
//        }
//    }
}