package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.panda.merge.api.ConfigMarketOddsStatusApi;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.api.ITradeMarketOddsApi;
import com.panda.merge.dto.ConfigMarketOddsStatusDTO;
import com.panda.merge.dto.MarketPlaceDtlDTO;
import com.panda.merge.dto.PlaySetStatusConfigDTO;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.StandardMatchMarketDTO;
import com.panda.merge.dto.TradeMarketConfigDTO;
import com.panda.merge.dto.TradeMarketPlaceConfigDTO;
import com.panda.merge.dto.UpdateTradeTypeDTO;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.enums.MatchLevelEnum;
import com.panda.sport.rcs.enums.ScoreTypeEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoDetailService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketOddsService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 融合api调用
 * @Author : Paca
 * @Date : 2021-07-24 20:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class ApiService {

    @Reference(check = false, lazy = true, retries = 1, timeout = 10000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Reference(check = false, lazy = true, retries = 1, timeout = 10000)
    private ITradeMarketOddsApi tradeMarketOddsApi;
    @Reference(check = false, lazy = true, retries = 1, timeout = 10000)
    private ConfigMarketOddsStatusApi configMarketOddsStatusApi;

    @Autowired
    protected StandardSportMarketService standardSportMarketService;
    @Autowired
    protected StandardSportMarketOddsService standardSportMarketOddsService;
    @Autowired
    protected RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    @Autowired
    private MatchStatisticsInfoDetailService matchStatisticsInfoDetailService;

    @Autowired
    private TradeStatusService tradeStatusService;

    public Response putTradeMarketConfig(TradeMarketConfigDTO tradeMarketConfigDTO, String linkId) {
        if (StringUtils.isBlank(linkId)) {
            linkId = CommonUtils.getLinkId();
        }
        linkId += "_config";
        return DataRealtimeApiUtils.handleApi(uniqueLinkId(linkId), tradeMarketConfigDTO, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return tradeMarketConfigApi.putTradeMarketConfig(request);
            }
        });
    }

    public Response putTradeMarketPlaceConfig(Long matchId, List<MarketPlaceDtlDTO> marketPlaceList, String linkId) {
        if (StringUtils.isBlank(linkId)) {
            linkId = CommonUtils.getLinkId();
        }
        linkId += "_place";
        TradeMarketPlaceConfigDTO tradeMarketPlaceConfigDTO = new TradeMarketPlaceConfigDTO();
        tradeMarketPlaceConfigDTO.setStandardMatchInfoId(matchId);
        tradeMarketPlaceConfigDTO.setMarketPlaceDtlDTOList(marketPlaceList);
        return DataRealtimeApiUtils.handleApi(uniqueLinkId(linkId), tradeMarketPlaceConfigDTO, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return tradeMarketConfigApi.putTradeMarketPlaceConfig(request);
            }
        });
    }

    public Response putTradeMarketOdds(Long matchId, List<StandardMarketDTO> marketList, String linkId) {
        if (CollectionUtils.isEmpty(marketList)) {
            return null;
        }
        marketList.forEach(market -> {
            if (market.getPlaceNumStatus() == null) {
                market.setPlaceNumStatus(TradeStatusEnum.OPEN.getStatus());
            }
            List<StandardMarketOddsDTO> marketOddsList = market.getMarketOddsList();
            if (CollectionUtils.isNotEmpty(marketOddsList)) {
                marketOddsList.forEach(marketOdds -> {
                    // 非自动模式，投注项激活状态为2时，修改为1
                    if (NumberUtils.INTEGER_TWO.equals(marketOdds.getActive())) {
                        marketOdds.setActive(1);
                    }
                });
            }
        });
        if (StringUtils.isBlank(linkId)) {
            linkId = CommonUtils.getLinkId();
        }
        linkId += "_odds";
        StandardMatchMarketDTO standardMatchMarketDTO = new StandardMatchMarketDTO();
        standardMatchMarketDTO.setStandardMatchInfoId(matchId);
        standardMatchMarketDTO.setMarketList(marketList);
        try {
            return DataRealtimeApiUtils.handleApi(uniqueLinkId(linkId), standardMatchMarketDTO, new DataRealtimeApiUtils.ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return tradeMarketOddsApi.putTradeMarketOdds(request);
                }
            });
        } catch (Exception e) {
            log.error("::{}::推送赔率异常{}" + linkId,e.getMessage(), e);
        }
        return null;
    }

    public Response putTradeTypeConfig(UpdateTradeTypeDTO config, String linkId) {
        if (StringUtils.isBlank(linkId)) {
            linkId = CommonUtils.getLinkId();
        }
        linkId += "_mode";
        return DataRealtimeApiUtils.handleApi(uniqueLinkId(linkId), config, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return tradeMarketConfigApi.putTradeTypeConfig(request);
            }
        });
    }

    public Response putCategoryStatusConfig(PlaySetStatusConfigDTO config, String linkId) {
        if (StringUtils.isBlank(linkId)) {
            linkId = CommonUtils.getLinkId();
        }
        linkId += "_playSetStatus";
        return DataRealtimeApiUtils.handleApi(uniqueLinkId(linkId), config, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return tradeMarketConfigApi.putCategoryStatusConfig(request);
            }
        });
    }

    public Response updateOddsMode(ConfigMarketOddsStatusDTO config, String linkId) {
        if (StringUtils.isBlank(linkId)) {
            linkId = CommonUtils.getLinkId();
        }
        linkId += "_oddsMode";
        return DataRealtimeApiUtils.handleApi(linkId, config, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return configMarketOddsStatusApi.update(request);
            }
        });
    }

    public TradeMarketConfigDTO generateTradeMarketConfigDTO(MarketStatusUpdateVO updateVO) {
        Integer tradeLevel = updateVO.getTradeLevel();
        Long matchId = updateVO.getMatchId();
        Long playId = updateVO.getCategoryId();
        List<Long> playIdList = updateVO.getCategoryIdList();
        String marketId = updateVO.getMarketId();
        Integer userId = updateVO.getUpdateUserId();
        TradeMarketConfigDTO configDTO = new TradeMarketConfigDTO();
        configDTO.setConfigId(generateConfigId(tradeLevel, matchId));
        configDTO.setLevel(generateLevel(tradeLevel));
        configDTO.setTargetId(matchId.toString());
        configDTO.setSourceSystem(2);
        configDTO.setActive(1);
        configDTO.setOperaterId(userId.longValue());
        configDTO.setModifyTime(System.currentTimeMillis());
        if (TradeLevelEnum.isPlayLevel(tradeLevel) ||
                TradeLevelEnum.isMarketLevel(tradeLevel) ||
                TradeLevelEnum.isPlaySetLevel(tradeLevel) ||
                TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            // 玩法级别、盘口级别、玩法集需传值addition1
            configDTO.setAddition1(generateIdString(tradeLevel, matchId, playId, playIdList, marketId));
        }
        return configDTO;
    }

    /**
     * 生成 configId
     *
     * @param tradeLevel
     * @param matchId
     * @return
     * @see com.panda.merge.dto.TradeMarketConfigDTO
     */
    private String generateConfigId(Integer tradeLevel, Long matchId) {
        if (TradeLevelEnum.isPlayLevel(tradeLevel) ||
                TradeLevelEnum.isPlaySetLevel(tradeLevel) ||
                TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            // 注意：玩法、玩法集需要拼接赛事ID
            return RcsConstant.PLAY_ID + matchId;
        }
        if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            // 注意：盘口需要拼接赛事ID
            return RcsConstant.MARKET_ID + matchId;
        }
        return RcsConstant.MATCH_ID + matchId;
    }

    /**
     * 生成 level，0：全部，1：玩法，2：联赛，3：赛事，4：盘口
     *
     * @param tradeLevel
     * @return
     * @see com.panda.merge.dto.TradeMarketConfigDTO
     */
    private int generateLevel(Integer tradeLevel) {
        if (TradeLevelEnum.isMatchLevel(tradeLevel)) {
            return MatchLevelEnum.MATCH.getLevel();
        }
        if (TradeLevelEnum.isPlayLevel(tradeLevel) ||
                TradeLevelEnum.isPlaySetLevel(tradeLevel) ||
                TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            return MatchLevelEnum.PLAY.getLevel();
        }
        if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            return MatchLevelEnum.MATCH_MARKET.getLevel();
        }
        return -1;
    }

    /**
     * 根据tradeLevel返回字符串ID，赛事ID、玩法ID、盘口ID、玩法ID集合（“,”号分隔）
     *
     * @param tradeLevel
     * @param matchId
     * @param playId
     * @param playIdList
     * @param marketId
     * @return
     */
    public String generateIdString(Integer tradeLevel, Long matchId, Long playId, List<Long> playIdList, String marketId) {
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            return String.valueOf(playId);
        }
        if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            return marketId;
        }
        if (TradeLevelEnum.isPlaySetLevel(tradeLevel) || TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            return StringUtils.join(playIdList, ",");
        }
        return matchId.toString();
    }

    public MarketPlaceDtlDTO getPlaceStatusDto(Long playId, Long subPlayId, Integer placeNum, Integer status) {
        MarketPlaceDtlDTO placeDTO = new MarketPlaceDtlDTO();
        placeDTO.setStandardCategoryId(playId);
        placeDTO.setChildStandardCategoryId(subPlayId);
        placeDTO.setPlaceNum(placeNum);
        placeDTO.setPlaceNumStatus(String.valueOf(status));
        return placeDTO;
    }

    public List<MarketPlaceDtlDTO> getAllPlaceStatusDto(Long playId, Long subPlayId, Integer status) {
        List<MarketPlaceDtlDTO> list = Lists.newArrayList();
        for (int placeNum = 1; placeNum <= RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT; placeNum++) {
            list.add(getPlaceStatusDto(playId, subPlayId, placeNum, status));
        }
        return list;
    }

    public void pushOdds(MarketStatusUpdateVO updateVO, Collection<Long> playIds, Integer matchStatus, Map<Long, Integer> tradeModeMap, String linkId) {
        log.info("::{}::推送赔率：updateVO={},playIds={},matchStatus={},tradeModeMap={}", linkId,JSON.toJSONString(updateVO), playIds, matchStatus, JSON.toJSONString(tradeModeMap));
        Long sportId = updateVO.getSportId();
        Long matchId = updateVO.getMatchId();
        Integer matchType = getMatchType(updateVO);
        Map<Long, Long> closeSubPlayMap = updateVO.getCloseSubPlayMap();
        if (CollectionUtils.isEmpty(tradeModeMap)) {
            tradeModeMap = rcsTradeConfigService.getTradeMode(matchId, playIds);
        }
        // 所有非自动玩法
        List<Long> notAutoPlayIds = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(tradeModeMap)) {
            tradeModeMap.forEach((playId, tradeMode) -> {
                if (!TradeEnum.isAuto(tradeMode)) {
                    notAutoPlayIds.add(playId);
                }
            });
        }
        if (CollectionUtils.isEmpty(notAutoPlayIds)) {
            log.warn("::{}::所有玩法都是A模式，不处理：playIds={}", matchId, playIds);
            return;
        }
        playIds = Lists.newArrayList(notAutoPlayIds);
        List<StandardSportMarket> standardSportMarkets = standardSportMarketService.queryMarketInfo(matchId, playIds);
        if (CollectionUtils.isEmpty(standardSportMarkets)) {
            log.warn("::{}::未查询到盘口信息：playIds={}", matchId, playIds);
            return;
        }
        Set<Long> marketIdList = standardSportMarkets.stream().map(StandardSportMarket::getId).collect(Collectors.toSet());
        Map<Long, List<StandardSportMarketOdds>> marketOddsGroupMap = standardSportMarketOddsService.listAndGroup(marketIdList);
        standardSportMarkets = standardSportMarkets.stream().peek(market -> {
            List<StandardSportMarketOdds> marketOddsList = marketOddsGroupMap.get(market.getId());
            market.setMarketOddsList(marketOddsList);
        }).filter(MarketUtils::checkMarket).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(standardSportMarkets)) {
            log.warn("::{}::未查询到盘口信息：playIds={}", matchId, playIds);
            return;
        }
        Map<Long, List<StandardSportMarket>> groupByPlayId = standardSportMarkets.stream().collect(Collectors.groupingBy(StandardSportMarket::getMarketCategoryId));
        if (matchStatus == null) {
            matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
        }
        List<StandardMarketDTO> marketDTOList = new ArrayList<>(standardSportMarkets.size());
        for (Map.Entry<Long, List<StandardSportMarket>> entry : groupByPlayId.entrySet()) {
            Long playId = entry.getKey();
            List<StandardSportMarket> marketList = entry.getValue();
            Integer tradeMode = tradeModeMap.getOrDefault(playId, TradeEnum.AUTO.getCode());
            if (TradeEnum.isAuto(tradeMode)) {
                log.warn("::{}::玩法自动操盘，不处理：playId={}", matchId, playId);
                continue;
            }
            if (TradeEnum.isAutoAdd(tradeMode) && LinkedTypeEnum.DATA_PROVIDER.isYes(updateVO.getLinkedType())) {
                log.warn("::{}::A+模式数据商挡板，不处理：playId={}", matchId, playId);
                continue;
            }
            List<StandardMarketDTO> standardMarketDTOList = marketList.stream().map(market -> {
                // 转换上游入参
                List<StandardMarketOddsDTO> marketOddsDTOList = market.getMarketOddsList().stream().map(MarketUtils::toStandardMarketOddsDTO).collect(Collectors.toList());
                StandardMarketDTO marketDTO = MarketUtils.toStandardMarketDTO(market);
                marketDTO.setMarketOddsList(marketOddsDTOList);
                if (matchType != null) {
                    marketDTO.setMarketType(matchType);
                }
                if (TradeEnum.isManual(tradeMode)) {
                    marketDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
                }
                return marketDTO;
            }).collect(Collectors.toList());
            tradeStatusService.handlePushStatus(sportId, matchId, playId, standardMarketDTOList, matchStatus, tradeMode, updateVO.getSourceCloseFlag(), updateVO.getOperateSource(), updateVO.getEndFlag());
            if (CollectionUtils.isNotEmpty(closeSubPlayMap)) {
                Long subPlayId = closeSubPlayMap.get(playId);
                if (subPlayId != null) {
                    standardMarketDTOList.forEach(market -> {
                        Long childStandardCategoryId = market.getChildStandardCategoryId();
                        if (childStandardCategoryId != null && childStandardCategoryId <= subPlayId) {
                            market.setStatus(TradeStatusEnum.CLOSE.getStatus());
                            market.setPlaceNumStatus(TradeStatusEnum.CLOSE.getStatus());
                            market.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
                        }
                    });
                }
            }
            updateAddition(sportId, matchId, playId, updateVO.getScore(), standardMarketDTOList);
            marketDTOList.addAll(standardMarketDTOList);
        }
        if (CollectionUtils.isNotEmpty(marketDTOList)) {
            putTradeMarketOdds(matchId, marketDTOList, linkId);
        }
    }

    private void updateAddition(Long sportId, Long matchId, Long playId, String scoreReq, List<StandardMarketDTO> marketList) {
        if (!SportIdEnum.isFootball(sportId) || CollectionUtils.isEmpty(marketList)) {
            return;
        }
        if (playId == 33L || playId == 232L) {
            Map<String, String> map = matchStatisticsInfoDetailService.fifteenSoreMap(matchId, playId);
            if (CollectionUtils.isEmpty(map)) {
                log.warn("::{}::未查询到15分钟比分",matchId);
                return;
            }
            marketList.forEach(market -> {
                Long subPlayId = market.getChildStandardCategoryId();
                String code = ScoreTypeEnum.Football.getCodeBySubPlayId(subPlayId);
                if (StringUtils.isNotBlank(code)) {
                    String score = map.get(code);
                    if (StringUtils.isBlank(score)) {
                        score = "0:0";
                    }
                    updateAddition(score, market, playId);
                }
            });
        } else {
            if (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(playId.intValue()) && StringUtils.isNotBlank(scoreReq)) {
                log.warn("::{}::更新附加字段：score={},playId={}",matchId, scoreReq, playId);
                marketList.forEach(market -> updateAddition(scoreReq, market, playId));
            }
        }
    }

    private void updateAddition(String score, StandardMarketDTO dto, Long playId) {
        try {
            String[] scoreArray = score.split(":");
            String add1 = dto.getAddition1();
            String home = scoreArray[0];
            String away = scoreArray[1];
            if(334 != playId.intValue()){
                String add2 = CommonUtils.toBigDecimal(add1).add(CommonUtils.toBigDecimal(away)).subtract(CommonUtils.toBigDecimal(home)).stripTrailingZeros().toPlainString();
                dto.setAddition2(add2);
            }else{
                dto.setAddition2(add1);
            }
            dto.setAddition3(home);
            dto.setAddition4(away);
        } catch (Exception e) {
            log.error("::{}::更新附加字段异常{}", CommonUtil.getRequestId(dto.getId()),e.getMessage(), e);
        }
    }

    private Integer getMatchType(MarketStatusUpdateVO updateVO) {
        Long matchId = updateVO.getMatchId();
        Integer matchType = updateVO.getMatchType();
        if (matchType == null) {
            // 盘口类型，1-赛前盘，0-滚球盘
            StandardMatchInfo matchInfo = standardMatchInfoService.getById(matchId);
            matchType = RcsConstant.getMatchType(matchInfo);
        }
        updateVO.setMatchType(matchType);
        return matchType;
    }

    private String uniqueLinkId(String linkId) {
        if (StringUtils.isBlank(linkId)) {
            return CommonUtils.getLinkId();
        }
        String[] array = linkId.split("_");
        array[0] = CommonUtils.getUUID();
        return StringUtils.join(array, "_");
    }
}
