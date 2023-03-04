package com.panda.sport.rcs.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.panda.merge.dto.MarketPlaceDtlDTO;
import com.panda.merge.dto.PlaySetStatusConfigDTO;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.TradeMarketConfigDTO;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.constants.MqConstant;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.Football;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.enums.YesNoEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.factory.BeanFactory;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.MatchPeriod;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.service.ApiService;
import com.panda.sport.rcs.trade.service.LinkageCommonService;
import com.panda.sport.rcs.trade.service.MarketBuildService;
import com.panda.sport.rcs.trade.service.TradeModeService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.RcsLocalCacheUtils;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;
import com.panda.sport.rcs.trade.wrapper.RcsMarketCategorySetRelationService;
import com.panda.sport.rcs.trade.wrapper.RcsMatchPlayConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketOddsService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.RcsAssert;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.MatchMarketTradeTypeVo;
import com.panda.sport.rcs.vo.MatchStatusAndDataSuorceVo;
import com.panda.sport.rcs.vo.trade.PlaceStatusInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 操盘状态服务
 * @Author : Paca
 * @Date : 2021-07-28 13:54
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class TradeStatusServiceImpl implements TradeStatusService {

    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    @Autowired
    protected StandardSportMarketService standardSportMarketService;
    @Autowired
    protected StandardSportMarketOddsService standardSportMarketOddsService;
    @Autowired
    protected RcsMarketCategorySetRelationService rcsMarketCategorySetRelationService;
    @Autowired
    protected RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Autowired
    private RcsMatchPlayConfigService rcsMatchPlayConfigService;
    @Autowired
    private MarketCategorySetService marketCategorySetService;

    @Autowired
    private ApiService apiService;
    @Autowired
    protected MarketBuildService marketBuildService;
    @Autowired
    private TradeModeService tradeModeService;
    @Autowired
    private LinkageCommonService linkageCommonService;

    @Autowired
    protected ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    protected RedisUtils redisUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateTradeStatus(MarketStatusUpdateVO updateVO) {
        CommonUtils.mdcPutIfAbsent();
        String linkId = StringUtils.isBlank(updateVO.getLinkId())?updateVO.generateLinkId("status"):updateVO.getLinkId();
        log.info("::{}::修改盘口状态开始：入参={}", linkId, JSON.toJSONString(updateVO));
        getSportId(updateVO);
        updateVO.updateStatusParamCheck();
        Integer tradeLevel = updateVO.getTradeLevel();
        Long sportId = updateVO.getSportId();
        Long playId = updateVO.getCategoryId();
        Long subPlayId = updateVO.getSubPlayId();
        if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
                // 占位符玩法盘口位置开关封锁，必传子玩法ID
                RcsAssert.gtZero(subPlayId, "子玩法ID[subPlayId]不能为空");
            } else {
                updateVO.setSubPlayId(null);
            }
        } else if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            if (!RcsConstant.isPlaceholderPlay(sportId, playId)) {
                updateVO.setSubPlayId(null);
            }
        } else {
            updateVO.setSubPlayId(null);
        }
        if (TradeStatusEnum.isEnd(updateVO.getMarketStatus()) && SportIdEnum.FOOTBALL.isNo(sportId)) {
            throw new RcsServiceException("只有足球才支持收盘");
        }

        // 玩法分组
        playGroup(updateVO);
        // 保存配置
        rcsTradeConfigService.saveTradeStatusConfig(updateVO);
        // 清除统计跳分次数
        clearCountTimes(updateVO);

        if (TradeLevelEnum.isMatchLevel(tradeLevel)) {
            return updateMatchStatus(updateVO);
        }
        if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            return updatePlaceStatus(updateVO);
        }
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            return updatePlayStatus(updateVO);
        }
        if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            return updatePlaySetStatus(updateVO);
        }
        if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            return updateBatchPlayStatus(updateVO);
        }
        if (TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            return updatePlaySetCodeStatus(updateVO);
        }
        return linkId;
    }

    private void clearCountTimes(MarketStatusUpdateVO updateVO) {
        Integer tradeLevel = updateVO.getTradeLevel();
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        if (SportIdEnum.FOOTBALL.isNo(sportId)) {
            return;
        }
        if (!TradeStatusEnum.isOpen(updateVO.getMarketStatus())) {
            return;
        }
        if (!NumberUtils.INTEGER_ONE.equals(updateVO.getOperateSource())) {
            return;
        }
        Long playId = updateVO.getCategoryId();
        List<Long> playIdList = updateVO.getCategoryIdList();
        List<Long> playIds = null;
        if (TradeLevelEnum.isPlayLevel(tradeLevel) && TradeConstant.RCS_COUNT_TIMES_PLAY.contains(playId.intValue())) {
            playIds = Lists.newArrayList(playId);
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            playIds = playIdList.stream().filter(pId -> TradeConstant.RCS_COUNT_TIMES_PLAY.contains(pId.intValue())).collect(Collectors.toList());
        } else if (TradeLevelEnum.isMarketLevel(tradeLevel) && TradeConstant.THREE_ODDS_TYPE.contains(playId.intValue())) {
            playIds = Lists.newArrayList(playId);
        }
        if (CollectionUtils.isNotEmpty(playIds)) {
            ClearDTO clearDTO = new ClearDTO();
            clearDTO.setMatchId(matchId);
            clearDTO.setPlayIds(playIds);
            clearDTO.setSportId(sportId);
            clearDTO.setGlobalId(CommonUtils.getLinkId("clearTimes"));
            producerSendMessageUtils.sendMessage("CLEAR_FOOTBALL_JUMP_TIMES", matchId.toString(), clearDTO.getGlobalId(), clearDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateBatchSubPlayTradeStatus(MarketStatusUpdateVO updateVO) {
        String linkId = updateVO.generateLinkId("status");
        log.info("::{}::修改盘口状态开始：入参={}", linkId, JSON.toJSONString(updateVO));
        getSportId(updateVO);
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        Long playId = updateVO.getCategoryId();
        List<Long> subPlayIds = updateVO.getSubPlayIds();
        Integer status = updateVO.getMarketStatus();
        String userId = String.valueOf(updateVO.getUpdateUserId());

        List<RcsTradeConfig> configList = subPlayIds.stream().map(subPlayId -> {
            RcsTradeConfig config = new RcsTradeConfig();
            config.setMatchId(matchId.toString());
            config.setTraderLevel(updateVO.getTradeLevel());
            config.setStatus(status);
            config.setSourceType(updateVO.getLinkedType());
            config.setUpdateUser(userId);
            config.setTargerData(String.valueOf(playId));
            config.setSubPlayId(subPlayId);
            return config;
        }).collect(Collectors.toList());
        rcsTradeConfigService.saveBatch(configList);

        RcsTradeConfig playConfig = new RcsTradeConfig();
        playConfig.setStatus(status);
        playConfig.setId(Integer.MAX_VALUE);
        RcsTradeConfig playSetConfig = rcsTradeConfigService.getPlaySetStatusByPlayId(matchId, playId);
        Map<Long, Map<Long, Map<Integer, RcsTradeConfig>>> statusMap = rcsTradeConfigService.getSubPlayPlaceStatus(matchId, Lists.newArrayList(playId), subPlayIds);
        Map<Long, Map<Integer, RcsTradeConfig>> map = statusMap.getOrDefault(playId, Maps.newHashMap());
        List<MarketPlaceDtlDTO> list = Lists.newArrayList();
        subPlayIds.forEach(subPlayId -> {
            Map<Integer, RcsTradeConfig> placeStatusMap = map.getOrDefault(subPlayId, Maps.newHashMap());
            List<MarketPlaceDtlDTO> placeList = getPlaceStatusByPlay(playId, subPlayId, playConfig, playSetConfig, placeStatusMap);
            list.addAll(placeList);
        });
        PlaceStatusInfo placeStatusInfo = new PlaceStatusInfo();
        placeStatusInfo.setPlaceList(list);

        putTradeMarketPlaceConfigAndRedis(sportId, matchId, placeStatusInfo, linkId);
        apiService.pushOdds(updateVO, Lists.newArrayList(playId), null, null, linkId);
        return linkId;
    }

    private String updateMatchStatus(MarketStatusUpdateVO updateVO) {
        log.info("::{}::修改赛事状态：{}", CommonUtil.getRequestId(updateVO.getLinkId()),JSON.toJSONString(updateVO));
        Integer status = updateVO.getMarketStatus();
        // 调用融合RPC接口推送配置
        TradeMarketConfigDTO configDTO = apiService.generateTradeMarketConfigDTO(updateVO);
        configDTO.setMarketStatus(status);
        // 赛事状态缓存
        String key = RedisKey.getMatchTradeStatusKey(updateVO.getMatchId());
        redisUtils.set(key, String.valueOf(status));
        redisUtils.expire(key, 5L, TimeUnit.MINUTES);
        apiService.putTradeMarketConfig(configDTO, updateVO.getLinkId());
        // 赛事开盘下沉到盘口
        if (TradeStatusEnum.isOpen(status)) {
            apiService.pushOdds(updateVO, Lists.newArrayList(), status, null, updateVO.getLinkId());
        }
        // 发送MQ
        updateTradeStatusSendMq(updateVO);
        return updateVO.getLinkId();
    }

    private String updatePlaceStatus(MarketStatusUpdateVO updateVO) {
        log.info("::{}::修改位置状态：" + JSON.toJSONString(updateVO),CommonUtil.getRequestId(updateVO.getMatchId()));
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        Long playId = updateVO.getCategoryId();
        Long subPlayId = updateVO.getSubPlayId();
        Integer placeNum = updateVO.getMarketPlaceNum();
        Integer status = updateVO.getMarketStatus();

        // 缓存位置状态
        setPlaceStatusToRedis(sportId, matchId, playId, subPlayId, placeNum, status);
        // 调用融合RPC接口，推送盘口位置状态
        MarketPlaceDtlDTO placeDTO = apiService.getPlaceStatusDto(playId, subPlayId, placeNum, status);
        apiService.putTradeMarketPlaceConfig(matchId, Lists.newArrayList(placeDTO), updateVO.getLinkId());

        if (YesNoEnum.isYes(updateVO.getIsPushOdds())) {
            apiService.pushOdds(updateVO, Lists.newArrayList(playId), null, null, updateVO.getLinkId());
        }
        return updateVO.getLinkId();
    }

    private String updatePlayStatus(MarketStatusUpdateVO updateVO) {
        log.info("::{}::修改玩法状态：" + JSON.toJSONString(updateVO),updateVO.getLinkId());
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        Long playId = updateVO.getCategoryId();
        Long subPlayId = updateVO.getSubPlayId();
        Integer status = updateVO.getMarketStatus();

        PlaceStatusInfo placeStatusInfo = new PlaceStatusInfo();
        if (RcsConstant.isPlaceholderPlay(sportId, playId) && subPlayId == null) {
            // 占位符玩法总开关
            MarketPlaceDtlDTO placeStatusDto = apiService.getPlaceStatusDto(playId, null, -1, status);
            placeStatusInfo.setMainPlayStatusList(Lists.newArrayList(placeStatusDto));
        } else {
            RcsTradeConfig playConfig = new RcsTradeConfig();
            playConfig.setStatus(status);
            playConfig.setId(Integer.MAX_VALUE);
            RcsTradeConfig playSetConfig = rcsTradeConfigService.getPlaySetStatusByPlayId(matchId, playId);
            Map<Integer, RcsTradeConfig> placeStatusMap;
            if (subPlayId == null) {
                placeStatusMap = rcsTradeConfigService.getMarketPlaceStatus(matchId, playId);
            } else {
                placeStatusMap = rcsTradeConfigService.getSubPlayPlaceStatus(matchId, playId, subPlayId);
            }
            List<MarketPlaceDtlDTO> placeList = getPlaceStatusByPlay(playId, subPlayId, playConfig, playSetConfig, placeStatusMap);
            placeStatusInfo.setPlaceList(placeList);
        }
        putTradeMarketPlaceConfigAndRedis(sportId, matchId, placeStatusInfo, updateVO.getLinkId());
        if (LinkedTypeEnum.FIFTEEN_MINUTES_SUB_PLAY_CLOSE.getCode().equals(updateVO.getLinkedType()) && subPlayId != null) {
            Map<Long, Long> closeSubPlayMap = Maps.newHashMap();
            closeSubPlayMap.put(playId, subPlayId);
            updateVO.setCloseSubPlayMap(closeSubPlayMap);
        }
        if (YesNoEnum.isYes(updateVO.getIsPushOdds())) {
            apiService.pushOdds(updateVO, Lists.newArrayList(playId), null, null, updateVO.getLinkId());
        }
        // 发送MQ
        updateTradeStatusSendMq(updateVO);
        return updateVO.getLinkId();
    }

    private String updatePlaySetStatus(MarketStatusUpdateVO updateVO) {
        log.info("::{}::修改玩法集状态：" + JSON.toJSONString(updateVO),updateVO.getLinkId());
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        Integer status = updateVO.getMarketStatus();
        List<Long> normalPlayIds = updateVO.getNormalPlayIds();
        List<Long> placeholderPlayIds = updateVO.getPlaceholderPlayIds();
        if (SportIdEnum.FOOTBALL.isYes(sportId)) {
            String playSetCode = marketCategorySetService.getPlaySetCodeByPlaySetId(updateVO.getCategorySetId());
            if (StringUtils.isNotBlank(playSetCode)) {
                updateVO.setTradeLevel(TradeLevelEnum.PLAY_SET_CODE.getLevel());
                updateVO.setPlaySetCode(playSetCode);
                return updatePlaySetCodeStatus(updateVO);
            }
        }

        RcsTradeConfig playSetConfig = new RcsTradeConfig();
        playSetConfig.setStatus(status);
        playSetConfig.setId(Integer.MAX_VALUE);
        PlaceStatusInfo placeStatusInfo = getPlaceStatusByPlaySet(matchId, normalPlayIds, placeholderPlayIds, playSetConfig);
        putTradeMarketPlaceConfigAndRedis(sportId, matchId, placeStatusInfo, updateVO.getLinkId());
        if (YesNoEnum.isYes(updateVO.getIsPushOdds())) {
            apiService.pushOdds(updateVO, updateVO.getCategoryIdList(), null, null, updateVO.getLinkId());
        }
        // 发送MQ
        updateTradeStatusSendMq(updateVO);
        return updateVO.getLinkId();
    }

    private String updatePlaySetCodeStatus(MarketStatusUpdateVO updateVO) {
        log.info("::{}::修改玩法集编码状态：" + JSON.toJSONString(updateVO),updateVO.getLinkId());
        putPlaySetCodeStatusConfig(updateVO);

        RcsTradeConfig playConfig = new RcsTradeConfig();
        playConfig.setStatus(updateVO.getMarketStatus());
        playConfig.setId(Integer.MAX_VALUE);
        PlaceStatusInfo placeStatusInfo = getPlaceStatusByBatchPlay(updateVO.getMatchId(), updateVO.getSealNormalPlayIds(), updateVO.getSealPlaceholderPlayIds(), playConfig);
        putTradeMarketPlaceConfigAndRedis(updateVO.getSportId(), updateVO.getMatchId(), placeStatusInfo, updateVO.getLinkId());

        if (YesNoEnum.isYes(updateVO.getIsPushOdds())) {
            apiService.pushOdds(updateVO, updateVO.getCategoryIdList(), null, null, updateVO.getLinkId());
        }
        // 发送MQ
        updateTradeStatusSendMq(updateVO);
        return updateVO.getLinkId();
    }

    private String updateBatchPlayStatus(MarketStatusUpdateVO updateVO) {
        log.info("::{}::批量修改玩法状态：" + JSON.toJSONString(updateVO),updateVO.getLinkId());
        Integer status = updateVO.getMarketStatus();
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        List<Long> playIdList = updateVO.getCategoryIdList();
        List<Long> normalPlayIds = updateVO.getNormalPlayIds();
        List<Long> placeholderPlayIds = updateVO.getPlaceholderPlayIds();

        if (Football.CORNER_PLAY_SET_ID.equals(updateVO.getCategorySetId()) && (TradeStatusEnum.isClose(status) || TradeStatusEnum.isOpen(status))) {
            sendMqCornerShow(sportId, matchId, status, updateVO.getLinkId());
        }

        PlaceStatusInfo placeStatusInfo;
        if (YesNoEnum.isYes(updateVO.getSourceCloseFlag())) {
            // 自动关盘，数据源关盘
            placeStatusInfo = new PlaceStatusInfo();
            if (CollectionUtils.isNotEmpty(normalPlayIds)) {
                List<MarketPlaceDtlDTO> placeList = new ArrayList<>(normalPlayIds.size() * RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT);
                normalPlayIds.forEach(playId -> {
                    List<MarketPlaceDtlDTO> list = apiService.getAllPlaceStatusDto(playId, null, status);
                    placeList.addAll(list);
                });
                placeStatusInfo.setPlaceList(placeList);
            }
            if (CollectionUtils.isNotEmpty(placeholderPlayIds)) {
                List<MarketPlaceDtlDTO> mainPlayStatusList = new ArrayList<>(placeholderPlayIds.size() * RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT);
                placeholderPlayIds.forEach(playId -> {
                    List<MarketPlaceDtlDTO> list = apiService.getAllPlaceStatusDto(playId, null, status);
                    mainPlayStatusList.addAll(list);
                });
                placeStatusInfo.setMainPlayStatusList(mainPlayStatusList);
            }
        } else {
            RcsTradeConfig playConfig = new RcsTradeConfig();
            playConfig.setStatus(status);
            playConfig.setId(Integer.MAX_VALUE);
            placeStatusInfo = getPlaceStatusByBatchPlay(matchId, normalPlayIds, placeholderPlayIds, playConfig);
        }
        putTradeMarketPlaceConfigAndRedis(sportId, matchId, placeStatusInfo, updateVO.getLinkId());
        if (YesNoEnum.isYes(updateVO.getIsPushOdds())) {
            apiService.pushOdds(updateVO, playIdList, null, null, updateVO.getLinkId());
        }
        // 发送MQ
        updateTradeStatusSendMq(updateVO);
        return updateVO.getLinkId();
    }

    private List<MarketPlaceDtlDTO> getPlaceStatusByPlay(Long playId, Long subPlayId, RcsTradeConfig playConfig, RcsTradeConfig playSetConfig, Map<Integer, RcsTradeConfig> placeConfigMap) {
        log.info("::{}::获取玩法下位置状态：playId={},subPlayId={},playConfig={},playSetConfig={},placeConfigMap={}",CommonUtil.getRequestId(playConfig.getMatchId()), playId, subPlayId, JSON.toJSONString(playConfig), JSON.toJSONString(playSetConfig), JSON.toJSONString(placeConfigMap));
        if (CollectionUtils.isEmpty(placeConfigMap)) {
            // 没有盘口状态修改记录，盘口位置-1：表示玩法下所有盘口都是此状态
            RcsTradeConfig placeConfig = BeanFactory.defaultMarketPlaceStatus();
            Integer placeStatus = getFinalPlaceStatus(placeConfig, playConfig, playSetConfig);
            return apiService.getAllPlaceStatusDto(playId, subPlayId, placeStatus);
        }
        List<MarketPlaceDtlDTO> placeList = new ArrayList<>(RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT);
        // 盘口位置，最多10个位置
        for (int placeNum = 1; placeNum <= RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT; placeNum++) {
            RcsTradeConfig placeConfig = placeConfigMap.getOrDefault(placeNum, BeanFactory.defaultMarketPlaceStatus());
            Integer placeStatus = getFinalPlaceStatus(placeConfig, playConfig, playSetConfig);
            MarketPlaceDtlDTO placeStatusDto = apiService.getPlaceStatusDto(playId, subPlayId, placeNum, placeStatus);
            placeList.add(placeStatusDto);
        }
        return placeList;
    }

    private PlaceStatusInfo getPlaceStatusByPlaySet(Long matchId, List<Long> normalPlayIds, List<Long> placeholderPlayIds, RcsTradeConfig playSetConfig) {
        log.info("::{}::获取玩法集下位置状态：normalPlayIds={},placeholderPlayIds={},playSetConfig={}",CommonUtil.getRequestId(matchId), normalPlayIds, placeholderPlayIds, JSON.toJSONString(playSetConfig));
        PlaceStatusInfo placeStatusInfo = new PlaceStatusInfo();
        if (CollectionUtils.isNotEmpty(normalPlayIds)) {
            List<MarketPlaceDtlDTO> placeList = new ArrayList<>(normalPlayIds.size() * RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT);
            // 玩法状态
            Map<Long, RcsTradeConfig> playStatusMap = rcsTradeConfigService.getPlayStatus(matchId, normalPlayIds);
            // 玩法下位置状态
            Map<Long, Map<Integer, RcsTradeConfig>> placeStatusMap = rcsTradeConfigService.getMarketPlaceStatus(matchId, normalPlayIds);
            for (Long playId : normalPlayIds) {
                RcsTradeConfig playConfig = playStatusMap.getOrDefault(playId, BeanFactory.defaultCategoryStatus());
                Map<Integer, RcsTradeConfig> placeConfigMap = placeStatusMap.get(playId);
                List<MarketPlaceDtlDTO> list = getPlaceStatusByPlay(playId, null, playConfig, playSetConfig, placeConfigMap);
                placeList.addAll(list);
            }
            placeStatusInfo.setPlaceList(placeList);
        }
        if (CollectionUtils.isNotEmpty(placeholderPlayIds)) {
            // 占位符玩法总开关封锁
            Integer status = playSetConfig.getStatus();
            List<MarketPlaceDtlDTO> list = placeholderPlayIds.stream().map(pid -> apiService.getPlaceStatusDto(pid, null, -1, status)).collect(Collectors.toList());
            placeStatusInfo.setMainPlayStatusList(list);
        }
        return placeStatusInfo;
    }

    private PlaceStatusInfo getPlaceStatusByBatchPlay(Long matchId, List<Long> normalPlayIds, List<Long> placeholderPlayIds, RcsTradeConfig playConfig) {
        log.info("::{}::获取批量玩法下位置状态：matchId={},normalPlayIds={},placeholderPlayIds={},playConfig={}",CommonUtil.getRequestId(matchId), normalPlayIds, placeholderPlayIds, JSON.toJSONString(playConfig));
        PlaceStatusInfo placeStatusInfo = new PlaceStatusInfo();
        if (CollectionUtils.isNotEmpty(normalPlayIds)) {
            List<MarketPlaceDtlDTO> placeList = new ArrayList<>(normalPlayIds.size() * RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT);
            // 玩法下位置状态
            Map<Long, Map<Integer, RcsTradeConfig>> placeStatusMap = rcsTradeConfigService.getMarketPlaceStatus(matchId, normalPlayIds);
            for (Long playId : normalPlayIds) {
                RcsTradeConfig playSetConfig = rcsTradeConfigService.getPlaySetStatusByPlayId(matchId, playId);
                Map<Integer, RcsTradeConfig> placeConfigMap = placeStatusMap.get(playId);
                List<MarketPlaceDtlDTO> list = getPlaceStatusByPlay(playId, null, playConfig, playSetConfig, placeConfigMap);
                placeList.addAll(list);
            }
            placeStatusInfo.setPlaceList(placeList);
        }
        if (CollectionUtils.isNotEmpty(placeholderPlayIds)) {
            // 占位符玩法总开关封锁
            Integer status = playConfig.getStatus();
            List<MarketPlaceDtlDTO> list = placeholderPlayIds.stream().map(pid -> apiService.getPlaceStatusDto(pid, null, -1, status)).collect(Collectors.toList());
            placeStatusInfo.setMainPlayStatusList(list);
        }

        return placeStatusInfo;
    }

    /**
     * 获取最终位置状态
     *
     * @param placeConfig
     * @param playConfig
     * @param playSetConfig
     * @return
     */
    private Integer getFinalPlaceStatus(final RcsTradeConfig placeConfig, final RcsTradeConfig playConfig, final RcsTradeConfig playSetConfig) {
        final Integer placeStatus = placeConfig.getStatus();
        final Integer playStatus = playConfig.getStatus();
        final Integer playSetStatus = playSetConfig.getStatus();
        final Integer placeConfigId = placeConfig.getId();
        final Integer playConfigId = playConfig.getId();
        final Integer playSetConfigId = playSetConfig.getId();
        if (!TradeStatusEnum.isOpen(placeStatus)) {
            // 位置状态 非开
            return placeStatus;
        }
        if (placeConfigId.compareTo(playConfigId) > 0 &&
                placeConfigId.compareTo(playSetConfigId) > 0) {
            // 盘口位置状态最后操作
            return placeStatus;
        }
        if (playSetConfigId.compareTo(placeConfigId) > 0 &&
                placeConfigId.compareTo(playConfigId) > 0) {
            // 先操作玩法状态，再操作盘口位置状态 开，最后操作玩法集状态，取玩法集状态
            return playSetStatus;
        }
        if (playSetConfigId.compareTo(playConfigId) > 0 &&
                playConfigId.compareTo(placeConfigId) > 0 &&
                TradeStatusEnum.isOpen(playStatus)) {
            // 先操作盘口位置状态 开，再操作玩法状态 开，最后操作玩法集状态，取玩法集状态
            return playSetStatus;
        } else {
            return playStatus;
        }
    }

    private void putTradeMarketPlaceConfigAndRedis(Long sportId, Long matchId, PlaceStatusInfo placeStatusInfo, String linkId) {
        List<MarketPlaceDtlDTO> placeDtoList = getPlaceStatus(sportId, matchId, placeStatusInfo);
        if (CollectionUtils.isNotEmpty(placeDtoList)) {
            // 调用融合RPC接口
            apiService.putTradeMarketPlaceConfig(matchId, placeDtoList, linkId);
        }
    }

    private List<MarketPlaceDtlDTO> getPlaceStatus(Long sportId, Long matchId, PlaceStatusInfo placeStatusInfo) {
        log.info("::{}::获取状态：sportId={},placeStatusInfo={}",CommonUtil.getRequestId(matchId), sportId, JSON.toJSONString(placeStatusInfo));
        if (placeStatusInfo == null) {
            return null;
        }
        List<MarketPlaceDtlDTO> placeList = placeStatusInfo.getPlaceList();
        if (CollectionUtils.isNotEmpty(placeList)) {
            String split = "-";
            // 根据玩法ID和子玩法ID分组
            Map<String, List<MarketPlaceDtlDTO>> groupMap = placeList.stream().collect(Collectors.groupingBy(placeDTO -> {
                if (placeDTO.getChildStandardCategoryId() == null) {
                    return String.valueOf(placeDTO.getStandardCategoryId());
                } else {
                    return placeDTO.getStandardCategoryId() + split + placeDTO.getChildStandardCategoryId();
                }
            }));

            groupMap.forEach((key, list) -> {
                Map<String, String> placeStatusMap = Maps.newHashMap();
                list.forEach(bean -> {
                    if (bean.getPlaceNum() == -1) {
                        // 盘口位置，最多10个位置
                        for (int placeNum = 1; placeNum <= RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT; placeNum++) {
                            placeStatusMap.put(String.valueOf(placeNum), bean.getPlaceNumStatus());
                        }
                    } else {
                        placeStatusMap.put(String.valueOf(bean.getPlaceNum()), bean.getPlaceNumStatus());
                    }
                });
                long playId;
                Long subPlayId = null;
                if (key.contains(split)) {
                    String[] array = key.split(split);
                    playId = NumberUtils.toLong(array[0]);
                    subPlayId = NumberUtils.toLong(array[1]);
                } else {
                    playId = NumberUtils.toLong(key);
                }
                // 缓存位置状态
                setPlaceStatusToRedis(sportId, matchId, playId, subPlayId, placeStatusMap);
            });
        }
        List<MarketPlaceDtlDTO> mainPlayStatusList = placeStatusInfo.getMainPlayStatusList();
        if (CollectionUtils.isNotEmpty(mainPlayStatusList)) {
            Map<String, String> mainPlayStatusMap = Maps.newHashMap();
            mainPlayStatusList.forEach(placeDto -> mainPlayStatusMap.put(String.valueOf(placeDto.getStandardCategoryId()), placeDto.getPlaceNumStatus()));
            // 缓存占位符主玩法状态
            setPlaceholderMainPlayStatusToRedis(matchId, mainPlayStatusMap);
        }
        return placeStatusInfo.getAllPlaceDtoList();
    }

    @Override
    public List<MarketPlaceDtlDTO> updateTradeModeSeal(MarketStatusUpdateVO updateVO, String linkId) {
        log.info("::{}::修改操盘模式封盘：updateVO={}",linkId, JSON.toJSONString(updateVO));
        final Integer tradeLevel = updateVO.getTradeLevel();
        if (!TradeLevelEnum.isPlayLevel(tradeLevel) && !TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            return null;
        }
        final Long sportId = updateVO.getSportId();
        final Long matchId = updateVO.getMatchId();
        final Integer status = TradeStatusEnum.SEAL.getStatus();
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            updateVO.setCategoryIdList(Lists.newArrayList(updateVO.getCategoryId()));
            updateVO.setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel());
        }
        playGroup(updateVO);
        updateVO.setTradeLevel(tradeLevel);

        PlaceStatusInfo placeStatusInfo = new PlaceStatusInfo();
        List<Long> normalPlayIds = updateVO.getNormalPlayIds();
        List<Long> placeholderPlayIds = updateVO.getPlaceholderPlayIds();

        if (CollectionUtils.isNotEmpty(normalPlayIds)) {
            // 普通玩法
            List<MarketPlaceDtlDTO> placeList = new ArrayList<>(normalPlayIds.size() * RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT);
            normalPlayIds.forEach(playId -> placeList.addAll(getSealNormalPlayPlaceStatus(sportId, matchId, playId, status)));
            placeStatusInfo.setPlaceList(placeList);
        }

        if (CollectionUtils.isNotEmpty(placeholderPlayIds)) {
            // 占位符玩法，总玩法开关封锁
            placeholderPlayIds = placeholderPlayIds.stream().filter(playId -> {
                // 获取占位符玩法总状态为开的玩法
                Integer mainPlayStatus = getPlaceholderMainPlayStatusFromRedis(matchId, playId);
                return TradeStatusEnum.isOpen(mainPlayStatus);
            }).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(placeholderPlayIds)) {
                // 开的盘口变成封，关、封、锁的盘口保留原样
                return null;
            }
            List<MarketPlaceDtlDTO> mainPlayStatusList = new ArrayList<>(placeholderPlayIds.size());
            placeholderPlayIds.forEach(playId -> mainPlayStatusList.add(apiService.getPlaceStatusDto(playId, null, -1, status)));
            placeStatusInfo.setMainPlayStatusList(mainPlayStatusList);
        }
//        List<RcsTradeConfig> configList = updateVO.getCategoryIdList().stream().map(playId -> {
//            RcsTradeConfig config = new RcsTradeConfig();
//            config.setMatchId(matchId.toString());
//            config.setTraderLevel(TradeLevelEnum.PLAY.getLevel());
//            config.setTargerData(String.valueOf(playId));
//            config.setStatus(status);
//            config.setSourceType(LinkedTypeEnum.TRADE_MODE.getCode());
//            config.setUpdateUser(String.valueOf(updateVO.getUpdateUserId()));
//            return config;
//        }).collect(Collectors.toList());
//        rcsTradeConfigService.saveBatch(configList);
//        putTradeMarketPlaceConfigAndRedis(sportId, matchId, placeStatusInfo, linkId);
        // 发送MQ
        updateVO.setMarketStatus(status);
        updateTradeStatusSendMq(updateVO);
        updateVO.setMarketStatus(null);
        return getPlaceStatus(sportId, matchId, placeStatusInfo);
    }

    private List<MarketPlaceDtlDTO> getSealNormalPlayPlaceStatus(Long sportId, Long matchId, Long playId, Integer status) {
        Map<Integer, Integer> placeStatusMap = getPlaceStatusFromRedis(sportId, matchId, playId, null);
        if (CollectionUtils.isEmpty(placeStatusMap)) {
            return apiService.getAllPlaceStatusDto(playId, null, status);
        }
        List<MarketPlaceDtlDTO> placeList = new ArrayList<>(RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT);
        for (int placeNum = 1; placeNum <= RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT; placeNum++) {
            Integer placeStatus = placeStatusMap.getOrDefault(placeNum, TradeStatusEnum.OPEN.getStatus());
            if (TradeStatusEnum.isOpen(placeStatus)) {
                // 开的盘口变成封，关、封、锁的盘口保留原样
                placeStatus = status;
            }
            MarketPlaceDtlDTO placeStatusDto = apiService.getPlaceStatusDto(playId, null, placeNum, placeStatus);
            placeList.add(placeStatusDto);
        }
        return placeList;
    }

    @Override
    public void switchLive(Long matchId, boolean matchStatusSeal) {
        log.info("::{}::早盘切滚球：matchId={},matchStatusSeal={}",CommonUtil.getRequestId(matchId), matchId, matchStatusSeal);
        StandardMatchInfo matchInfo = standardMatchInfoService.getById(matchId);
        if (matchInfo == null) {
            throw new RcsServiceException("赛事不存在：" + matchId);
        }
        Long sportId = matchInfo.getSportId();
        if (!Lists.newArrayList(1L, 2L, 3L, 4L, 5L, 7L, 8L, 9L, 10L).contains(sportId)) {
            log.warn("::{}::不是可操盘赛种：sportId={}", matchId, sportId);
            return;
        }
        Integer matchType = 0;
        boolean isMts = "MTS".equalsIgnoreCase(matchInfo.getLiveRiskManagerCode());
        basketballClear(sportId, matchId);

        // 所有玩法操盘模式
        Map<Long, Integer> tradeModeMap = rcsTradeConfigService.getTradeMode(matchId, null);
        // 切换自动模式时失败的玩法
        List<Long> switchErrorPlayList = allPlaySwitchAutoMode(sportId, matchId, matchType, tradeModeMap, isMts);

        if (matchStatusSeal) {
            // 赛事封盘
            MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
            updateVO.setTradeLevel(TradeLevelEnum.MATCH.getLevel());
            updateVO.setSportId(sportId);
            updateVO.setMatchId(matchId);
            updateVO.setMarketStatus(TradeStatusEnum.SEAL.getStatus());
            updateVO.setLinkedType(LinkedTypeEnum.LIVE.getCode());
            updateVO.setRemark("早盘切滚球，赛事封盘");
            updateVO.setMatchType(matchType);
            String linkId = updateTradeStatus(updateVO);
            log.info("::{}::早盘切滚球，赛事封盘", linkId);
        }

        if (!isMts) {
            // mts不用切A+/L
            CommonUtils.sleep(TimeUnit.MILLISECONDS, 500);
            basketballSwitchAutoPlus(sportId, matchId, matchType);
//            basketballSwitchLinkage(sportId, matchId, matchType);
        }

        // 所有未开盘的状态
        List<RcsTradeConfig> notOpenList = rcsTradeConfigService.getNotOpenStatusByMatchId(matchId);
        if (CollectionUtils.isNotEmpty(notOpenList)) {
            List<Long> notOpenPlayIds = new ArrayList<>(notOpenList.size());
            for (RcsTradeConfig config : notOpenList) {
                Integer tradeLevel = config.getTraderLevel();
                if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
                    // 玩法集开
                    MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
                    updateVO.setTradeLevel(tradeLevel);
                    updateVO.setSportId(sportId);
                    updateVO.setMatchId(matchId);
                    updateVO.setCategorySetId(NumberUtils.toLong(config.getTargerData()));
                    updateVO.setMarketStatus(TradeStatusEnum.OPEN.getStatus());
                    updateVO.setLinkedType(LinkedTypeEnum.LIVE.getCode());
                    updateVO.setMatchType(matchType);
                    updateVO.setSwitchErrorPlayList(switchErrorPlayList);
                    String linkId = updateTradeStatus(updateVO);
                    log.info("::{}::早盘切滚球，玩法集开盘" ,linkId);
                } else if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
                    long playId = NumberUtils.toLong(config.getTargerData());
                    Long subPlayId = config.getSubPlayId();
                    // 过滤切换自动失败的玩法
                    if (CollectionUtils.isEmpty(switchErrorPlayList) || !switchErrorPlayList.contains(playId)) {
                        if (subPlayId == null) {
                            notOpenPlayIds.add(playId);
                        } else {
                            // 子玩法开
                            MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
                            updateVO.setTradeLevel(tradeLevel);
                            updateVO.setSportId(sportId);
                            updateVO.setMatchId(matchId);
                            updateVO.setCategoryId(playId);
                            updateVO.setSubPlayId(subPlayId);
                            updateVO.setMarketStatus(TradeStatusEnum.OPEN.getStatus());
                            updateVO.setLinkedType(LinkedTypeEnum.LIVE.getCode());
                            updateVO.setMatchType(matchType);
                            String linkId = updateTradeStatus(updateVO);
                            log.info("::{}::早盘切滚球，子玩法开盘" ,linkId);
                        }
                    }
                } else if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
                    long playId = NumberUtils.toLong(config.getAddition1());
                    Long subPlayId = config.getSubPlayId();
                    if (CollectionUtils.isEmpty(switchErrorPlayList) || !switchErrorPlayList.contains(playId)) {
                        // 盘口位置开
                        MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
                        updateVO.setTradeLevel(tradeLevel);
                        updateVO.setSportId(sportId);
                        updateVO.setMatchId(matchId);
                        updateVO.setCategoryId(playId);
                        updateVO.setSubPlayId(subPlayId);
                        updateVO.setMarketPlaceNum(NumberUtils.toInt(config.getTargerData()));
                        updateVO.setMarketStatus(TradeStatusEnum.OPEN.getStatus());
                        updateVO.setLinkedType(LinkedTypeEnum.LIVE.getCode());
                        updateVO.setMatchType(matchType);
                        String linkId = updateTradeStatus(updateVO);
                        log.info("::{}::早盘切滚球，盘口位置开盘" ,linkId);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(switchErrorPlayList)) {
                // 过滤切换自动失败的玩法
                notOpenPlayIds.removeAll(switchErrorPlayList);
            }
            if (CollectionUtils.isNotEmpty(notOpenPlayIds)) {
                // 批量玩法开
                MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
                updateVO.setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel());
                updateVO.setSportId(sportId);
                updateVO.setMatchId(matchId);
                updateVO.setCategoryIdList(notOpenPlayIds);
                updateVO.setMarketStatus(TradeStatusEnum.OPEN.getStatus());
                updateVO.setLinkedType(LinkedTypeEnum.LIVE.getCode());
                updateVO.setMatchType(matchType);
                String linkId = updateTradeStatus(updateVO);
                log.info("::{}::早盘切滚球，批量玩法开盘",linkId);
            }
        }

        Set<Long> playIds = tradeModeMap.keySet();
        if (CollectionUtils.isNotEmpty(switchErrorPlayList)) {
            // 过滤切换自动失败的玩法
            playIds.removeAll(switchErrorPlayList);
        }
        if (CollectionUtils.isNotEmpty(playIds)) {
            // 删除缓存，并向上游推送装填
            List<MarketPlaceDtlDTO> list = new ArrayList<>(playIds.size());
            for (Long playId : playIds) {
                if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
                    continue;
                }
                String key = RedisKey.getMarketPlaceStatusConfigKey(matchId, playId);
                redisUtils.del(key);
                List<MarketPlaceDtlDTO> placeStatusDtoList = apiService.getAllPlaceStatusDto(playId, null, TradeStatusEnum.OPEN.getStatus());
                list.addAll(placeStatusDtoList);
            }
            if (CollectionUtils.isNotEmpty(list)) {
                apiService.putTradeMarketPlaceConfig(matchId, list, CommonUtils.getLinkId("_live_place"));
            }
        }
    }

    /**
     * 所有玩法切换A模式
     *
     * @param sportId
     * @param matchId
     * @param matchType
     * @param tradeModeMap
     * @return 切换失败的玩法ID集合
     */
    private List<Long> allPlaySwitchAutoMode(Long sportId, Long matchId, Integer matchType, Map<Long, Integer> tradeModeMap, boolean isMts) {
        if (CollectionUtils.isEmpty(tradeModeMap)) {
            // 都是A模式
            return null;
        }
        // 所有非A模式玩法
        List<Long> notAutoPlayIds = new ArrayList<>(tradeModeMap.size());
        tradeModeMap.forEach((playId, tradeMode) -> {
            if (!TradeEnum.isAuto(tradeMode)) {
                notAutoPlayIds.add(playId);
            }
        });
        if (CollectionUtils.isEmpty(notAutoPlayIds)) {
            // 都是A模式
            return null;
        }
        // 所有非A模式玩法切A模式
        MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
        updateVO.setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel());
        updateVO.setSportId(sportId);
        updateVO.setMatchId(matchId);
        updateVO.setCategoryIdList(notAutoPlayIds);
        updateVO.setTradeType(TradeEnum.AUTO.getCode());
        updateVO.setLinkedType(LinkedTypeEnum.LIVE.getCode());
        updateVO.setRemark("早盘切滚球，玩法切自动");
        updateVO.setMatchType(matchType);
        updateVO.setIsMts(isMts);
        updateVO.setIsSeal(YesNoEnum.N.getValue());
        String linkId = tradeModeService.updateTradeMode(updateVO);
        log.info("::{}::早盘切滚球，玩法切自动",linkId);

        List<Long> switchErrorPlayList = updateVO.getSwitchErrorPlayList();
        if (CollectionUtils.isNotEmpty(switchErrorPlayList)) {
            // 切换A模式失败，非L模式盘口数据源关盘，L模式盘口封盘
            List<Long> closePlayIds = Lists.newArrayList();
            List<Long> sealPlayIds = Lists.newArrayList();
            switchErrorPlayList.forEach(playId -> {
                Integer tradeMode = tradeModeMap.getOrDefault(playId, TradeEnum.AUTO.getCode());
                if (!TradeEnum.isLinkage(tradeMode)) {
                    closePlayIds.add(playId);
                } else {
                    sealPlayIds.add(playId);
                }
            });
            if (CollectionUtils.isNotEmpty(closePlayIds)) {
                MarketStatusUpdateVO closeUpdateVO = new MarketStatusUpdateVO();
                closeUpdateVO.setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel());
                closeUpdateVO.setSportId(sportId);
                closeUpdateVO.setMatchId(matchId);
                closeUpdateVO.setCategoryIdList(closePlayIds);
                closeUpdateVO.setMarketStatus(TradeStatusEnum.CLOSE.getStatus());
                closeUpdateVO.setSourceCloseFlag(YesNoEnum.Y.getValue());
                closeUpdateVO.setLinkedType(LinkedTypeEnum.LIVE.getCode());
                closeUpdateVO.setRemark("早盘切滚球，切自动失败玩法数据源关盘");
                closeUpdateVO.setMatchType(matchType);
                String closeLinkId = updateTradeStatus(closeUpdateVO);
                log.info("::{}::早盘切滚球，切自动失败玩法数据源关盘",closeLinkId);

            }
            if (CollectionUtils.isNotEmpty(sealPlayIds)) {
                JSONObject json = new JSONObject()
                        .fluentPut("tradeLevel", TradeLevelEnum.BATCH_PLAY.getLevel())
                        .fluentPut("sportId", sportId)
                        .fluentPut("matchId", matchId)
                        .fluentPut("playIdList", sealPlayIds)
                        .fluentPut("status", TradeStatusEnum.SEAL.getStatus())
                        .fluentPut("linkedType", LinkedTypeEnum.LIVE.getCode())
                        .fluentPut("remark", "早盘切滚球，玩法切自动，失败玩法封盘")
                        .fluentPut("matchType", matchType);
                Request<JSONObject> request = new Request<>();
                request.setData(json);
                request.setLinkId(linkId + "_playSeal");
                request.setDataSourceTime(System.currentTimeMillis());

                // TODO 向上游推送位置状态
                List<MarketPlaceDtlDTO> placeList = new ArrayList<>(sealPlayIds.size() * RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT);
                sealPlayIds.forEach(pId -> {
                    if (RcsConstant.isPlaceholderPlay(sportId, pId)) {
                        MarketPlaceDtlDTO placeStatusDto = apiService.getPlaceStatusDto(pId, null, -1, TradeStatusEnum.SEAL.getStatus());
                        placeList.add(placeStatusDto);
                    } else {
                        List<MarketPlaceDtlDTO> placeStatusDtoList = apiService.getAllPlaceStatusDto(pId, null, TradeStatusEnum.SEAL.getStatus());
                        placeList.addAll(placeStatusDtoList);
                    }
                });
                apiService.putTradeMarketPlaceConfig(matchId, placeList, request.getLinkId());

                producerSendMessageUtils.sendMessage(MqConstant.Topic.RCS_TRADE_UPDATE_MARKET_STATUS, String.valueOf(matchId), request.getLinkId(), request);
            }
        }
        return switchErrorPlayList;
    }

    /**
     * 篮球A+模式玩法、L模式玩法清除水差和盘口差
     *
     * @param sportId
     * @param matchId
     */
    private void basketballClear(Long sportId, Long matchId) {
        if (SportIdEnum.BASKETBALL.isNo(sportId)) {
            return;
        }
        // A+处理多个玩法
        Set<Long> playIds = Sets.newHashSet(Basketball.LIVE_AUTO_PLUS_PLAY);
        for (Basketball.Linkage linkage : Basketball.Linkage.values()) {
            playIds.add(linkage.getTotalT1());
            playIds.add(linkage.getTotalT2());
            // 删除联动模式标志，进入滚球后重新计算
            linkageCommonService.delLinkageCache(matchId, linkage);
        }
        try {
            rcsMatchMarketConfigService.clearWaterDiff(matchId, playIds);
            rcsMatchPlayConfigService.clearMarketHeadGap(matchId, playIds);
        } catch (Throwable t) {
            log.error("::{}::早盘切滚球，篮球清除水差和盘口差异常{}",CommonUtil.getRequestId(),t.getMessage(), t);
        }
    }

    /**
     * 篮球切换A+模式
     *
     * @param sportId
     * @param matchId
     * @param matchType
     */
    private void basketballSwitchAutoPlus(Long sportId, Long matchId, Integer matchType) {
        if (SportIdEnum.BASKETBALL.isNo(sportId)) {
            return;
        }
        // 玩法切换成A+
        String uuid = CommonUtils.getUUID();
        for (Long playId : Basketball.LIVE_AUTO_PLUS_PLAY) {
            MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
            updateVO.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
            updateVO.setSportId(sportId);
            updateVO.setMatchId(matchId);
            updateVO.setCategoryId(playId);
            updateVO.setTradeType(TradeEnum.AUTOADD.getCode());
            updateVO.setLinkedType(LinkedTypeEnum.LIVE.getCode());
            updateVO.setRemark("早盘切滚球，玩法切A+模式：" + uuid);
            updateVO.setMatchType(matchType);
            updateVO.setIsSeal(YesNoEnum.N.getValue());
            String tag = matchId + "_" + playId;
            String key = tag + "_" + uuid;
            producerSendMessageUtils.sendMessage("RCS_MARKET_TRADE_TYPE", tag, key, updateVO);
        }
    }

    /**
     * 篮球切换L模式
     *
     * @param sportId
     * @param matchId
     * @param matchType
     */
    private void basketballSwitchLinkage(Long sportId, Long matchId, Integer matchType) {
        if (SportIdEnum.BASKETBALL.isNo(sportId)) {
            return;
        }
        // 玩法切换成L
        Set<Long> playIds = Sets.newHashSet();
        for (Basketball.Linkage linkage : Basketball.Linkage.values()) {
            playIds.add(linkage.getTotalT1());
            playIds.add(linkage.getTotalT2());
        }
        String uuid = CommonUtils.getUUID();
        for (Long playId : playIds) {
            MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
            updateVO.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
            updateVO.setSportId(sportId);
            updateVO.setMatchId(matchId);
            updateVO.setCategoryId(playId);
            updateVO.setTradeType(TradeEnum.LINKAGE.getCode());
            updateVO.setLinkedType(LinkedTypeEnum.LIVE.getCode());
            updateVO.setRemark("早盘切滚球，玩法切L模式：" + uuid);
            updateVO.setMatchType(matchType);
            updateVO.setIsSeal(YesNoEnum.N.getValue());
            String tag = matchId + "_" + playId;
            String key = tag + "_" + uuid;
            producerSendMessageUtils.sendMessage("RCS_MARKET_TRADE_TYPE", tag, key, updateVO);
        }
    }

    @Override
    public String updateTradeStatusEvent(MarketStatusUpdateVO updateVO, MatchPeriod matchPeriod) {
        String linkId = updateVO.generateLinkId("status");
        log.info("::{}::比分事件触发操盘状态改变：updateVO={},matchPeriod={}",linkId, JSON.toJSONString(updateVO), JSON.toJSONString(matchPeriod));
        getSportId(updateVO);
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        List<Long> playIdList = updateVO.getCategoryIdList();

        Map<Long, Long> playMap = Maps.newHashMap();
        List<RcsTradeConfig> configList = Lists.newArrayList();
        List<MarketPlaceDtlDTO> mainPlayStatusList = new ArrayList<>(playIdList.size());
        List<MarketPlaceDtlDTO> placeList = new ArrayList<>(playIdList.size() * RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT);
        playIdList.forEach(playId -> {
            // 占位符玩法主玩法封盘，子玩法关盘
            if (SportIdEnum.FOOTBALL.isYes(sportId) && !Lists.newArrayList(Basketball.FIFTEEN_MINUTE_PLAY).contains(playId)) {
                // 足球才玩法封盘
                RcsTradeConfig config = new RcsTradeConfig();
                config.setMatchId(matchId.toString());
                config.setTraderLevel(TradeLevelEnum.PLAY.getLevel());
                config.setTargerData(String.valueOf(playId));
                config.setStatus(TradeStatusEnum.SEAL.getStatus());
                config.setSourceType(updateVO.getLinkedType());
                config.setUpdateUser(String.valueOf(updateVO.getUpdateUserId()));
                configList.add(config);
                MarketPlaceDtlDTO placeStatusDto = apiService.getPlaceStatusDto(playId, null, -1, TradeStatusEnum.SEAL.getStatus());
                mainPlayStatusList.add(placeStatusDto);
            }
            Long subPlayId = getSubPlayId(sportId, matchPeriod, playId);
            RcsTradeConfig tradeConfig = new RcsTradeConfig();
            tradeConfig.setMatchId(matchId.toString());
            tradeConfig.setTraderLevel(TradeLevelEnum.PLAY.getLevel());
            tradeConfig.setTargerData(String.valueOf(playId));
            tradeConfig.setSubPlayId(subPlayId);
            tradeConfig.setStatus(TradeStatusEnum.CLOSE.getStatus());
            tradeConfig.setSourceType(updateVO.getLinkedType());
            tradeConfig.setUpdateUser(String.valueOf(updateVO.getUpdateUserId()));
            configList.add(tradeConfig);
            List<MarketPlaceDtlDTO> placeStatusDtoList = apiService.getAllPlaceStatusDto(playId, subPlayId, TradeStatusEnum.CLOSE.getStatus());
            placeList.addAll(placeStatusDtoList);
            playMap.put(playId, subPlayId);
        });
        rcsTradeConfigService.saveBatch(configList);
        PlaceStatusInfo placeStatusInfo = new PlaceStatusInfo();
        placeStatusInfo.setMainPlayStatusList(mainPlayStatusList);
        placeStatusInfo.setPlaceList(placeList);
        putTradeMarketPlaceConfigAndRedis(sportId, matchId, placeStatusInfo, linkId);
        if (SportIdEnum.FOOTBALL.isYes(sportId)) {
            // 足球才玩法封盘
            // 发送MQ
            updateTradeStatusSendMq(updateVO);
        }

        Map<Long, Integer> tradeModeMap = rcsTradeConfigService.getTradeMode(matchId, playIdList);
        playIdList = playIdList.stream().filter(playId -> {
            Integer tradeMode = tradeModeMap.getOrDefault(playId, TradeEnum.AUTO.getCode());
            return !TradeEnum.isAuto(tradeMode);
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(playIdList)) {
            log.info("::{}::所有玩法都是自动，没有赔率推送",linkId);
        }
        updateVO.setCloseSubPlayMap(playMap);
        apiService.pushOdds(updateVO, playIdList, null, tradeModeMap, linkId);
        return linkId;
    }

    private Long getSubPlayId(Long sportId, MatchPeriod matchPeriod, Long playId) {
        if (SportIdEnum.FOOTBALL.isYes(sportId)) {
            if (Lists.newArrayList(336L, 28L, 30L, 109L, 110L, 31L, 222L, 148L, 357L, 362L, 363L, 364L, 365L, 366L).contains(playId)) {
                // 进球比分
                String[] score = getScore(matchPeriod.getScore());
                return playId * 100 + getGoals(score);
            } else if (Lists.newArrayList(225L, 120L, 125L, 230L).contains(playId)) {
                // 角球比分
                String[] score = getScore(matchPeriod.getCornerScore());
                return playId * 100 + getGoals(score);
            } else if (Lists.newArrayList(224L).contains(playId)) {
                // 罚牌比分
                String[] score = new String[]{"0", "0"};
                if (StringUtils.isNotBlank(matchPeriod.getYellowCardScore())) {
                    score = getScore(matchPeriod.getYellowCardScore());
                }
                if (StringUtils.isNotBlank(matchPeriod.getRedCardScore())) {
                    String[] red = getScore(matchPeriod.getRedCardScore());
                    score[0] = NumberUtils.toInt(score[0]) + NumberUtils.toInt(red[0]) + "";
                    score[1] = NumberUtils.toInt(score[1]) + NumberUtils.toInt(red[1]) + "";
                }
                return playId * 100 + getGoals(score);
            } else if (Lists.newArrayList(235L).contains(playId)) {
                // 加时比分
                String[] score = getScore(matchPeriod.getExtraTimeScore());
                return playId * 100 + getGoals(score);
            } else if (Lists.newArrayList(133L, 237L).contains(playId)) {
                // 点球比分
                String[] score = getScore(matchPeriod.getPenaltyShootout());
                return playId * 100 + getGoals(score);
            } else if (Lists.newArrayList(34L, 32L, 33L, 233L, 231L, 232L).contains(playId)) {
                // 15分钟玩法
                long minute = matchPeriod.getSecondsFromStart() / 60;
                int stage;
                if (minute <= 15) {
                    stage = 1;
                } else if (minute <= 30) {
                    stage = 2;
                } else if (minute <= 45) {
                    stage = 3;
                } else if (minute <= 60) {
                    stage = 4;
                } else if (minute <= 75) {
                    stage = 5;
                } else {
                    stage = 6;
                }
                return playId * 100 + stage;
            }
        } else if (SportIdEnum.BASKETBALL.isYes(sportId)) {
            if (Lists.newArrayList(145L, 146L).contains(playId)) {
                // 小节
                int section = getSection(matchPeriod.getPeriod());
                return playId * 100 + section;
            } else if (Lists.newArrayList(201L, 214L).contains(playId)) {
                // 进球比分
                String[] score = getScore(matchPeriod.getScore());
                int maxScore = Integer.max(NumberUtils.toInt(score[0]), NumberUtils.toInt(score[1]));
                maxScore = maxScore / 5 * 5;
                return playId * 100 + maxScore;
            } else if (Lists.newArrayList(147L, 215L).contains(playId)) {
                // 小节
                int section = getSection(matchPeriod.getPeriod());
                // 进球比分
                String[] score = getScore(matchPeriod.getScore());
                int maxScore = Integer.max(NumberUtils.toInt(score[0]), NumberUtils.toInt(score[1]));
                maxScore = maxScore / 5 * 5;
                return playId * 10000 + (section * 100) + maxScore;
            }
        }
        return playId;
    }

    private int getSection(Integer period) {
        // 通过阶段获取篮球小节
        int section = 0;
        if (period != null) {
            if (period == 301 || period == 14) {
                // 第一节休息 或 第二节
                section = 1;
            } else if (period == 302 || period == 15) {
                // 第二节休息 或 第三节
                section = 2;
            } else if (period == 303 || period == 16) {
                // 第三节休息 或 第四节
                section = 3;
            } else if (period == 100 || period == 40 || period == 110) {
                // 全场结束 或 加时赛 或 加时赛结束
                section = 4;
            }
        }
        return section;
    }

    private int getGoals(String[] score) {
        return NumberUtils.toInt(score[0]) + NumberUtils.toInt(score[1]);
    }

    private String[] getScore(String score) {
        if (StringUtils.isNotBlank(score)) {
            String[] scores = score.split(":");
            if (scores.length == 2) {
                return scores;
            }
        }
        return new String[]{"0", "0"};
    }

    @Override
    public void updatePlaceStatus(Long sportId, Long matchId, Long playId, Long subPlayId, Integer placeNum, Integer placeStatus) {
        log.info("::{}::调价窗口修改位置状态：sportId={},matchId={},playId={},subPlayId={},placeNum={},placeStatus={}", matchId,sportId, matchId, playId, subPlayId, placeNum, placeStatus);
        if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
            // 占位符玩法盘口位置开关封锁，必传子玩法ID
            RcsAssert.gtZero(subPlayId, "子玩法ID[subPlayId]不能为空");
        } else {
            subPlayId = null;
        }
        // 保存配置
        RcsTradeConfig config = new RcsTradeConfig();
        config.setMatchId(matchId.toString());
        config.setTraderLevel(TradeLevelEnum.MARKET.getLevel());
        config.setTargerData(String.valueOf(placeNum));
        config.setAddition1(playId.toString());
        config.setSubPlayId(subPlayId);
        config.setStatus(placeStatus);
        config.setSourceType(LinkedTypeEnum.WINDOW.getCode());
        config.setUpdateUser(String.valueOf(TradeUserUtils.getUserIdNoException()));
        rcsTradeConfigService.save(config);
        setPlaceStatusToRedis(sportId, matchId, playId, subPlayId, placeNum, placeStatus);
    }

    @Override
    public Integer getStatus(Long sportId, Long matchId, Long playId, Integer placeStatus) {
        Integer status = placeStatus;
        Integer matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
        if (!TradeStatusEnum.isOpen(matchStatus)) {
            status = matchStatus;
        } else if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
            Integer mainPlayStatus = getPlaceholderMainPlayStatusFromRedis(matchId, playId);
            if (!TradeStatusEnum.isOpen(mainPlayStatus)) {
                status = mainPlayStatus;
            }
        }
        return status;
    }

    private Map<Integer, Integer> getAllPlaceSameStatus(Integer status) {
        Map<Integer, Integer> resultMap = Maps.newHashMap();
        // 盘口位置，最多10个位置
        for (int placeNum = 1; placeNum <= RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT; placeNum++) {
            resultMap.put(placeNum, status);
        }
        return resultMap;
    }

    private void setPlaceStatusToRedis(Long sportId, Long matchId, Long playId, Long subPlayId, Map<String, String> placeStatusMap) {
        //log.info("::{}::Redis缓存位置状态：sportId={},matchId={},playId={},subPlayId={},placeStatusMap={}",matchId, sportId, matchId, playId, subPlayId, JSON.toJSONString(placeStatusMap));
        String play;
        if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
            play = playId + "-" + subPlayId;
        } else {
            play = String.valueOf(playId);
        }
        String key = RedisKey.getPlaceStatusConfigKey(matchId, play);
        redisUtils.hmset(key, placeStatusMap);
        redisUtils.expire(key, 180L, TimeUnit.DAYS);
    }

    private void setPlaceStatusToRedis(Long sportId, Long matchId, Long playId, Long subPlayId, Integer placeNum, Integer placeStatus) {
        //log.info("::{}::Redis缓存位置状态：sportId={},matchId={},playId={},subPlayId={},placeNum={},placeStatus={}",matchId, sportId, matchId, playId, subPlayId, placeNum, placeStatus);
        String play;
        if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
            play = playId + "-" + subPlayId;
        } else {
            play = String.valueOf(playId);
        }
        String key = RedisKey.getPlaceStatusConfigKey(matchId, play);
        redisUtils.hset(key, String.valueOf(placeNum), String.valueOf(placeStatus));
        redisUtils.expire(key, 180L, TimeUnit.DAYS);
    }

    @Override
    public Map<Integer, Integer> getPlaceStatusFromRedis(Long sportId, Long matchId, Long playId, Long subPlayId) {
        String play;
        if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
            play = playId + "-" + subPlayId;
        } else {
            play = String.valueOf(playId);
        }
        String key = RedisKey.getPlaceStatusConfigKey(matchId, play);
        Map<String, String> map = redisUtils.hgetAll(key);
        //log.info("::{}::获取Redis位置状态：sportId={},matchId={},playId={},subPlayId={},key={},map={}",matchId, sportId, matchId, playId, subPlayId, key, JSON.toJSONString(map));
        if (CollectionUtils.isEmpty(map)) {
            return getAllPlaceSameStatus(TradeStatusEnum.OPEN.getStatus());
        }
        Map<Integer, Integer> resultMap = Maps.newHashMap();
        map.forEach((k, v) -> resultMap.put(NumberUtils.toInt(k), NumberUtils.toInt(v, TradeStatusEnum.OPEN.getStatus())));
        return resultMap;
    }

    @Override
    public Integer getPlaceStatusFromRedis(Long sportId, Long matchId, Long playId, Long subPlayId, Integer placeNum) {
        String play;
        if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
            play = playId + "-" + subPlayId;
        } else {
            play = String.valueOf(playId);
        }
        String key = RedisKey.getPlaceStatusConfigKey(matchId, play);
        String value = redisUtils.hget(key, String.valueOf(placeNum));
        //log.info("::{}::获取Redis位置状态：sportId={},matchId={},playId={},subPlayId={},placeNum={},key={},value={}",matchId, sportId, matchId, playId, subPlayId, placeNum, key, value);
        return NumberUtils.toInt(value, TradeStatusEnum.OPEN.getStatus());
    }

    private void setPlaceholderMainPlayStatusToRedis(Long matchId, Map<String, String> mainPlayStatusMap) {
        //log.info("::{}::缓存占位符主玩法状态：matchId={},mainPlayStatusMap={}",matchId, matchId, JSON.toJSONString(mainPlayStatusMap));
        String key = RedisKey.getPlaceholderMainPlayStatusKey(matchId);
        redisUtils.hmset(key, mainPlayStatusMap);
        redisUtils.expire(key, 180L, TimeUnit.DAYS);
    }

    private void setPlaceholderMainPlayStatusToRedis(Long matchId, Long playId, Integer mainPlayStatus) {
        //log.info("::{}::缓存占位符主玩法状态：matchId={},playId={},mainPlayStatus={}",matchId, matchId, playId, mainPlayStatus);
        String key = RedisKey.getPlaceholderMainPlayStatusKey(matchId);
        redisUtils.hset(key, String.valueOf(playId), String.valueOf(mainPlayStatus));
        redisUtils.expire(key, 180L, TimeUnit.DAYS);
    }

    @Override
    public Integer getPlaceholderMainPlayStatusFromRedis(Long matchId, Long playId) {
        String key = RedisKey.getPlaceholderMainPlayStatusKey(matchId);
        String value = redisUtils.hget(key, String.valueOf(playId));
        //log.info("::{}::获取占位符主玩法状态：matchId={},playId={},key={},value={}",matchId, matchId, playId, key, value);
        return NumberUtils.toInt(value, TradeStatusEnum.OPEN.getStatus());
    }

    @Override
    public boolean isLinkage(Long matchId, Long playId) {
        if (!Basketball.Linkage.getLinkagePlayId().contains(playId)) {
            return false;
        }
        String key = String.format(RedisKey.LINKAGE_SWITCH_FLAG, matchId, playId);
        String linkageFlag = redisUtils.get(key);
        return StringUtils.isNotBlank(linkageFlag);
    }

    @Override
    public Integer getPlaySetCodeStatus(Long sportId, Long matchId, Long playId) {
        String relationKey = RedisKey.getPlaySetCodeRelationKey(sportId);
        String playSetCode = RcsLocalCacheUtils.getValue(relationKey, String.valueOf(playId), redisUtils::hget, 10 * 60 * 1000L);
        //log.info("::{}::通过玩法ID获取玩法集编码：key={},field={},value={}",matchId, relationKey, playId, playSetCode);
        if (StringUtils.isBlank(playSetCode)) {
            return TradeStatusEnum.OPEN.getStatus();
        }
        String statusKey = RedisKey.getPlaySetCodeStatusKey(matchId);
        String value = redisUtils.hget(statusKey, playSetCode);
        //log.info("::{}::通过玩法集编码获取状态：key={},field={},value={}",matchId, statusKey, playSetCode, value);
        if (StringUtils.isBlank(value)) {
            return TradeStatusEnum.OPEN.getStatus();
        }
        return NumberUtils.toInt(value, TradeStatusEnum.OPEN.getStatus());
    }

    @Override
    public Map<String, Integer> getPlaySetCodeStatus(Long matchId) {
        String key = RedisKey.getPlaySetCodeStatusKey(matchId);
        Map<String, String> hash = redisUtils.hgetAll(key);
        //log.info("::{}::获取玩法集编码状态：key={},hash={}",matchId, key, JSON.toJSONString(hash));
        if (CollectionUtils.isEmpty(hash)) {
            return Maps.newHashMap();
        }
        Map<String, Integer> map = Maps.newHashMap();
        hash.forEach((k, v) -> map.put(k, NumberUtils.toInt(v, TradeStatusEnum.OPEN.getStatus())));
        return map;
    }

    @Override
    public Map<Long, Integer> getPlaySetCodeStatus(Long sportId, Long matchId, List<Long> playIds) {
        if (CollectionUtils.isEmpty(playIds)) {
            return Maps.newHashMap();
        }
        String relationKey = RedisKey.getPlaySetCodeRelationKey(sportId);
        Map<String, String> hash = redisUtils.hgetAll(relationKey);
        if (CollectionUtils.isEmpty(hash)) {
            return Maps.newHashMap();
        }
        Map<String, Integer> playSetCodeStatusMap = getPlaySetCodeStatus(matchId);
        Map<Long, Integer> map = Maps.newHashMapWithExpectedSize(playIds.size());
        playIds.forEach(playId -> {
            String playSetCode = hash.get(String.valueOf(playId));
            if (StringUtils.isBlank(playSetCode)) {
                map.put(playId, TradeStatusEnum.OPEN.getStatus());
            } else {
                map.put(playId, playSetCodeStatusMap.getOrDefault(playSetCode, TradeStatusEnum.OPEN.getStatus()));
            }
        });
        return map;
    }

    private void putPlaySetCodeStatusConfig(MarketStatusUpdateVO updateVO) {
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        String playSetCode = updateVO.getPlaySetCode();
        List<Long> playIds = updateVO.getCategoryIdList();
        Integer status = updateVO.getMarketStatus();
        Map<String, String> hash = Maps.newHashMapWithExpectedSize(playIds.size());
        playIds.forEach(playId -> hash.put(String.valueOf(playId), playSetCode));
        // 缓存玩法集和玩法映射关系
        String relationKey = RedisKey.getPlaySetCodeRelationKey(sportId);
        redisUtils.hmset(relationKey, hash);
        redisUtils.expire(relationKey, 180L, TimeUnit.DAYS);
        PlaySetStatusConfigDTO config = new PlaySetStatusConfigDTO();
        config.setSportId(sportId);
        config.setMatchId(matchId);
        config.setPlaySetCode(playSetCode);
        config.setStatus(status);
        config.setPlayIds(playIds);
        // 向融合推送玩法集编码状态
        apiService.putCategoryStatusConfig(config, updateVO.getLinkId());
        // 缓存玩法集编码状态
        String statusKey = RedisKey.getPlaySetCodeStatusKey(matchId);
        redisUtils.hset(statusKey, playSetCode, String.valueOf(status));
        redisUtils.expire(statusKey, 90L, TimeUnit.DAYS);
    }

    @Override
    public void handlePushStatus(Long sportId, Long matchId, Long playId, List<StandardMarketDTO> marketList, Integer matchStatus, Integer tradeMode, Integer sourceCloseFlag, Integer operateSource, Integer endFlag) {
        log.info("::{}::处理推送状态：sportId={},matchId={},playId={},marketList={},matchStatus={},tradeMode={},sourceCloseFlag={},operateSource={},endFlag={}",matchId, sportId, matchId, playId, JSON.toJSONString(marketList), matchStatus, tradeMode, sourceCloseFlag, operateSource, endFlag);
        if (CollectionUtils.isEmpty(marketList)) {
            return;
        }
        // 赛事挡板状态
        if (matchStatus == null) {
            matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
        }
        boolean matchIsStop = TradeStatusEnum.isEnd(matchStatus);
        // 足球玩法集挡板状态
        if (SportIdEnum.FOOTBALL.isYes(sportId) && TradeStatusEnum.isOpen(matchStatus)) {
            matchStatus = getPlaySetCodeStatus(sportId, matchId, playId);
        }
        if (YesNoEnum.isYes(sourceCloseFlag)) {
            // 数据源关盘
            marketList.forEach(market -> {
                market.setStatus(TradeStatusEnum.CLOSE.getStatus());
                market.setPlaceNumStatus(TradeStatusEnum.CLOSE.getStatus());
                market.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
                market.setRemark("数据源关盘");
                // 是否收盘
                if (matchIsStop || YesNoEnum.isYes(endFlag)) {
                    market.setEndEdStatus(YesNoEnum.Y.getValue());
                } else {
                    market.setEndEdStatus(YesNoEnum.N.getValue());
                }
            });
            return;
        }
        if (!TradeEnum.checkTradeType(tradeMode)) {
            tradeMode = rcsTradeConfigService.getDataSource(matchId, playId);
        }
        if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
            // 带X玩法 总玩法挡板状态
            if (TradeStatusEnum.isOpen(matchStatus)) {
                matchStatus = getPlaceholderMainPlayStatusFromRedis(matchId, playId);
            }
            Map<Long, List<StandardMarketDTO>> groupMap = marketList.stream().collect(Collectors.groupingBy(StandardMarketDTO::getChildStandardCategoryId));
            for (Map.Entry<Long, List<StandardMarketDTO>> entry : groupMap.entrySet()) {
                Long subPlayId = entry.getKey();
                List<StandardMarketDTO> list = entry.getValue();
                Map<Integer, Integer> placeStatusMap = getPlaceStatusFromRedis(sportId, matchId, playId, subPlayId);
                handlePushStatus(matchId, list, matchStatus, tradeMode, placeStatusMap, operateSource, sportId);
            }
        } else {
            Map<Integer, Integer> placeStatusMap = getPlaceStatusFromRedis(sportId, matchId, playId, null);
            handlePushStatus(matchId, marketList, matchStatus, tradeMode, placeStatusMap, operateSource, sportId);
        }
    }

    private void handlePushStatus(Long matchId, List<StandardMarketDTO> marketList, Integer matchStatus, Integer tradeMode, Map<Integer, Integer> placeStatusMap, Integer operateSource, Long sportId) {
        log.info("::{}::处理单个玩法推送状态：matchId={},marketList={},matchStatus={},tradeMode={},placeStatusMap={},operateSource={},sportId={}",
                matchId, matchId, JSON.toJSONString(marketList), matchStatus, tradeMode, JSON.toJSONString(placeStatusMap), operateSource, sportId);
        marketList.forEach(market -> {
            Integer placeStatus = placeStatusMap.getOrDefault(market.getPlaceNum(), TradeStatusEnum.OPEN.getStatus());
            Long playId = market.getMarketCategoryId();
            market.setPlaceNumStatus(placeStatus);
            if (!TradeEnum.isAuto(tradeMode)) {
                List<StandardMarketOddsDTO> marketOddsList = market.getMarketOddsList();
                if (CollectionUtils.isNotEmpty(marketOddsList)) {
                    marketOddsList.forEach(marketOdds -> {
                        // 非自动模式，投注项激活状态为2时，修改为1
                        if (NumberUtils.INTEGER_TWO.equals(marketOdds.getActive())) {
                            marketOdds.setActive(1);
                        }
                        // 非自动模式，操盘手开盘激活投注项
                        if (NumberUtils.INTEGER_ONE.equals(operateSource)) {
                            if (TradeStatusEnum.isOpen(placeStatus) && TradeStatusEnum.isOpen(matchStatus)) {
                                marketOdds.setActive(1);
                            }
                        }
                    });
                }
            }

            market.setEndEdStatus(YesNoEnum.N.getValue());
            if (TradeStatusEnum.isOpen(matchStatus)) {
                market.setStatus(placeStatus);
            } else if (TradeStatusEnum.isEnd(matchStatus)) {
                market.setStatus(TradeStatusEnum.CLOSE.getStatus());
                market.setEndEdStatus(YesNoEnum.Y.getValue());
                return;
            } else {
                market.setStatus(matchStatus);
            }

            if (TradeEnum.isAutoAdd(tradeMode)) {
                // A+模式状态跟着数据源走
                //三方盘口源状态
                Integer sourceStatus = market.getThirdMarketSourceStatus();
                //盘口状态状态判断
                if(!TradeStatusEnum.isOpen(sourceStatus)){
                    market.setStatus(sourceStatus);
                }

                //单双玩法下A+模式数据源封盘不允许手动开盘
                if(!TradeConstant.BASKETBALL_SINGLE_DOUBLE_PLAY.contains(playId)){
                    // A+模式数据源封盘也可以让操盘手开盘
                    if (NumberUtils.INTEGER_ONE.equals(operateSource)) {
                        if (TradeStatusEnum.isOpen(placeStatus) && TradeStatusEnum.isOpen(matchStatus)) {
                            market.setStatus(placeStatus);
                            market.setPlaceNumStatus(placeStatus);
                            market.setThirdMarketSourceStatus(placeStatus);
                        }
                    }
                }
            } else if (TradeEnum.isManual(tradeMode)) {
                // 手动数据源状态设为开
                market.setThirdMarketSourceStatus(TradeStatusEnum.OPEN.getStatus());
            } else if (TradeEnum.isLinkage(tradeMode)) {
                Long subPlayId = market.getChildStandardCategoryId();
                if (subPlayId == null) {
                    subPlayId = playId;
                }
                // 状态为开时需要联动
                if (TradeStatusEnum.isOpen(placeStatus) && TradeStatusEnum.isOpen(matchStatus)) {
                    Basketball.Linkage linkage = Basketball.Linkage.getByTargetSubPlayId(subPlayId);
                    if (linkage != null) {
                        Map<Long, StandardSportMarket> mainMarketInfoMap = linkageCommonService.getMainMarketInfo(matchId, linkage, false);
                        StandardSportMarket handicapMainMarket = mainMarketInfoMap.get(linkage.getHandicap());
                        StandardSportMarket totalMainMarket = mainMarketInfoMap.get(linkage.getTotal());
                        Integer status = linkageCommonService.getLinkageStatus(matchId, handicapMainMarket, totalMainMarket);
                        Integer sourceStatus = linkageCommonService.getLinkageSourceStatus(handicapMainMarket, totalMainMarket);
                        market.setThirdMarketSourceStatus(sourceStatus);
                        market.setPlaceNumStatus(status);
                        market.setStatus(status);
                    }
                }
                String key = RedisKey.getAutoCloseStatusKey(matchId);
                Map<String, String> map = redisUtils.hgetAll(key);
                if (map != null && StringUtils.isNotBlank(map.get(playId.toString()))) {
                    log.info("::{}::自动关盘的玩法不能再开",matchId);
                    market.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
                }
            }
        });
        log.info("::{}::处理推送状态：marketList={}",matchId, JSON.toJSONString(marketList));
    }

    private void updateTradeStatusSendMq(MarketStatusUpdateVO updateVO) {
        log.info("::{}::修改状态发送消息队列：" + JSON.toJSONString(updateVO),updateVO.getLinkId());
        Integer tradeLevel = updateVO.getTradeLevel();
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        Integer status = updateVO.getMarketStatus();
        if (TradeLevelEnum.isMatchLevel(tradeLevel)) {
            // 发送MQ，更新MongoDB
            MatchMarketTradeTypeVo mongoMq = new MatchMarketTradeTypeVo()
                    .setLinkId(updateVO.getLinkId() + "_MongoDB")
                    .setSportId(sportId)
                    .setMatchId(matchId)
                    .setLevel(tradeLevel)
                    .setStatus(status)
                    .setNewFlag(updateVO.getNewFlag());
            producerSendMessageUtils.sendMessage(MqConstants.MARKET_CONGIG_UPDTAE_TOPIC, matchId.toString(), mongoMq.getLinkId(), mongoMq);
            // 发送MQ，推送WS到前端
            MatchStatusAndDataSuorceVo wsMq = BeanFactory.matchTradeStatusWsInfo(sportId, matchId, status, updateVO.getLinkId() + "_WS");
            producerSendMessageUtils.sendMessage(MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TOPIC, MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TAG, wsMq.getLinkId(), wsMq);
            return;
        }

        if (TradeLevelEnum.isPlayLevel(tradeLevel) && LinkedTypeEnum.CHU_ZHANG.getCode().equals(updateVO.getLinkedType())) {
            // WS推送货量出涨预警标志到前端
            wsPushChuZhangWarnSign(updateVO);
            return;
        }

        if (TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            // 发送MQ，推送WS到前端
            MatchStatusAndDataSuorceVo wsMq = BeanFactory.playSetCodeTradeStatusWsInfo(sportId, matchId, updateVO.getPlaySetCode(), status, updateVO.getCategoryIdList(), updateVO.getLinkId() + "_WS");
            producerSendMessageUtils.sendMessage(MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TOPIC, MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TAG, wsMq.getLinkId(), wsMq);
        }

        Map<Long, Integer> mainPlayStatusMap = Maps.newHashMap();
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            Long playId = updateVO.getCategoryId();
            Long subPlayId = updateVO.getSubPlayId();
            if (RcsConstant.isPlaceholderPlay(sportId, playId) && subPlayId == null) {
                mainPlayStatusMap.put(playId, status);

            }
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel) || TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            List<Long> placeholderPlayIds = updateVO.getPlaceholderPlayIds();
            if (CollectionUtils.isNotEmpty(placeholderPlayIds)) {
                placeholderPlayIds.forEach(pId -> mainPlayStatusMap.put(pId, status));
            }
        } else if (TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            List<Long> placeholderPlayIds = updateVO.getSealPlaceholderPlayIds();
            if (CollectionUtils.isNotEmpty(placeholderPlayIds)) {
                placeholderPlayIds.forEach(pId -> mainPlayStatusMap.put(pId, status));
            }
        } else if (TradeLevelEnum.isScoreEvent(tradeLevel)) {
            List<Long> playIdList = updateVO.getCategoryIdList();
            if (CollectionUtils.isNotEmpty(playIdList)) {
                playIdList.forEach(pId -> {
                    if (!Lists.newArrayList(Basketball.FIFTEEN_MINUTE_PLAY).contains(pId)) {
                        mainPlayStatusMap.put(pId, TradeStatusEnum.SEAL.getStatus());
                    }
                });
            }
        }

        if (CollectionUtils.isNotEmpty(mainPlayStatusMap)) {
            // 发送MQ，更新MongoDB
            MatchMarketTradeTypeVo mongoMq = new MatchMarketTradeTypeVo()
                    .setLinkId(updateVO.getLinkId() + "_MongoDB")
                    .setSportId(sportId)
                    .setMatchId(matchId)
                    .setMainPlayStatusMap(mainPlayStatusMap);
            producerSendMessageUtils.sendMessage(MqConstants.MARKET_CONGIG_UPDTAE_TOPIC, matchId.toString(), mongoMq.getLinkId(), mongoMq);
            // 发送MQ，推送WS到前端
            Map<String, Integer> map = Maps.newHashMap();
            mainPlayStatusMap.forEach((playId, mainPlayStatus) -> map.put(String.valueOf(playId), mainPlayStatus));
            MatchStatusAndDataSuorceVo wsMq = BeanFactory.mainPlayTradeStatusWsInfo(sportId, matchId, map, updateVO.getLinkId() + "_WS");
            producerSendMessageUtils.sendMessage(MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TOPIC, MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TAG, wsMq.getLinkId(), wsMq);
        }
    }

    private void wsPushChuZhangWarnSign(MarketStatusUpdateVO updateVO) {
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        String playKey;
        Long playId = updateVO.getCategoryId();
        Long subPlayId = updateVO.getSubPlayId();
        if (RcsConstant.isPlaceholderPlay(sportId, playId)) {
            playKey = playId + "_" + subPlayId;
        } else {
            playKey = playId + "_" + playId;
        }
        Map<String, String> chuZhangWarnSignMap = Maps.newHashMap();
        chuZhangWarnSignMap.put(matchId.toString(), YesNoEnum.Y.getCode());
        chuZhangWarnSignMap.put(playKey, YesNoEnum.Y.getCode());
        MatchStatusAndDataSuorceVo wsMq = BeanFactory.chuZhangWarnSignWsInfo(sportId, matchId, chuZhangWarnSignMap, updateVO.getLinkId() + "_WS");
        producerSendMessageUtils.sendMessage(MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TOPIC, MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TAG, wsMq.getLinkId(), wsMq);
        // 缓存货量出涨预警标志
        String key = RedisKey.getChuZhangWarnSignKey(matchId, updateVO.getMatchType());
        redisUtils.hmset(key, chuZhangWarnSignMap);
        redisUtils.expire(key, 180L, TimeUnit.DAYS);
        log.info("::{}::缓存货量出涨预警标志：key={},hashMap={}",updateVO.getLinkId(), key, JSON.toJSONString(chuZhangWarnSignMap));
    }

    /**
     * 角球展示 发送到业务
     *
     * @param sportId
     * @param matchId
     * @param status
     */
    private void sendMqCornerShow(Long sportId, Long matchId, Integer status, String linkId) {
        log.info("::{}::角球展示，发送到业务：sportId={},matchId={},status={}",linkId, sportId, matchId, status);
        JSONObject json = new JSONObject();
        json.put("matchId", matchId);
        json.put("sportId", sportId);
        json.put("cornerShow", TradeStatusEnum.isOpen(status));
        linkId = linkId + "_show";
        JSONObject data = new JSONObject();
        data.put("linkId", linkId);
        data.put("data", json);
        producerSendMessageUtils.sendMessage(MqConstant.RCS_MATCH_CATEGORYSET_SHOW, String.valueOf(matchId), linkId, data);
    }

    private void playGroup(MarketStatusUpdateVO updateVO) {
        log.info("::{}::玩法分组前：" + JSON.toJSONString(updateVO),updateVO.getLinkId());
        Integer tradeLevel = updateVO.getTradeLevel();
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            Long playSetId = updateVO.getCategorySetId();
            // 通过玩法集ID查询所有下级玩法ID
            List<Long> playIds = rcsMarketCategorySetRelationService.getCategoryIdByCategorySetId(playSetId);
            updateVO.setCategoryIdList(playIds);
            if (CollectionUtils.isEmpty(playIds)) {
                log.warn("::{}::玩法集下无玩法：playSetId={}", matchId, playSetId);
                throw new RcsServiceException("玩法集下无玩法");
            }
        }
        if (TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            String playSetCode = updateVO.getPlaySetCode();
            List<Long> playIds = rcsMarketCategorySetRelationService.getPlayIdByPlaySetCode(playSetCode);
            updateVO.setCategoryIdList(playIds);
            if (CollectionUtils.isEmpty(playIds)) {
                log.warn("::{}::玩法集编码下无玩法：playSetCode={}", matchId, playSetCode);
                throw new RcsServiceException("玩法集编码下无玩法");
            }
        }
        if (TradeLevelEnum.isPlaySetLevel(tradeLevel) || TradeLevelEnum.isBatchPlayLevel(tradeLevel) || TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            List<Long> playIdList = updateVO.getCategoryIdList();
            if (CollectionUtils.isNotEmpty(playIdList)) {
                List<Long> normalPlayIds = Lists.newArrayList(playIdList);
                List<Long> placeholderPlayIds = RcsConstant.getPlaceholderPlayIds(sportId);
                // 取交集得到占位符玩法
                placeholderPlayIds.retainAll(playIdList);
                if (CollectionUtils.isNotEmpty(placeholderPlayIds)) {
                    // 移除占位符玩法得到常规玩法
                    normalPlayIds.removeAll(placeholderPlayIds);
                    // 早盘切滚球，玩法集开盘处理，过滤切换自动失败的玩法
                    if (TradeLevelEnum.isPlaySetLevel(tradeLevel) &&
                            LinkedTypeEnum.LIVE.getCode().equals(updateVO.getLinkedType()) &&
                            CollectionUtils.isNotEmpty(updateVO.getSwitchErrorPlayList())) {
                        placeholderPlayIds.removeAll(updateVO.getSwitchErrorPlayList());
                    }
                }
                if (SportIdEnum.FOOTBALL.isYes(sportId) && updateVO.getMatchType() == 0 && TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
                    // 足球 滚球 玩法集编码级别
                    if (LinkedTypeEnum.EVENT.getCode().equals(updateVO.getLinkedType()) && !TradeStatusEnum.isOpen(updateVO.getMarketStatus())) {
                        // 进球（比分变动）封盘，M模式让球玩法需要玩法级别封盘
                        Map<Long, Integer> tradeModeMap = rcsTradeConfigService.getTradeMode(matchId, playIdList);
                        updateVO.setSealNormalPlayIds(getManualSealPlayIds(tradeModeMap, normalPlayIds));
                        updateVO.setSealPlaceholderPlayIds(getManualSealPlayIds(tradeModeMap, placeholderPlayIds));
                    }
                }
                updateVO.setNormalPlayIds(normalPlayIds);
                updateVO.setPlaceholderPlayIds(placeholderPlayIds);
            }
        }
        log.info("::{}::玩法分组后：" + JSON.toJSONString(updateVO),updateVO.getLinkId());
    }

    private List<Long> getManualSealPlayIds(Map<Long, Integer> tradeModeMap, List<Long> playIds) {
        if (CollectionUtils.isEmpty(tradeModeMap) || CollectionUtils.isEmpty(playIds)) {
            return Lists.newArrayList();
        }
        List<Long> sealPlayIds = Lists.newArrayList();
        tradeModeMap.forEach((playId, status) -> {
            if (TradeEnum.isManual(status) && playIds.contains(playId) && TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(playId.intValue())) {
                // M模式 让球
                sealPlayIds.add(playId);
            }
        });
        return sealPlayIds;
    }

    private void getSportId(MarketStatusUpdateVO updateVO) {
        Long matchId = updateVO.getMatchId();
        if (updateVO.getSportId() == null || updateVO.getMatchType() == null) {
            StandardMatchInfo matchInfo = standardMatchInfoService.getById(matchId);
            if (matchInfo == null) {
                throw new RcsServiceException("赛事不存在：" + matchId);
            }
            updateVO.setSportId(matchInfo.getSportId());
            updateVO.setMatchType(RcsConstant.getMatchType(matchInfo));
        }
    }
}
