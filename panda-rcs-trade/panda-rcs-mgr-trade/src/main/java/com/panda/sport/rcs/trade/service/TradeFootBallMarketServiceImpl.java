package com.panda.sport.rcs.trade.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.vo.HttpResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.panda.merge.dto.MarketMarginGapDtlDTO;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.TradeMarketMarginGapConfigDTO;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.trade.log.LogContext;
import com.panda.sport.rcs.trade.log.format.LogFormatBean;
import com.panda.sport.rcs.trade.log.format.LogFormatPublicBean;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.RcsTradeConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketOddsMapper;
import com.panda.sport.rcs.mapper.sub.RcsMatchMarketConfigSubMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.pojo.enums.FootBallPlayEnum;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.enums.LogTypeEnum;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.trade.wrapper.impl.MatchTradeConfigServiceImpl;
import com.panda.sport.rcs.trade.wrapper.impl.RcsMatchMarketConfigServiceImpl;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.vo.OddsValueVo;
import com.panda.sport.rcs.vo.UpdateOddsValueVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.ws.rs.HEAD;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author :  Sean
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.service
 * @Description :  操盘服务类
 * @Date: 2020-08-13 14:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class TradeFootBallMarketServiceImpl {
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private TradeMarketSetServiceImpl tradeMarketSetService;
    @Autowired
    RcsTournamentOperateMarketService rcsTournamentOperateMarketService;
    @Autowired
    MatchStatisticsInfoService matchStatisticsInfoService;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Autowired
    private RcsTradeConfigMapper rcsTradeConfigMapper;
    @Autowired
    private TradeCommonService tradeCommonService;
    @Autowired
    private TradeSubPlayCommonService tradeSubPlayCommonService;
    @Autowired
    private TradeVerificationService tradeVerificationService;
    @Autowired
    private RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Autowired
    private RcsMatchMarketConfigSubMapper rcsMatchMarketConfigSubMapper;
    @Autowired
    private MatchTradeConfigServiceImpl matchTradeConfigService;
    @Autowired
    private OddsRangeService oddsRangeService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private StandardSportMarketOddsMapper standardSportMarketOddsMapper;
    @Autowired
    private RcsMatchMarketConfigServiceImpl rcsMatchMarketConfigServiceImpl;

    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
//    @Autowired
//    private StandardSportMarketOddsMapper standardSportMarketOddsMapper;
    @Autowired
    RedisClient redisClient;
    @Autowired
    private TradeBasketBallMarketServiceImpl tradeBasketBallMarketService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;

    /**
     * @Description   //篮球修改赔率
     * @Param [config]
     * @Author  sean
     * @Date   2021/2/4
     * @return void
     * 代码有修改
     **/
    @Transactional
    public String updateMarketOddsOrWater(RcsMatchMarketConfig config) {
        String logDec = "自动水差";
        List<Map<String, Object>> maps = null;
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        config.setDataSource(dataSource.longValue());
        // 设置子玩法id
        tradeSubPlayCommonService.setSubPlayId(config);
//        List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        // 获取赔率
        RcsStandardMarketDTO market = getFootBallMarketOdds(config,playAllMarketList);
        rcsMatchMarketConfigService.getAndSetRcsMatchMarketConfig(config);
        // 这里有margin了
        RcsMatchMarketConfig beforeConfig = JSONObject.parseObject(JSONObject.toJSONString(config),RcsMatchMarketConfig.class);
        // 设置赔率变化
        tradeCommonService.getAndSetOddsChange(config);
        List<StandardMarketOddsDTO> oddsList = JSONArray.parseArray(JSONArray.toJSONString(market.getMarketOddsList()),StandardMarketOddsDTO.class);
        String oddsType = tradeVerificationService.getOddsType(market);
        String msg = null;
        if (MarketUtils.isAuto(dataSource)) {
            RcsMatchMarketMarginConfig marginConfig = rcsMatchMarketConfigService.getRcsMatchMarketMarginConfig(config);
            BigDecimal autoRatio = new BigDecimal(ObjectUtils.isEmpty(marginConfig.getAwayAutoChangeRate()) ? NumberUtils.DOUBLE_ZERO.toString() : marginConfig.getAwayAutoChangeRate());
            // 计算水差
            msg = tradeCommonService.calculateWater(config,autoRatio,config.getOddsChange(),oddsType);
            config.setOddsType(oddsType);
            tradeMarketSetService.sendWaterToDataCenter(config);
            // 保存水差
//            rcsMatchMarketConfigMapper.insertOrUpdateMarketMarginConfig(config);
            tradeOddsCommonService.updateRedisWater(config);
            beforeConfig.setAwayAutoChangeRate(StringUtils.isEmpty(marginConfig.getAwayAutoChangeRate()) ? NumberUtils.DOUBLE_ZERO.toString() : marginConfig.getAwayAutoChangeRate());
            // 水差日志
            LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.TRADE_TYPE.getCode()+"", logDec, String.valueOf(config.getMatchId()));
            Map<String, Object> dynamicBean = new HashMap<String, Object>();
            dynamicBean.put("click_case", "操盘列表手动调整");
            dynamicBean.put("play_id", config.getPlayId());
            dynamicBean.put("obj_id", "盘口：" + market.getAddition1());
            dynamicBean.put("match_type",config.getMatchType());
            beforeConfig.setAwayAutoChangeRate(String.format("%.2f",Double.parseDouble(beforeConfig.getAwayAutoChangeRate())));
            config.setAwayAutoChangeRate(ObjectUtils.isEmpty(config.getAwayAutoChangeRate())?"0.00":String.format("%.2f",Double.parseDouble(config.getAwayAutoChangeRate())));
//            LogContext.getContext().addFormatBean(publicBean, dynamicBean, beforeConfig, config);
            LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("自动水差", beforeConfig.getAwayAutoChangeRate(), config.getAwayAutoChangeRate()));
        }else {
//            logDec = "修改赔率";
            if (!ObjectUtils.isEmpty(config.getIsSpecialPumping()) && config.getIsSpecialPumping() == 1){
                RcsMatchMarketConfig conf = JSONObject.parseObject(JSONObject.toJSONString(config),RcsMatchMarketConfig.class);

                conf.setMargin(oddsRangeService.getSpicalSpread(oddsList,conf));
                // 特殊抽水
                tradeCommonService.caluFootBallOddsBySpread(oddsList,conf);
                oddsRangeService.caluSpecialOddsBySpread(oddsList,conf);
            }else {
                tradeCommonService.caluFootBallOddsBySpread(oddsList,config);
            }

            // 比较大小
            msg = tradeCommonService.getCheckOddsLimitAndUpdateStatus(config,oddsList);

            if (StringUtils.isNotBlank(msg)) {
                return msg;
            }
            market.setMarketOddsList(JSONArray.parseArray(JSONArray.toJSONString(oddsList),StandardMarketOddsDTO.class));
            market.setPlaceNum(config.getMarketIndex());
            if (CollectionUtils.isNotEmpty(playAllMarketList)){
                for (RcsStandardMarketDTO m : playAllMarketList){
                    if (m.getId().equalsIgnoreCase(market.getId())){
                        m = market;
                    }
                }
                List<String> marketIds = playAllMarketList.stream().map(e -> e.getId()).collect(Collectors.toList());
                if (!marketIds.contains(config.getMarketId().toString())){
                    playAllMarketList.add(market);
                }
            }else {
                playAllMarketList =  Lists.newArrayList(market);
            }
            List<StandardMarketDTO> dtos = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList),StandardMarketDTO.class);
            tradeCommonService.putTradeMarketOdds(config, dtos,null);
            List<StandardSportMarketOdds> marketOddsList = JSONArray.parseArray(JSONArray.toJSONString(oddsList),StandardSportMarketOdds.class);
            maps = matchTradeConfigService.getOddsList(config,marketOddsList);
            config.setOddsList(maps);
        }
        balanceService.clearAllBalance(BalanceTypeEnum.JUMP_ODDS.getType(), config.getSportId().longValue(), config.getMatchId(), config.getPlayId(), config.getDateExpect(),config.getSubPlayId());
//        config.setRelevanceType(NumberUtils.INTEGER_ZERO);
//        tradeVerificationService.clearBalance(config,config.getAwayAutoChangeRate(),null,NumberUtils.INTEGER_ONE);
//        LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("水差", beforeConfig.getAwayAutoChangeRate(), config.getAwayAutoChangeRate(),"水差修改：%s"));
        return msg;
    }

    /**
     * @Description   //足球盘口配置
     * @Param [config]
     * @Author  sean
     * @Date   2021/2/7
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    @Transactional(rollbackFor = Exception.class)
    public RcsMatchMarketConfig updateMatchMarketConfig(RcsMatchMarketConfig config) {
        Integer marketIndex = config.getMarketIndex();
        if (!(TradeConstant.FOOTBALL_CAN_CREATE_MY_PLAYS.contains(config.getPlayId().intValue()) ||
                TradeConstant.FOOTBALL_MAIN_EU_PLAYS.contains(config.getPlayId().intValue()) ||
                TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue()))){
            throw new RcsServiceException("不支持的玩法");
        }

        // 这里有margin了
        RcsMatchMarketConfig beforeConfig = rcsMatchMarketConfigService.queryMatchMarketConfigNew(config);
        RcsMatchMarketMarginConfig marginConfig = rcsMatchMarketConfigServiceImpl.getFootballWaterDiff(config);
        if (ObjectUtils.isEmpty(beforeConfig)){
            beforeConfig = new RcsMatchMarketConfig();
        }
        beforeConfig.setAwayAutoChangeRate(marginConfig.getAwayAutoChangeRate());
        // 子玩法id换成融合的子玩法id
        String subPlayId = config.getSubPlayId();
//        if (ObjectUtils.isEmpty(config.getMarketId()) && TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue())){
            config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
//        }
//        List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        RcsTournamentTemplatePlayMargain buildConfig = tradeVerificationService.queryTournamentTemplateConfig(config);
        if (ObjectUtils.isEmpty(config.getMarketId())){
            if (CollectionUtils.isNotEmpty(playAllMarketList) && (!TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue()))){
                throw new RcsServiceException("已存在盘口不可以新增");
            }
            if (TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue())){
                for (RcsStandardMarketDTO marketDTO :playAllMarketList){
                    if (SubPlayUtil.getRongHeSubPlayId(marketDTO).equalsIgnoreCase(config.getSubPlayId())){
                        throw new RcsServiceException("已存在盘口不可以新增");
                    }
                }
            }
//            RcsTournamentTemplatePlayMargain buildConfig = tradeVerificationService.queryTournamentTemplateConfig(config);
            // 切换操盘方式不推送赔率
            tradeSubPlayCommonService.changeTradeType(config, SportIdEnum.FOOTBALL.getId());
            if (Lists.newArrayList(2L, 4L, 18L, 19L, 113L, 114L, 121L, 122L, 127L, 128L, 134L).contains(config.getPlayId())) {
                // 构建构建附加盘标志
                config.setMarketBuildFlag(true);
                config.setBuildConfig(buildConfig);
            }
        }
        //自动手动放进去
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        //是否使用数据源1：手动；0：使用数据源。 没有配置即使用数据源
        config.setDataSource(dataSource.longValue());
        BigDecimal margin = tradeVerificationService.getConfigMargin(config);
        log.info("::{}::,margin = {}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),margin.toPlainString());
        //2：验证各种数据的正确性
        String msg = tradeCommonService.verifyData(config,margin,buildConfig);
        // 清平衡值
        if (tradeVerificationService.isMarketOddsChange(playAllMarketList,config)){
            config.setRelevanceType(NumberUtils.INTEGER_ZERO);
            config.setOddsType(tradeVerificationService.getOddType(config));
            tradeVerificationService.clearBalance(config,"",null,NumberUtils.INTEGER_ONE);
        }

        // 接口合并，发送盘口配置到数据中心
        tradeVerificationService.sendMarketConfigToDataCenter(config,null,margin);

        config.setHomeMarketValue(config.getAwayMarketValue().subtract(config.getHomeMarketValue()).stripTrailingZeros());
        if (dataSource.intValue() != DataSourceTypeEnum.AUTOMATIC.getValue().intValue()) {
            // 超过赔率封盘
            if (ObjectUtils.isEmpty(config.getMarketId())){
                config.setMarketStatus(NumberUtils.INTEGER_ONE);
                // 换成前端子玩法id，里面有参数
                config.setSubPlayId(subPlayId);
            }
//             设置赔率
            RcsStandardMarketDTO market = tradeCommonService.setOddsValue(config,playAllMarketList,margin);

            // 最后换成融合的子玩法id存库
            config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
            // 更新状态
            tradeCommonService.updatePlaceStatus(config.getMatchId(), config.getPlayId(), config.getMarketIndex(), config.getMarketStatus(), config.getSubPlayId());
            //设置描点
            tradeCommonService.setDefaultAnchor(market);
            // 组装玩法盘口数据
            List<StandardMarketDTO> marketList = tradeCommonService.packageMarketList(config, playAllMarketList, market);
            // 推送赔率
            tradeCommonService.putTradeMarketOdds(config, marketList,null);
        }
        RcsMatchMarketConfig conf = JSONObject.parseObject(JSONObject.toJSONString(config),RcsMatchMarketConfig.class);
        if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType()) && config.getOddsList().size() == 3){
            conf.setAwayAutoChangeRate(null);
        }else {
            tradeOddsCommonService.updateRedisWater(config);
//            rcsMatchMarketConfigMapper.insertOrUpdateMarketMarginConfig(config);
        }
        //保存配置
        tradeOddsCommonService.updateMarketConfig(conf);
//        if (TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue())){
//            rcsMatchMarketConfigSubMapper.insertOrUpdateMarketConfig(conf);
//        }else {
//            rcsMatchMarketConfigMapper.insertOrUpdateMarketConfig(conf);
//        }
        rcsTournamentOperateMarketService.sendRcsDataMq(null, config.getPlayId() + "", marketIndex + "", config.getMatchId().toString(),config.getSubPlayId(),config.getMaxSingleBetAmount());
        if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
            // 水差日志
            LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.TRADE_TYPE.getCode()+"", "弹窗赔率修改", String.valueOf(config.getMatchId()));
            Map<String, Object> dynamicBean = new HashMap<String, Object>();
            dynamicBean.put("click_case", "调价弹窗手动调整");
            dynamicBean.put("play_id", config.getPlayId());
            dynamicBean.put("match_type",config.getMatchType());
            beforeConfig.setAwayAutoChangeRate(String.format("%.2f",Double.parseDouble(beforeConfig.getAwayAutoChangeRate())));
            config.setAwayAutoChangeRate(ObjectUtils.isEmpty(config.getAwayAutoChangeRate())?"0.00":String.format("%.2f",Double.parseDouble(config.getAwayAutoChangeRate())));
            dynamicBean.put("obj_id", "盘口：" + (ObjectUtils.isEmpty(config.getMarketId())?"新增":config.getHomeMarketValue()));
//            LogContext.getContext().addFormatBean(publicBean, dynamicBean, beforeConfig, config);
            LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("自动水差", beforeConfig.getAwayAutoChangeRate(), config.getAwayAutoChangeRate()));

            dynamicBean.put("obj_id", "坑位：" + config.getMarketIndex());
            beforeConfig.setMinOdds(ObjectUtils.isEmpty(beforeConfig.getMinOdds())? BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_DOWN): beforeConfig.getMinOdds().setScale(2, BigDecimal.ROUND_DOWN));
            beforeConfig.setMaxOdds(ObjectUtils.isEmpty(beforeConfig.getMaxOdds())? BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_DOWN): beforeConfig.getMaxOdds().setScale(2, BigDecimal.ROUND_DOWN));
            config.setMinOdds(config.getMinOdds().setScale(2, BigDecimal.ROUND_DOWN));
            config.setMaxOdds(config.getMaxOdds().setScale(2, BigDecimal.ROUND_DOWN));
            LogContext.getContext().addFormatBean(publicBean, dynamicBean, beforeConfig, config);
        }
        // 清理跳分次数
        if (!ObjectUtils.isEmpty(config.getMarketId()) && TradeConstant.RCS_COUNT_TIMES_PLAY.contains(config.getPlayId().intValue())
                && !ObjectUtils.isEmpty(beforeConfig.getOddChangeRule())
                && beforeConfig.getOddChangeRule().intValue() != config.getOddChangeRule()){
            redisClient.hashRemove(String.format(TradeConstant.RCS_COUNT_TIMES,config.getMatchId()),config.getMarketId().toString());
        }
        return config;
    }
    /**
     * @Description   //获取当前盘口
     * @Param [config]
     * @Author  sean
     * @Date   2021/3/7
     * @return com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO
     **/
    private RcsStandardMarketDTO getFootBallMarketOdds(RcsMatchMarketConfig config,List<RcsStandardMarketDTO> playAllMarketList) {
        RcsStandardMarketDTO market = null;
        if (CollectionUtils.isNotEmpty(playAllMarketList)){
            for (RcsStandardMarketDTO m : playAllMarketList){
                if (m.getId().equalsIgnoreCase(config.getMarketId().toString())){
                    market = m;
                }
            }
        }
        if (ObjectUtils.isEmpty(market) || CollectionUtils.isEmpty(market.getMarketOddsList())) {
            log.error("::{}::该盘口没数据{}", CommonUtil.getRequestId(),config.getMarketId());
            throw new RcsServiceException("该盘口没数据");
        }
        return market;
    }
    /**
     * @Description   //更新margin玩法水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/5/2
     * @return java.lang.String
     **/
    public String updateMarketWater(RcsMatchMarketConfig config,Integer sportId) {
//        if (!(TradeConstant.FOOTBALL_MAIN_EU_PLAYS.contains(config.getPlayId().intValue()) ||
//                TradeConstant.BASKETBALL_MAIN_EU_PLAYS.contains(config.getPlayId().intValue()))){
//            throw new RcsServiceException("不支持的玩法");
//        }
        // 两项盘逻辑
//        QueryWrapper<StandardSportMarketOdds> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(StandardSportMarketOdds::getMarketId,config.getMarketId());
//        List<StandardSportMarketOdds> list = standardSportMarketOddsMapper.selectList(queryWrapper);
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        RcsStandardMarketDTO market = null;
        for (RcsStandardMarketDTO m:playAllMarketList){
            if (m.getId().equalsIgnoreCase(config.getMarketId().toString())){
                market = m;
                break;
            }
        }
        if (ObjectUtils.isEmpty(market)){
            throw new RcsServiceException("盘口id错误");
        }
        if (CollectionUtils.isNotEmpty(market.getMarketOddsList()) && market.getMarketOddsList().size() ==2){
            return tradeBasketBallMarketService.updateEUMarketOddsOrWater(config);
        }
        if (ObjectUtils.isEmpty(config.getOddsList())){
            throw new RcsServiceException("水差参数为空");
        }
        // 设置子玩法id
        tradeSubPlayCommonService.setSubPlayId(config);
        TradeMarketMarginGapConfigDTO gapConfigDTO = new TradeMarketMarginGapConfigDTO();
        gapConfigDTO.setStandardCategoryId(config.getPlayId());
        gapConfigDTO.setChildStandardCategoryId(Long.parseLong(config.getSubPlayId()));
        gapConfigDTO.setStandardMatchInfoId(config.getMatchId());
        gapConfigDTO.setPlaceNum(config.getMarketIndex());
        //如果有盘口值 需要设置盘口值  取实时值
//        List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);

        if (CollectionUtils.isEmpty(playAllMarketList) || CollectionUtils.isEmpty(playAllMarketList.get(NumberUtils.INTEGER_ZERO).getMarketOddsList())) {
            log.error("::{}::,该盘口没数据:config={},playAllMarketList={}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), JSONObject.toJSONString(config),JSONObject.toJSONString(playAllMarketList));
            throw new RcsServiceException("该盘口没数据");
        }
        //  组装参数调融合接口
        rcsMatchMarketConfigService.getAndSetRcsMatchMarketConfig(config);
        Boolean isOddsChange = Boolean.FALSE;
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        config.setDataSource(dataSource.longValue());
        List<MarketMarginGapDtlDTO> marginGapDtlDTOList = Lists.newArrayList();
        getAndSetMargin(config);

        RcsMatchMarketConfig rcsMatchMarketConfig = rcsMatchMarketConfigService.getMaxAndMinOddsValue(config.getMatchId(), config.getPlayId());
        for (Map<String,Object> map : config.getOddsList()){
            BigDecimal fieldOddsValue = new BigDecimal(map.get("fieldOddsValue").toString());
            if (fieldOddsValue.compareTo(new BigDecimal("1.01")) < 0 && (!MarketUtils.isAuto(dataSource))){
                throw new RcsServiceException("赔率不能小于1.01");
            }
            if (!MarketUtils.isAuto(dataSource) && Objects.nonNull(rcsMatchMarketConfig)){
                if(fieldOddsValue.compareTo(rcsMatchMarketConfig.getMinOdds()) < 0){
                    throw new RcsServiceException(fieldOddsValue+"赔率修改低于模板最小赔率"+rcsMatchMarketConfig.getMinOdds());
                }
                if(fieldOddsValue.compareTo(rcsMatchMarketConfig.getMaxOdds()) > 0){
                    throw new RcsServiceException(fieldOddsValue+"赔率修改超出模板最大赔率"+rcsMatchMarketConfig.getMaxOdds());
                }
            }
            MarketMarginGapDtlDTO dtlDTO = JSONObject.parseObject(JSONObject.toJSONString(map),MarketMarginGapDtlDTO.class);
            dtlDTO.setOddsType(map.get("oddsType").toString());
            dtlDTO.setMargin(config.getMargin().doubleValue());
            map.put("marketDiffValue",ObjectUtils.isEmpty(map.get("marketDiffValue")) ? NumberUtils.INTEGER_ZERO : map.get("marketDiffValue"));
            dtlDTO.setDiffValue(ObjectUtils.isEmpty(map.get("marketDiffValue"))?null:new BigDecimal(map.get("marketDiffValue").toString()).divide(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE),2,BigDecimal.ROUND_DOWN).doubleValue());
            dtlDTO.setAnchor(ObjectUtils.isEmpty(map.get("anchor")) ? null : Integer.parseInt(map.get("anchor").toString()));
            marginGapDtlDTOList.add(dtlDTO);
            isOddsChange = setOddsByOddsType(market,map,config);
        }
        // 自动默认不均分，手工默认均分
        gapConfigDTO.setLinkageMode(config.getLinkageMode());
        if (ObjectUtils.isEmpty(gapConfigDTO.getLinkageMode())){
            if (MarketUtils.isAuto(dataSource)){
                gapConfigDTO.setLinkageMode(NumberUtils.INTEGER_ONE);
            }else {
                gapConfigDTO.setLinkageMode(NumberUtils.INTEGER_ZERO);
            }
        }
        tradeCommonService.setDefaultAnchor(marginGapDtlDTOList);
        gapConfigDTO.setList(marginGapDtlDTOList);
        gapConfigDTO.setChildStandardCategoryId(Long.parseLong(config.getSubPlayId()));
        if (MarketUtils.isAuto(dataSource)){
            // 配置推送
            tradeCommonService.putTradeMarginGap(gapConfigDTO);
        }else {
            // 赔率推送
            tradeCommonService.setDefaultAnchor(market);
            tradeCommonService.calculationOddsByMargin(market,config.getMargin());
            for (RcsStandardMarketDTO m:playAllMarketList){
                if (m.getId().equalsIgnoreCase(config.getMarketId().toString())){
                    m = market;
                    break;
                }
            }
            List<StandardMarketDTO> dtos = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList),StandardMarketDTO.class);
            tradeCommonService.putTradeMarketOdds(config, dtos,null);
        }
        if (isOddsChange){
            balanceService.clearAllBalance(BalanceTypeEnum.JUMP_ODDS.getType(), config.getSportId().longValue(), config.getMatchId(), config.getPlayId(), config.getDateExpect(),config.getSubPlayId());
//            config.setRelevanceType(NumberUtils.INTEGER_ZERO);
//            config.setOddsType(config.getOddsList().get(NumberUtils.INTEGER_ZERO).get("oddsType").toString());
//            tradeVerificationService.clearBalance(config,config.getAwayAutoChangeRate(),null,sportId);
        }
        return null;
    }

    private void getAndSetMargin(RcsMatchMarketConfig config) {
        if (ObjectUtils.isEmpty(config.getMargin())){
            BigDecimal margin = ObjectUtils.isEmpty(config.getOddsList().get(0).get("margin")) ? BigDecimal.valueOf(110) : new BigDecimal(config.getOddsList().get(0).get("margin").toString());
            config.setMargin(margin);
        }
    }

    private Boolean setOddsByOddsType(RcsStandardMarketDTO market, Map<String, Object> map,RcsMatchMarketConfig config) {
        Boolean isOddsChange = Boolean.FALSE;
        for (StandardMarketOddsDTO odds : market.getMarketOddsList()){
            BigDecimal oddsValue = ObjectUtils.isEmpty(odds.getOddsValue()) ? BigDecimal.ZERO : new BigDecimal(odds.getOddsValue());
            BigDecimal marketDiffValue = ObjectUtils.isEmpty(odds.getMarketDiffValue()) ? BigDecimal.ZERO : new BigDecimal(odds.getMarketDiffValue());
            if (odds.getOddsType().equalsIgnoreCase(map.get("oddsType").toString())){
                if (DataSourceTypeEnum.AUTOMATIC.getValue().intValue() == config.getDataSource()){
                    if (marketDiffValue.compareTo(new BigDecimal(map.get("marketDiffValue").toString())) != 0){
                        isOddsChange = Boolean.TRUE;
                    }
                }else {
                    if (oddsValue.compareTo(new BigDecimal(map.get("fieldOddsValue").toString()).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE))) != 0){
                        isOddsChange = Boolean.TRUE;
                    }
                }
                if (!ObjectUtils.isEmpty(map.get("anchor"))){
                    odds.setAnchor(Integer.parseInt(map.get("anchor").toString()));
                }
                if (!ObjectUtils.isEmpty(map.get("active"))){
                    odds.setActive(Integer.parseInt(map.get("active").toString()));
                }
                odds.setOddsValue(new BigDecimal(map.get("fieldOddsValue").toString()).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue());
            }
        }
        return isOddsChange;
    }

    /**
     * @Description   //生成盘口列表
     * @Param [config]
     * @Author  sean
     * @Date   2021/5/25
     * @return void
     **/
    public void updateMatchMarketValue(RcsMatchMarketConfig config) {
        // 大小球球头校验 aden说负数直接转成正数
        if (TradeConstant.FOOTBALL_HEAD_CHECK_PLAYS.contains(config.getPlayId().intValue())) {
            config.setHomeMarketValue(config.getHomeMarketValue().abs());
        }

        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        if (MarketUtils.isAuto(dataSource)){
            throw new RcsServiceException("自动模式下无法修改球头");
        }
        config.setDataSource(dataSource.longValue());
        
//        List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        if (CollectionUtils.isEmpty(playAllMarketList)){
            RcsTournamentTemplatePlayMargain matchConfig = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
            if (ObjectUtils.isEmpty(matchConfig)) {
                throw new RcsServiceException("玩法未开售，不能新增盘口");
            }
            config.setIsSpecialPumping(matchConfig.getIsSpecialPumping());
            config.setSpecialOddsInterval(matchConfig.getSpecialOddsInterval());
            //走新增盘口逻辑。。。
            RcsTournamentTemplatePlayMargainRef ref = rcsTournamentTemplatePlayMargainMapper.queryFootballMatchConfig(config);
            if(ref == null) {
                throw new RcsServiceException("玩法未开售");
            }
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(config.getMatchId());
            if (ObjectUtils.isEmpty(standardMatchInfo) ||
                    ObjectUtils.isEmpty(standardMatchInfo.getMatchPeriodId()) ||
                    (!FootBallPlayEnum.getPeriod(config.getPlayId()).contains(standardMatchInfo.getMatchPeriodId().intValue()))) {
                throw new RcsServiceException("当前比赛阶段不能新增该玩法盘口");
            }
        	config.setMargin(new BigDecimal(ref.getMargain()));
            if (!ObjectUtils.isEmpty(ref.getPauseMargain())){
            	config.setTimeOutMargin(new BigDecimal(ref.getPauseMargain()));
            }

            // 切换操盘方式不推送赔率
            tradeSubPlayCommonService.changeTradeType(config,SportIdEnum.FOOTBALL.getId());

            RcsTournamentTemplatePlayMargain buildConfig = tradeVerificationService.queryTournamentTemplateConfig(config);
            if (Lists.newArrayList(2L, 4L, 18L, 19L, 113L, 114L, 121L, 122L, 127L, 128L, 134L, 335L, 334L, 306L, 307L, 308L, 309L,130L,324L,327L).contains(config.getPlayId())) {
                // 构建构建附加盘标志
                config.setMarketBuildFlag(true);
                config.setBuildConfig(buildConfig);
            }

            config.setMarketType(MarketKindEnum.Malaysia.getValue());
            BigDecimal spread = new BigDecimal(buildConfig.getMargain());
            BigDecimal malayOdds = BigDecimal.ONE.subtract(spread.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP)).setScale(2, RoundingMode.DOWN);
            List<Map<String, Object>> oddsList = new ArrayList<Map<String,Object>>();
            config.setOddsList(oddsList);

            if(Lists.newArrayList(4,19,128,130,143,269,270,113,121,334,306,308).contains(config.getPlayId().intValue())) {//让球
            	Map<String, Object> map1 = new HashMap<String, Object>();
            	map1.put("oddsType", "1");
            	map1.put("fieldOddsValue", malayOdds.toPlainString());

            	Map<String, Object> map2 = new HashMap<String, Object>();
            	map2.put("oddsType", "2");
            	map2.put("fieldOddsValue", malayOdds.toPlainString());

            	oddsList.add(map1);
            	oddsList.add(map2);
            }else if (Arrays.asList(2,18,26,114,122,127,307,325,328,331,332,134,335,307,309).contains(config.getPlayId().intValue())){
            	Map<String, Object> map1 = new HashMap<String, Object>();
            	map1.put("oddsType", "Under");
            	map1.put("fieldOddsValue", malayOdds.toPlainString());

            	Map<String, Object> map2 = new HashMap<String, Object>();
            	map2.put("oddsType", "Over");
            	map2.put("fieldOddsValue", malayOdds.toPlainString());

            	oddsList.add(map1);
            	oddsList.add(map2);
            }else {
            	throw new RcsServiceException("不支持的玩法，请核对玩法！");
            }

            RcsStandardMarketDTO market = tradeCommonService.setOddsValue(config,null,new BigDecimal(buildConfig.getMargain()));

            StandardMarketDTO standardMarketDTO = JSONArray.parseObject(JSONArray.toJSONString(market),StandardMarketDTO.class);
            // 推送赔率
            tradeCommonService.putTradeMarketOdds(config, Arrays.asList(standardMarketDTO),null);

            return;
        }

        for (RcsStandardMarketDTO market : playAllMarketList) {
            if (new BigDecimal(market.getAddition1()).compareTo(config.getHomeMarketValue()) == 0) {
                throw new RcsServiceException("球头值已存在，请重新调整");
            }
            if (market.getId().equalsIgnoreCase(config.getMarketId().toString())){
                if (market.getId().equalsIgnoreCase(config.getMarketId().toString())) {
                    market.setAddition1(config.getHomeMarketValue().toPlainString());
                }
                if (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue())){
                    market.setAddition2(config.getHomeMarketValue().toPlainString());
                    for (StandardMarketOddsDTO dto:market.getMarketOddsList()){
                        if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(dto.getOddsType())){
                            dto.setNameExpressionValue(new BigDecimal(market.getAddition1()).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)).stripTrailingZeros().toPlainString());
                        }
                    }
                }
                tradeCommonService.updateBenchmarkScore(config, market);
            }
        }
        // 推送赔率
        List<StandardMarketDTO> marketList = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList),StandardMarketDTO.class);
        tradeCommonService.putTradeMarketOdds(config, marketList,null);
    }
    /**
     * @Description   //修改冠军玩法赔率
     * @Param [updateOddsValueVo]
     * @Author  sean
     * @Date   2021/6/16
     * @return void
     **/
    public void updateChampionMarketOdds(UpdateOddsValueVo updateOddsValueVo) {
        RcsTradeConfig tradeConfig = rcsTradeConfigMapper.selectRcsTradeConfig(updateOddsValueVo.getMatchId().toString(), updateOddsValueVo.getPlayId().toString(),updateOddsValueVo.getMarketId().toString());
        if (ObjectUtils.isEmpty(tradeConfig) || MarketUtils.isAuto(tradeConfig.getDataSource())) {
            throw new RcsServiceException("自动不允许修改赔率");
        }
        //修改盘口赔率
        StandardMarketDTO market = standardSportMarketMapper.selectChampionOddsByMarketIds(updateOddsValueVo);
        log.info("::{}::,market={}",JSONObject.toJSONString(market));
        if (ObjectUtils.isEmpty(market) || CollectionUtils.isEmpty(market.getMarketOddsList())){
            throw new RcsServiceException("没有找到对应盘口赔率");
        }
        tradeCommonService.setI18nName(market);
        List<StandardMarketOddsDTO> marketOddsList = market.getMarketOddsList();
        RcsMatchMarketConfig config = JSONObject.parseObject(JSONObject.toJSONString(updateOddsValueVo),RcsMatchMarketConfig.class);
        tradeCommonService.ifOddsChangeAndClearConfig(config,updateOddsValueVo.getOddsValueList(), JSONArray.parseArray(JSONArray.toJSONString(marketOddsList),StandardSportMarketOdds.class),1L);
        for (OddsValueVo oddsValueVo : updateOddsValueVo.getOddsValueList()) {
            if (oddsValueVo.getValue() <= 1) {
                throw new RcsServiceException("赔率小于1不能进行相关操作");
            }
            for (StandardMarketOddsDTO standardMarketOddsDTO : marketOddsList) {
                if (standardMarketOddsDTO.getOddsType().equals(oddsValueVo.getOddsType())) {
                    BigDecimal v = new BigDecimal(String.valueOf(oddsValueVo.getValue())).multiply(new BigDecimal(String.valueOf(BaseConstants.MULTIPLE_VALUE)));
                    standardMarketOddsDTO.setOddsValue(v.intValue());
//                    standardMarketOddsDTO.setActive(NumberUtils.INTEGER_ONE);
                }
            }
        }
        market.setMarketOddsList(marketOddsList);
        market.setId(updateOddsValueVo.getMarketId().toString());
        market = JSONObject.parseObject(JSONObject.toJSONString(market),StandardMarketDTO.class);
        market.setDataSourceCode("PA");   //冠军手动操盘更改为PA，ws推送区分操盘模式（手动PA和自动SR）
        tradeCommonService.putTradeMarketOdds(config, Arrays.asList(market),NumberUtils.INTEGER_TWO);
    }
    /**
     * @Description   //快捷新增盘口
     * @Param [config]
     * @Author  sean
     * @Date   2021/7/25
     * @return void
     **/
    public void convenientCreateMarket(RcsMatchMarketConfig config) {
        if (!TradeConstant.FOOTBALL_X_GOAL_PLAYS.contains(config.getPlayId().intValue())){
            throw new RcsServiceException("不支持的玩法");
        }
        //自动手动放进去
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        config.setDataSource(dataSource.longValue());
        if (MarketUtils.isAuto(dataSource)){
            throw new RcsServiceException("自动模式不支持新增盘口");
        }
        Long marketId = config.getMarketId();
        config.setMarketId(null);
        // 子玩法id换成融合的子玩法id
        String subPlayId = config.getSubPlayId();
        String rongheSubPlayId = SubPlayUtil.getRongHeSubPlayId(config);
        config.setSubPlayId(null);
//        List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        RcsStandardMarketDTO market = null;
//        RcsStandardMarketDTO oldMarket = null;
        if (CollectionUtils.isNotEmpty(playAllMarketList)){
            for (RcsStandardMarketDTO marketDTO :playAllMarketList){
                if (SubPlayUtil.getRongHeSubPlayId(marketDTO).equalsIgnoreCase(rongheSubPlayId)){
                    throw new RcsServiceException("已存在盘口不可以新增");
                }
                if (marketDTO.getId().equalsIgnoreCase(marketId.toString())){
                    config.setSubPlayId(subPlayId);
                    market = JSONObject.parseObject(JSONObject.toJSONString(marketDTO),RcsStandardMarketDTO.class);
//                    oldMarket = marketDTO;
                    tradeCommonService.setAdditionForXPlay(config,market);
                    market.setChildStandardCategoryId(Long.parseLong(SubPlayUtil.getRongHeSubPlayId(market)));
                    marketDTO.setPlaceNumStatus(NumberUtils.INTEGER_ONE);
                    marketDTO.setThirdMarketSourceStatus(NumberUtils.INTEGER_TWO);
                    tradeCommonService.updatePlaceStatus(config.getMatchId(), config.getPlayId(), config.getMarketIndex(), NumberUtils.INTEGER_ONE, SubPlayUtil.getRongHeSubPlayId(marketDTO));
                }
            }
            if (!ObjectUtils.isEmpty(market)){
                playAllMarketList.add(market);
//                playAllMarketList.remove(oldMarket);
            }
            List<StandardMarketDTO> marketList = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList),StandardMarketDTO.class);
            tradeCommonService.putTradeMarketOdds(config, marketList,null);
        }else {
            throw new RcsServiceException("玩法没有盘口不支持新增盘口");
        }
    }
}
