package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.TradeClearDiffValueDTO;
import com.panda.merge.dto.TradePlaceNumAutoDiffConfigDTO;
import com.panda.merge.dto.TradePlaceNumAutoDiffConfigItemDTO;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.RcsMatchMarketProbabilityConfigMapper;
import com.panda.sport.rcs.mapper.RcsNewSportWaterConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.statistics.RcsMatchPlayConfigMapper;
import com.panda.sport.rcs.mapper.sub.RcsMatchMarketConfigSubMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.enums.FootBallPlayEnum;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.statistics.BalanceVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.HEAD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 盘口位置变化消息通知
 *
 * @author black
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "RCS_CLEAR_MATCH_MARKET_TAG",
        consumerGroup = "RCS_TRADE_RCS_CLEAR_MATCH_MARKET_TAG_GROUP",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class ClearMatchMarketConsumer extends RcsConsumer<ClearDTO> {

    @Autowired
    RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private RcsMatchMarketProbabilityConfigMapper rcsMatchMarketProbabilityConfigMapper;
    @Autowired
    private ConsumetUtil consumetUtil;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;

    @Autowired
    private RcsMatchPlayConfigMapper matchPlayConfigMapper;
    @Autowired
    RcsMatchMarketConfigSubMapper rcsMatchMarketConfigSubMapper;
    @Autowired
    RcsNewSportWaterConfigMapper rcsNewSportWaterConfigMapper;
    //足篮
    public static List<Integer> FB = Arrays.asList(1,2);
    //网球等
    public static List<Integer> TETC = Arrays.asList(3,4,5,7,8,9,10);

    @Autowired
    RedisClient redisClient;

    @Override
    protected String getTopic() {
        return "RCS_CLEAR_MATCH_MARKET_TAG";
    }

    @Override
    public Boolean handleMs(ClearDTO clearDTO) {
        try {
            //三项盘自动模式数据源赔率变化清除  累计和概率差
            //进球事件，早盘进滚球，数据源切换清除水差 概率差相关数据
            //多项盘才清除投注项
            // 1.来自融合的自动清理标识  2.手动多项盘清理标识   3，task比分清理标识  4.赛前切滚球清识标识  5.数据源切换清理标识
            // 6.手动赔率变更清理平衡值等  7.篮球清水差 8.数据源切换 15.marketSource清理标识 20.融合传过来的清理我们库里的水差和平衡值
            log.info("::{}::RCS_CLEAR_MATCH_MARKET_TAG:{}", CommonUtil.getRequestId(),JsonFormatUtils.toJson(clearDTO));
            Integer clearType = clearDTO.getClearType();
            Long matchId = clearDTO.getMatchId();
            Long sportId = clearDTO.getSportId();
            Long beginTime = clearDTO.getBeginTime();
            List<Long> playIds = clearDTO.getPlayIds();
            List<ClearSubDTO> list = clearDTO.getList();
            StandardMatchInfo standardMatchInfo = null;
            if (null == sportId || null == beginTime) {
                standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
                sportId = standardMatchInfo.getSportId();
                beginTime = standardMatchInfo.getBeginTime();
            }
            if (null == clearType) {
                clearType = 0;
            }

            if (1 == sportId) {
                if (1 == clearType) {
                    //平衡值累积清理
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    // 清空配置水差
                    clearMarketDiff(list, clearType, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);

                } else if (2 == clearType) {
                    //平衡值累积清理
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    // 清空配置水差
                    clearMarketDiff(list, clearType, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);

                } else if (3 == clearType) {
                    //平衡值累积清理
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    // 清空配置水差
                    clearMarketDiff(list, clearType, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);
                    //修改状态
//                    updateStatus(list, clearType, sportId);

                } else if (4 == clearType || 5 == clearType) {
                    //平衡值累积清理
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    // 清空配置水差
                    clearMarketDiff(list, clearType, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                    //清空融合数据
                    //clearRonhe(list, clearType);
                } else if (6 == clearType) {
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);

                } else if (8 == clearType || 20 == clearType) {
                    List<ClearSubDTO> clearSubDTOS = transferBy20(matchId, playIds, list);
                    //平衡值累积清理
                    Map<String, Object> returnHashMap = playMarketOddsClear(clearSubDTOS, clearType, beginTime, matchId, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);

                    if(8 == clearType){
                        //清理水差
                        clearWaterDiff(list, sportId, returnHashMap, matchId);
                        //清空融合数据
                       // clearRonhe(list, clearType, sportId, matchId, returnHashMap);
                    }
                } else if (15 == clearType) {
                    //平衡值累积清理
                    Map<String, Object> returnHashMap = playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    //清理水差
                    clearWaterDiff(list, sportId,returnHashMap,matchId);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);
                } else {
                    log.info("::{}::其他条件清理","RTRCMMTG_"+standardMatchInfo.getId()+"_"+clearType);
                    //平衡值累积清理
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    // 清空配置水差
                    clearMarketDiff(list, clearType, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);
                    //修改状态
//                    updateStatus(list, clearType, sportId);
                }
            } else if (2 == sportId) {
                 if (4 == clearType || 5 == clearType) {
                    //平衡值累积清理
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    // 清空配置水差
                    clearMarketDiff(list, clearType, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                    //清空融合数据
                    //clearRonhe(list, clearType);
                } else if (6 == clearType) {
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                } else if (7 == clearType) {
                    updateRcsMatchMarketConfig(matchId, list,null);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                } else if (8 == clearType || 20 == clearType) {
                    List<ClearSubDTO> clearSubDTOS = transferBy20(matchId, playIds, list);
                    //平衡值累积清理
                    Map<String, Object> returnHashMap = playMarketOddsClear(clearSubDTOS, clearType, beginTime, matchId, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);

                     if(8 == clearType){
                         //清理水差
                         clearWaterDiff(list, sportId, returnHashMap, matchId);
                         //清空融合数据
//                         clearRonhe(list, clearType, sportId, matchId, returnHashMap);
                     }
                } else if (15 == clearType) {
                    //平衡值累积清理
                    Map<String, Object> returnHashMap = playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    //清理水差
                    clearWaterDiff(list, sportId, returnHashMap, matchId);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);
                }
            } else if (TradeConstant.OTHER_CAN_TRADE_SPORT.contains(sportId.intValue())) {
                if (4 == clearType || 5 == clearType) {
                    //平衡值累积清理
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    // 清空配置水差
                    clearMarketDiff(list, clearType, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                    //清空融合数据
                    //clearRonhe(list, clearType);
                } else if (6 == clearType) {
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                } else if (7 == clearType) {
                    updateRcsMatchMarketConfig(matchId, list,null);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                }
                else if (8 == clearType || 20 == clearType) {
                    List<ClearSubDTO> clearSubDTOS = transferBy20(matchId, playIds,list);
                    //平衡值累积清理
                    Map<String, Object> returnHashMap = playMarketOddsClear(clearSubDTOS, clearType, beginTime, matchId, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);

                    if(8 == clearType){
                        //清理水差
                        clearWaterDiff(list, sportId, returnHashMap, matchId);
                        //清空融合数据
//                        clearRonhe(list, clearType, sportId, matchId, returnHashMap);
                    }
                } else if (15 == clearType) {
                    //清理水差
                    clearWaterDiff(list, sportId, null, matchId);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);
                }
            }else if (TradeConstant.OTHER_BALL.contains(sportId.intValue())) {
                if (4 == clearType || 5 == clearType) {
                    //平衡值累积清理
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                    // 清空配置水差
                    clearNewTableWaterDiff(list, clearType, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                    //清空融合数据
                    //clearRonhe(list, clearType);
                }else if (6 == clearType) {
                    playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                } else if (7 == clearType) {
                    updateRcsMatchMarketConfig(matchId, list,null);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);
                } else if (8 == clearType|| 20 == clearType) {
                    List<ClearSubDTO> clearSubDTOS = transferBy20(matchId, playIds, list);
                    //平衡值累积清理
                    Map<String, Object> returnHashMap = playMarketOddsClear(clearSubDTOS, clearType, beginTime, matchId, sportId);
                    //本地数据库清理数据库概率差
                    clearLocalProbability(list, clearType, sportId);

                    if(8 == clearType){
                        //清理水差
                        clearWaterDiff(list, sportId, returnHashMap, matchId);
                        log.info("::{}::综合球种清空跳分次数"+String.format(TradeConstant.RCS_MARKET_TIMES,matchId),"RTRCMMTG_"+standardMatchInfo.getId()+"_"+clearType);
                        redisClient.delete(String.format(TradeConstant.RCS_MARKET_TIMES,matchId));
                        //清空融合数据
//                        clearRonhe(list, clearType, sportId, matchId,returnHashMap);
                    }
                } else if (15 == clearType) {
                    //清理水差
                    clearNewTableWaterDiff(list, clearType, sportId);
                    //清空融合数据
                    clearRonhe(list, clearType, sportId, matchId, null);
                }
            }else{
                //平衡值累积清理
                playMarketOddsClear(list, clearType, beginTime, matchId, sportId);
                // 清空配置水差
                clearMarketDiff(list, clearType, sportId);
                //本地数据库清理数据库概率差
                clearLocalProbability(list, clearType, sportId);
                //清空融合数据
                //clearRonhe(list, clearType, sportId, matchId);
                //修改状态
//                updateStatus(list, clearType, sportId);
            }

        } catch (Exception e) {
            log.error("::{}::RCS_CLEAR_MATCH_MARKET_TAG:{}",CommonUtil.getRequestId(),e.getMessage(),e);
        }
        return true;
    }

    /**
     * 清除新表水差
     * @param list
     * @param clearType
     * @param sportId
     */
    private void clearNewTableWaterDiff(List<ClearSubDTO> list, Integer clearType, Long sportId) {
        try {
            log.info("::{}::clearNewTableWaterDiff","RTRCMMTG_"+list.get(0).getMatchId()+"_"+clearType);
            if(CollectionUtils.isEmpty(list)){return;}
            List<ClearSubDTO> lt = JSONArray.parseArray(JSONArray.toJSONString(list),ClearSubDTO.class);
            lt.forEach(e -> e.setMarketId(null));
            rcsMatchMarketConfigMapper.clearMarketDiffByMatchAndPlay(lt);
            redisClient.delete(String.format(TradeConstant.RCS_MARKET_TIMES,list.get(0).getMatchId()));
//            rcsNewSportWaterConfigMapper.clearNewTableWaterDiff(list);
        } catch (Exception e) {
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(list),e.getMessage(),e);
        }
    }




    /**
     * clearType 20 转换
     *
     * @param matchId
     * @param playIds
     * @param clearSubDTOs
     * @return
     */
    private List<ClearSubDTO> transferBy20(Long matchId, List<Long> playIds, List<ClearSubDTO> clearSubDTOs) {
        try {
            log.info("::{}::transferBy20","RTRCMMTG_"+matchId);
            List<ClearSubDTO> list = new ArrayList<>();
            if (!CollectionUtils.isEmpty(playIds)) {
                for (Long playId : playIds) {
                    ClearSubDTO clearSubDTO = new ClearSubDTO();
                    clearSubDTO.setMatchId(matchId);
                    clearSubDTO.setPlayId(playId);
                    list.add(clearSubDTO);
                }
            }
            if (!CollectionUtils.isEmpty(clearSubDTOs)) {
                for (ClearSubDTO clearSubDTO : clearSubDTOs) {
                    list.add(clearSubDTO);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(playIds),e.getMessage(),e);
        }
        return new ArrayList<>();
    }


    /**
     * 清理水差?
     * @param matchId
     * @param list
     */
    private void updateRcsMatchMarketConfig(Long matchId, List<ClearSubDTO> list, Map<String, Object> returnHashMap) {
        try {
            log.info("::{}::updateRcsMatchMarketConfig","RTRCMMTG_"+matchId);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            buildData(list,returnHashMap,matchId);
            for (ClearSubDTO clearSubDTO : list) {
                RcsMatchMarketConfig config = new RcsMatchMarketConfig();
                config.setMatchId(matchId);
                config.setPlayId(clearSubDTO.getPlayId());
                config.setSubPlayId(clearSubDTO.getSubPlayId());
                config.setMarketIndex(clearSubDTO.getPlaceNum());
                config.setAwayAutoChangeRate("0");
                rcsMatchMarketConfigMapper.updatePlaceConfig(config);
                rcsMatchMarketConfigSubMapper.updatePlaceSubConfig(config);
                redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG,matchId,clearSubDTO.getPlayId()));
                List<Long> subPlayIds = clearSubDTO.getSubPlayIds();
                if(!CollectionUtils.isEmpty(subPlayIds)){
                    for (Long subPlayId : subPlayIds) {
                        redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG,matchId,clearSubDTO.getPlayId(),subPlayId));
                    }
                }

            }
        } catch (Exception e) {
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(list),e.getMessage(),e);
        }
    }


    //修改状态
    private void updateStatus(List<ClearSubDTO> list, Integer clearType, Long sportId) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            log.info("::{}::updateStatus" + JsonFormatUtils.toJson(list),"RTRCMMTG_"+list.get(0).getMatchId()+"_"+clearType);
            for (Integer playId : TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS) {
                //dataSource 在这里是手动自动
                Integer dataSource = rcsTradeConfigService.getDataSource(list.get(NumberUtils.INTEGER_ZERO).getMatchId(), playId.longValue());
                if (!MarketUtils.isAuto(dataSource)) {
                    MarketStatusUpdateVO vo = new MarketStatusUpdateVO();
                    vo.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
                    vo.setSportId(NumberUtils.INTEGER_ONE.longValue());
                    vo.setMatchId(list.get(NumberUtils.INTEGER_ZERO).getMatchId());
                    vo.setCategoryId(playId.longValue());
                    vo.setMarketStatus(NumberUtils.INTEGER_ONE);
                    vo.setTradeType(NumberUtils.INTEGER_ONE);
                    vo.setLinkedType(LinkedTypeEnum.SCORE_CHANGE.getCode());
                    vo.setIsPushOdds(NumberUtils.INTEGER_ZERO);
                    tradeStatusService.updateTradeStatus(vo);
                }
            }
        } catch (Exception e) {
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(list),e.getMessage(),e);
        }
    }

    /**
     * 本地数据库清理数据库概率差
     *
     * @param list
     * @param clearType
     * @param sportId
     */
    private void clearLocalProbability(List<ClearSubDTO> list, Integer clearType, Long sportId) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            if (!(1 == clearType || 2 == clearType)) {
                for (ClearSubDTO rcsMatchMarketConfig : list) {
                    rcsMatchMarketConfig.setOddsType(null);
                }
            }
            log.info("::{}::clearLocalProbability{}" ,"RTRCMMTG", JsonFormatUtils.toJson(list));
            rcsMatchMarketProbabilityConfigMapper.updateProbabilityBySelectivetToZero(list);
        } catch (Exception e) {
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(list),e.getMessage(),e);
        }
    }

    /**
     * 清空融合数据
     *  @param list
     * @param clearType
     * @param sportId
     * @param matchId
     * @param returnHashMap
     */
    private void clearRonhe(List<ClearSubDTO> list, Integer clearType, Long sportId, Long matchId, Map<String, Object> returnHashMap) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            List<ClearSubDTO> collect1 = list.stream().filter(e -> e.getPlaceNum() != null && (2 == sportId || ClearMatchMarketConsumer.TETC.contains(sportId.intValue()))).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect1)) {
                for (ClearSubDTO clearSubDTO : collect1) {
                    TradePlaceNumAutoDiffConfigItemDTO configItemDTO = new TradePlaceNumAutoDiffConfigItemDTO();
                    configItemDTO.setMarketCategoryId(clearSubDTO.getPlayId());
                    configItemDTO.setPlaceNum(clearSubDTO.getPlaceNum());
                    configItemDTO.setOddType(clearSubDTO.getOddsType());
                    configItemDTO.setDiffValue(0.0D);
                    TradePlaceNumAutoDiffConfigDTO diffConfigDTO = new TradePlaceNumAutoDiffConfigDTO();
                    diffConfigDTO.setMatchId(matchId);
                    diffConfigDTO.setDiffConfigs(configItemDTO);
                    DataRealtimeApiUtils.handleApi(diffConfigDTO, new DataRealtimeApiUtils.ApiCall() {
                        @Override
                        public <R> Response<R> callApi(Request request) {
                            return tradeMarketConfigApi.putTradePlaceNumAutoDiffConfig(request);
                        }
                    });

                }
            }
            List<Long> playIds = null;
            if(8==clearType.intValue()){
                try {
                    if(null!=returnHashMap&&null!=returnHashMap.get("playIds")){
                        playIds = (List<Long>) returnHashMap.get("playIds");
                    }
                }catch (Exception e){
                    log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(returnHashMap),e.getMessage(),e);
                }
            }
            List<ClearSubDTO> collect2 = list.stream().filter(e -> !(e.getPlaceNum() != null && (2 == sportId || ClearMatchMarketConsumer.TETC.contains(sportId.intValue())))).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect2)) {
                TradeClearDiffValueDTO diffValueDTO = new TradeClearDiffValueDTO();
                diffValueDTO.setSportId(sportId.intValue());
                diffValueDTO.setStandardMatchId(matchId);
                List<Long> categoryList = collect2.stream().map(e -> e.getPlayId()).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(playIds)&&playIds.get(0)!=null){
                    categoryList.addAll(playIds);
                }
                categoryList.removeAll(Collections.singleton(null));
                diffValueDTO.setCategoryList(categoryList);
                DataRealtimeApiUtils.handleApi(diffValueDTO, new DataRealtimeApiUtils.ApiCall() {
                    @Override
                    @Trace
                    public <R> Response<R> callApi(Request request) {
                        return tradeMarketConfigApi.clearDiffValue(request);
                    }
                });
            }
        } catch (Exception e) {
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(list),e.getMessage(),e);
        }
    }

    /**
     * 清空配置水差
     * 三项多项盘没有水差
     *
     * @param list
     * @param clearType
     * @param sportId
     */
    private void clearMarketDiff(List<ClearSubDTO> list, Integer clearType, Long sportId) {
        try {
            log.info("::{}::clearMarketDiff","RTRCMMTG");
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            rcsMatchMarketConfigMapper.clearMarketDiffByMatchAndPlay(list);
            redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_WATER,list.get(0).getMatchId()));
        } catch (Exception e) {
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(list),e.getMessage(),e);
        }
    }

    /**
     * 平衡值累积清理
     *
     * @param list
     * @param clearType
     * @param beginTime
     * @param matchId
     * @param sportId
     */
    private Map<String,Object> playMarketOddsClear(List<ClearSubDTO> list, Integer clearType, Long beginTime, Long matchId, Long sportId) {
        try {
            if (null == clearType) {
                clearType = 0;
            }
            QueryWrapper<StandardSportMarket> queryWrapper = new QueryWrapper();
            queryWrapper.lambda().eq(StandardSportMarket::getStandardMatchInfoId, matchId);
            if (1 == sportId && (!(4 == clearType || 5 == clearType))) {
                if (!CollectionUtils.isEmpty(list)&&list.size()>0) {
                    List<Long> plIds = list.stream().map(e -> e.getPlayId()).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(plIds)&&plIds.get(0)!=null) {
                        queryWrapper.lambda().in(StandardSportMarket::getMarketCategoryId, plIds);
                    }
                    if (StringUtils.isNotBlank(list.get(0).getSubPlayId()) && list.get(0).getSubPlayId() != "-1"&&list.get(0)!=null) {
                        queryWrapper.lambda().in(StandardSportMarket::getChildMarketCategoryId, list.stream().map(e -> e.getSubPlayId()).collect(Collectors.toList()));
                    }
                }
            }
            if (2 == sportId && (6 == clearType || 8 == clearType)) {
                if (!CollectionUtils.isEmpty(list)&&list.size()>0) {
                    List<Long> plIds = list.stream().map(e -> e.getPlayId()).collect(Collectors.toList());
                    List<Long> marketIds = list.stream().map(e -> e.getMarketId()).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(plIds)&&plIds.get(0)!=null) {
                        queryWrapper.lambda().in(StandardSportMarket::getMarketCategoryId, plIds);
                    }
                    if (StringUtils.isNotBlank(list.get(0).getSubPlayId()) && list.get(0).getSubPlayId() != "-1"&&list.get(0)!=null) {
                        queryWrapper.lambda().in(StandardSportMarket::getChildMarketCategoryId, list.stream().map(e -> e.getSubPlayId()).collect(Collectors.toList()));
                    }
                    if (!CollectionUtils.isEmpty(marketIds)&&marketIds.get(0)!=null) {
                        queryWrapper.lambda().in(StandardSportMarket::getId, marketIds);
                    }
                }
            }
            if (20 == clearType) {
                List<Long> plIds = list.stream().map(e -> e.getPlayId()).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(plIds)&&plIds.get(0)!=null) {
                    queryWrapper.lambda().in(StandardSportMarket::getMarketCategoryId, plIds);
                }
            }
            List<StandardSportMarket> markets = standardSportMarketMapper.selectList(queryWrapper);
            if (CollectionUtils.isEmpty(markets)) {return null;}
            consumetUtil.setPlaceNum(markets);
            if (null == beginTime) {
                beginTime = System.currentTimeMillis();
            }
            String dateExpect = DateUtils.getDateExpect(beginTime);
            ArrayList<Object> playIds = new ArrayList<>();
            HashMap<String, Object> returnHashMap = new HashMap<>();
            Map<Long, List<StandardSportMarket>> map = markets.stream().collect(Collectors.groupingBy(StandardSportMarket::getMarketCategoryId));
            for (Long id : map.keySet()) {
                List<StandardSportMarket> standardSportMarkets = map.get(id);
                playIds.add(id);
                for (StandardSportMarket market : standardSportMarkets) {
                    //清理平衡值累积
                    if (1 == clearType || 2 == clearType) {
                        for (ClearSubDTO rcsMatchMarketConfig : list) {
                            if (market.getId().longValue() == rcsMatchMarketConfig.getMarketId()) {
                                consumetUtil.clearBalanceValue(dateExpect, market, clearType, rcsMatchMarketConfig.getOddsType(), sportId);
                                BalanceVo balanceVo = new BalanceVo(matchId, market.getMarketCategoryId(), market.getId());
                                balanceVo.setPlaceNum(market.getPlaceNum());
                                balanceVo.setMarketId(market.getId());
                                balanceVo.setBalanceValue(NumberUtils.LONG_ZERO);
                                balanceVo.setCurrentSide(FootBallPlayEnum.getOddsType(rcsMatchMarketConfig.getPlayId()));
                                //log.info("::{}::平衡值存入MQ消息队列:{}", "RTRCMMTG_"+list.get(0).getMatchId()+"_"+clearType,JsonFormatUtils.toJson(balanceVo));
                                producerSendMessageUtils.sendMessage(MqConstants.REALTIME_SYNC_BALANCE_TOPIC, MqConstants.REALTIME_SYNC_BALANCE_TAG, "", balanceVo);
                            }
                        }
                    } else {
                        consumetUtil.clearBalanceValue(dateExpect, market, clearType, null, sportId);
                        BalanceVo balanceVo = new BalanceVo(matchId, market.getMarketCategoryId(), market.getId());
                        balanceVo.setPlaceNum(market.getPlaceNum());
                        balanceVo.setMarketId(market.getId());
                        balanceVo.setBalanceValue(NumberUtils.LONG_ZERO);
                        balanceVo.setCurrentSide(FootBallPlayEnum.getOddsType(market.getMarketCategoryId()));
                        //log.info("::{}::平衡值存入MQ消息队列:{}",  "RTRCMMTG_"+list.get(0).getMatchId()+"_"+clearType,JsonFormatUtils.toJson(balanceVo));
                        producerSendMessageUtils.sendMessage(MqConstants.REALTIME_SYNC_BALANCE_TOPIC, MqConstants.REALTIME_SYNC_BALANCE_TAG, "", balanceVo);
                    }
                    if (8 == clearType || 20 == clearType) {
                        if(1==sportId||2==sportId||ClearMatchMarketConsumer.TETC.contains(sportId.intValue())){
                            market.setStandardMatchInfoId(matchId);
                            rcsMatchMarketConfigService.clearConfig(market,sportId);
                        }else {
                            market.setStandardMatchInfoId(matchId);
                            rcsMatchMarketConfigService.clearNewConfig(market,sportId);
                        }
                    }
                }
            }
            returnHashMap.put("playIds",playIds);
            returnHashMap.put("map",map);
            return returnHashMap;
        } catch (Exception e) {
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(list),e.getMessage(),e);
        }
        return null;
    }


    /**
     * 清理水差
     * @param list
     * @param sportId
     * @param returnHashMap
     * @param matchId
     */
    private void clearWaterDiff(List<ClearSubDTO> list, Long sportId, Map<String, Object> returnHashMap, Long matchId) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            buildData(list,returnHashMap, matchId);
            log.info("::{}::clearWaterDiff{}" ,"RTRCMMTG_"+matchId, JsonFormatUtils.toJson(list));
            for (ClearSubDTO rcsMatchMarketConfig : list) {
                StandardSportMarket standardSportMarket = new StandardSportMarket();
                standardSportMarket.setStandardMatchInfoId(rcsMatchMarketConfig.getMatchId());
                standardSportMarket.setMarketCategoryId(rcsMatchMarketConfig.getPlayId());
                if(StringUtils.isNotBlank(rcsMatchMarketConfig.getSubPlayId())){
                    standardSportMarket.setChildMarketCategoryId(Long.valueOf(rcsMatchMarketConfig.getSubPlayId()));
                }
                rcsMatchMarketConfigMapper.updateMatchMarketMarginConfigByMatch(standardSportMarket);
                redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_WATER,rcsMatchMarketConfig.getMatchId()));
                // 清空配置水差
                if (!ConsumetUtil.getxPlays().contains(standardSportMarket.getMarketCategoryId().intValue()) && FB.contains(sportId.intValue())) {
                    rcsMatchMarketConfigMapper.updateMatchMarketConfigByMatch(standardSportMarket);
                    redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG,rcsMatchMarketConfig.getMatchId(),rcsMatchMarketConfig.getPlayId()));
                }
                List<Long> subPlayIds = rcsMatchMarketConfig.getSubPlayIds();
                if (ConsumetUtil.getxPlays().contains(standardSportMarket.getMarketCategoryId().intValue()) || ClearMatchMarketConsumer.TETC.contains(sportId.intValue())) {
                    rcsMatchMarketConfigSubMapper.updateMatchMarketConfigSubByMatch(standardSportMarket);
                    if(!CollectionUtils.isEmpty(subPlayIds)){
                        for (Long subPlayId : subPlayIds) {
                            redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG,rcsMatchMarketConfig.getMatchId(),rcsMatchMarketConfig.getPlayId(),subPlayId));
                        }
                    }
                }
                matchPlayConfigMapper.clearMarketHeadGapByMatch(standardSportMarket);
                if(!CollectionUtils.isEmpty(subPlayIds)) {
                    for (Long subPlayId : subPlayIds) {
                        redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_HEAD_CONFIG, rcsMatchMarketConfig.getMatchId(), rcsMatchMarketConfig.getPlayId(), subPlayId));
                    }
                }
            }
        } catch (Exception e) {
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(list),e.getMessage(),e);
        }
    }

    private void buildData(List<ClearSubDTO> list, Map<String, Object> returnHashMap, Long matchId) {
        Map<Long, List<StandardSportMarket>> map = null;
        try{
            if(null!=returnHashMap){
                map= (Map<Long, List<StandardSportMarket>>) returnHashMap.get("map");
            }
            if(CollectionUtils.isEmpty(map)){
                List<Long> plIds = list.stream().map(e -> e.getPlayId()).collect(Collectors.toList());
                if((StringUtils.isBlank(list.get(0).getSubPlayId())||"null".equals(list.get(0).getSubPlayId()))&&null!=plIds.get(0)&&null!=matchId){
                    QueryWrapper<StandardSportMarket> queryWrapper = new QueryWrapper();
                    queryWrapper.lambda().eq(StandardSportMarket::getStandardMatchInfoId, matchId);
                    queryWrapper.lambda().in(StandardSportMarket::getMarketCategoryId, plIds);
                    List<StandardSportMarket> markets = standardSportMarketMapper.selectList(queryWrapper);
                    map = markets.stream().collect(Collectors.groupingBy(StandardSportMarket::getMarketCategoryId));
                }
            }
        }catch (Exception e){
            log.error("::{}::{},{},{}","RTRCMMTG",JsonFormatUtils.toJson(list),e.getMessage(),e);
        }
        if(null!=map){
            if(list.size()<=1&&null==list.get(0).getPlayId()){
                ArrayList<ClearSubDTO> objects = new ArrayList<>();
                for (Long id : map.keySet()) {
                    List<StandardSportMarket> standardSportMarkets = map.get(id);
                    if(!CollectionUtils.isEmpty(standardSportMarkets)){
                        ClearSubDTO clearSubDTO = new ClearSubDTO();
                        clearSubDTO.setMatchId(matchId);
                        clearSubDTO.setPlayId(id);
                        clearSubDTO.setSubPlayIds(standardSportMarkets.stream().map(e -> e.getChildMarketCategoryId()).collect(Collectors.toList()));
                        objects.add(clearSubDTO);
                    }
                }
                list.clear();
                list.addAll(objects);
            }else{
                for (Long id : map.keySet()) {
                    List<StandardSportMarket> standardSportMarkets = map.get(id);
                    if(!CollectionUtils.isEmpty(standardSportMarkets)){
                        for (ClearSubDTO clearSubDTO : list) {
                            if(clearSubDTO.getPlayId().equals(id)){
                                clearSubDTO.setSubPlayIds(standardSportMarkets.stream().map(e -> e.getChildMarketCategoryId()).collect(Collectors.toList()));
                            }
                        }
                    }
                }
            }
        }else{
            for (ClearSubDTO clearSubDTO : list) {
                if(null!=clearSubDTO.getPlayId()){
                    clearSubDTO.setSubPlayIds(Arrays.asList(Long.valueOf(clearSubDTO.getSubPlayId())));
                }
            }
        }
    }
}
