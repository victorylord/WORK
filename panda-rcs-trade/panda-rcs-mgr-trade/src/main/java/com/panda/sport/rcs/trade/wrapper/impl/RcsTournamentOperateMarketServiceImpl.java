package com.panda.sport.rcs.trade.wrapper.impl;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.TradeMarketAutoDiffConfigDTO;
import com.panda.merge.dto.TradeMarketAutoDiffConfigItemDTO;
import com.panda.merge.dto.TradeMarketConfigDTO;
import com.panda.sport.data.rcs.dto.ExtendBean;
import com.panda.sport.data.rcs.dto.OrderItem;
import com.panda.sport.data.rcs.dto.ThreewayOverLoadTriggerItem;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.MatchConstants;
import com.panda.sport.rcs.constants.PlayIdEnum;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.MatchLevelEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsTournamentOperateMarketMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsBusinessSingleBetConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsTournamentMarketConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.trade.controller.MarketViewController;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOperateMarketService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketOddsService;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils.ApiCall;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.MatchMarketLiveOddsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.common.MqConstants.MARKET_WATER_CONFIG_TOPIC;

/**
 * @author :  Sean
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  联赛操盘赔付服务实现类
 * @Date: 2019-10-23 16:37
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsTournamentOperateMarketServiceImpl extends ServiceImpl<RcsTournamentOperateMarketMapper, RcsTournamentMarketConfig> implements RcsTournamentOperateMarketService {

    @Autowired
    RcsTournamentOperateMarketMapper rcsTournamentOperateMarketMapper;

    @Reference(lazy = true, check = false, retries = 3)
    ITradeMarketConfigApi iTradeMarketConfigApi;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    MarketViewController marketViewController;

    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;

    @Autowired
    StandardSportMarketOddsService standardSportMarketOddsService;

    @Autowired
    StandardSportMarketMapper standardSportMarketMapper;

    @Autowired
    IRcsMatchMarketConfigService rcsMatchMarketConfigService;

    @Autowired
    RcsTournamentOperateMarketService rcsTournamentOperateMarketService;

    static final Long expiry = 24 * 60 * 60L;

//    @Override
//    public List<RcsTournamentMarketConfig> getRcsTournamentMarketConfig(Map<String, Object> columnMap) {
//        return rcsTournamentOperateMarketMapper.selectByMap(columnMap);
//    }


//    private void verifyData(RcsTournamentMarketConfig config) {
//        if (config.getMaxOdds() == null || config.getMinOdds() == null) {
//            throw new RcsServiceException("最大最小赔率不能为空");
//        }
//
//        //7：赔率最大最小验证
//        if (config.getMaxOdds().compareTo(config.getMinOdds()) < 0) {
//            throw new RcsServiceException("最小赔率比最大赔率大");
//        }
//
//        if (config.getMaxOdds().compareTo(new BigDecimal("300"))>0
//                || config.getMinOdds().compareTo(new BigDecimal("1.01"))<0) {
//            throw new RcsServiceException("最大最小赔率必须介于1.01 - 300 之间");
//        }
//
//        //限额 机制
//        if(Arrays.asList(PlayIdEnum.CornHalfOverUnder.getTwoWayDoublePlay()).contains(config.getPlayId().intValue())){
//            if(config.getOddChangeRule()==null){
//                throw new RcsServiceException("跳分机制不能为空");
//            }
//
//            if(config.getOddChangeRule() == 0){
//                if((config.getHomeSingleMaxAmount() != null && config.getHomeSingleMaxAmount()<100)
//                        || (config.getHomeMultiMaxAmount() != null && config.getHomeMultiMaxAmount()<100)){
//                    throw new RcsServiceException("单枪/累计限额不能<100");
//                }
//            }else{
//                if((config.getHomeLevelFirstMaxAmount() != null && config.getHomeLevelFirstMaxAmount()<100)
//                        || (config.getHomeLevelSecondMaxAmount() != null && config.getHomeLevelSecondMaxAmount()<100)){
//                    throw new RcsServiceException("一二级限额不能<100");
//                }
//            }
//        }
//        //水差/赔率变化率
//        BigDecimal mg = Optional.ofNullable(config.getMargin()).orElse(new BigDecimal("0"));
//        if (Arrays.asList(PlayIdEnum.CornHalfOverUnder.getTwoWayDoublePlay()).contains(config.getPlayId().intValue())){
//            //两项盘
//            if(new BigDecimal("0.05").compareTo(mg)>0 ||
//                    new BigDecimal("0.5").compareTo(mg)<0){
//                throw new RcsServiceException("马来盘Margin值需要介于0.05-0.5");
//            }
//
//            if(config.getOddChangeRule() == 0){
//                //累计/单枪
//                if (config.getAwaySingleOddsRate() == null ||
//                        config.getAwayMultiOddsRate() == null ||
//                        config.getHomeSingleOddsRate() == null || config.getHomeMultiOddsRate() == null) {
//                    throw new RcsServiceException("两项盘自动操盘水差/手动操盘赔率变化率不能为空");
//                } else if (config.getAwaySingleOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
//                        || config.getAwayMultiOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
//                        || config.getHomeSingleOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
//                        || config.getHomeMultiOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0) {
//                    throw new RcsServiceException("两项盘自动操盘水差/手动操盘赔率变化率绝对值需要介于0-0.15");
//                }
//                config.setAwaySingleOddsRate(config.getAwaySingleOddsRate().abs());
//                config.setAwayMultiOddsRate(config.getAwayMultiOddsRate().abs());
//                config.setHomeSingleOddsRate(config.getHomeSingleOddsRate().abs());
//                config.setHomeMultiOddsRate(config.getHomeMultiOddsRate().abs());
//            }else{
//                //一级/二级
//                if (config.getAwayLevelFirstOddsRate() == null ||
//                        config.getAwayLevelSecondOddsRate() == null ||
//                        config.getHomeLevelFirstOddsRate() == null || config.getHomeLevelSecondOddsRate() == null) {
//                    throw new RcsServiceException("两项盘自动操盘水差/手动操盘赔率变化率不能为空");
//                } else if (config.getAwayLevelFirstOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
//                        || config.getAwayLevelSecondOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
//                        || config.getHomeLevelFirstOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
//                        || config.getHomeLevelSecondOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0) {
//                    throw new RcsServiceException("两项盘自动操盘水差/手动操盘赔率变化率绝对值需要介于0-0.15");
//                }
//                config.setAwayLevelFirstOddsRate(config.getAwayLevelFirstOddsRate().abs());
//                config.setAwayLevelSecondOddsRate(config.getAwayLevelSecondOddsRate().abs());
//                config.setHomeLevelFirstOddsRate(config.getHomeLevelFirstOddsRate().abs());
//                config.setHomeLevelSecondOddsRate(config.getHomeLevelSecondOddsRate().abs());
//            }
//
//        }else{
////            if(new BigDecimal("102").compareTo(mg)>0 ||
////                    new BigDecimal("110").compareTo(mg)<0){
////                throw new RcsServiceException("马来盘Margin值需要介于102-110");
////            }
//            //独赢盘
//            if(config.getHomeLevelFirstOddsRate() ==null){
//                throw new RcsServiceException("独赢盘赔率变化率不可以为空");
//            }else if(config.getHomeLevelFirstOddsRate().compareTo(new BigDecimal("30"))>0
//                    || config.getHomeLevelFirstOddsRate().compareTo(new BigDecimal("-30"))<0){
//                throw new RcsServiceException("独赢盘赔率变化率绝对值需要介于0-30");
//            }
//            config.setHomeLevelFirstOddsRate(config.getHomeLevelFirstOddsRate().abs());
//        }
//
////        //最大投注金额校验
////        BigDecimal maxAmout = getMaxBetAmount(config.getTournamentId().longValue(),config.getPlayId().longValue());
////        if(ObjectUtils.isEmpty(config.getMaxSingleBetAmount()) ||
////                ObjectUtils.isEmpty(maxAmout) ||
////                config.getMaxSingleBetAmount().longValue() > maxAmout.longValue()){
////            throw new RcsServiceException("最大投注金额不能大于联赛配置");
////        }
//    }

//    @Override
//    public BigDecimal getMaxBetAmount(RcsMatchMarketConfig config) {
//        // 查询联赛的最大赔付
//        Map<String, Object> map = new HashMap<>(2);
//        map.put("tournament_id", config.getTournamentId());
//        map.put("play_id", config.getPlayId());
//        BigDecimal maxBetAmount = BigDecimal.valueOf(0);
//        if (!mainPlayIds().contains(config.getPlayId())){
//            maxBetAmount = new BigDecimal("10000000") ;
//        }
//        List<RcsTournamentMarketConfig> rcsTournamentMarketConfigList = rcsTournamentOperateMarketMapper.selectByMap(map);
//        if ((!org.springframework.util.CollectionUtils.isEmpty(rcsTournamentMarketConfigList)) && rcsTournamentMarketConfigList.get(0).getMaxSingleBetAmount() != null) {
//            maxBetAmount = BigDecimal.valueOf(rcsTournamentMarketConfigList.get(0).getMaxSingleBetAmount());
//        }else {
//            RcsBusinessSingleBetConfig bet = rcsTournamentOperateMarketMapper.queryBusinessBetMaxAmount(config);
//            if (!ObjectUtils.isEmpty(bet) &&
//                !ObjectUtils.isEmpty(bet.getOrderMaxValue())){
//                maxBetAmount = bet.getOrderMaxValue();
//            }
//        }
//        return maxBetAmount;
//    }
//    private List<Long> mainPlayIds(){
//        List<Long> list = Lists.newArrayList();
//        list.add(PlayIdEnum.Handicap.getId().longValue());
//        list.add(PlayIdEnum.HalftimeHandicap.getId().longValue());
//        list.add(PlayIdEnum.OverUnder.getId().longValue());
//        list.add(PlayIdEnum.HalftimeOverUnder.getId().longValue());
//        list.add(PlayIdEnum.ThreeWay.getId().longValue());
//        list.add(PlayIdEnum.HalftimeThreeWay.getId().longValue());
//        return list;
//    }
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public HttpResponse<RcsTournamentMarketConfig> updateGetTournamentConfig(RcsTournamentMarketConfig config) {
//            formatData(config);
//            verifyData(config);
//            rcsTournamentOperateMarketMapper.insertOrUpdateTournamentConfig(config);
//        //  插入数据
////        if (config.getId() == null) {
////            Integer tournamentId = config.getTournamentId();
////            if (tournamentId == null) {
////                return HttpResponse.fail("联赛id不能为空");
////            }
////            Integer playId = config.getPlayId();
////            if (playId == null) {
////                return HttpResponse.fail("玩法id不能为空");
////            }
////            verifyData(config);
////            rcsTournamentOperateMarketMapper.insert(config);
////        }//更新数据
////        else {
////            verifyData(config);
////            rcsTournamentOperateMarketMapper.updateById(config);
////        }
//        //数据发送到融合
////        putTradeTournamentConfig(config);
//        //更新缓存数据
//        redisClient.setExpiry(String.format(RedisKeys.RCS_TOURNAMENT_MAX_AMOUNT, config.getTournamentId(), config.getPlayId()), config.getMaxSingleBetAmount(), 1000 * 60 * 60 * 24L);
//        rcsTournamentOperateMarketService.sendRcsDataMq(null,config.getPlayId()+"","","0");
//
//        return HttpResponse.success(config);
//    }

    private void formatData(RcsTournamentMarketConfig config){
        if (!ObjectUtils.isEmpty(config.getHomeLevelFirstOddsRate())){
            config.setHomeLevelFirstOddsRate(config.getHomeLevelFirstOddsRate().abs());
        }
        if (!ObjectUtils.isEmpty(config.getAwayLevelFirstOddsRate())){
            config.setAwayLevelFirstOddsRate(config.getAwayLevelFirstOddsRate().abs());
        }
        if (!ObjectUtils.isEmpty(config.getHomeLevelSecondOddsRate())){
            config.setHomeLevelSecondOddsRate(config.getHomeLevelSecondOddsRate().abs());
        }
        if (!ObjectUtils.isEmpty(config.getAwayLevelSecondOddsRate())){
            config.setAwayLevelSecondOddsRate(config.getAwayLevelSecondOddsRate().abs());
        }
        if (!ObjectUtils.isEmpty(config.getHomeMultiOddsRate())){
            config.setHomeMultiOddsRate(config.getHomeMultiOddsRate().abs());
        }
        if (!ObjectUtils.isEmpty(config.getHomeSingleOddsRate())){
            config.setHomeSingleOddsRate(config.getHomeSingleOddsRate().abs());
        }
        if (!ObjectUtils.isEmpty(config.getAwayMultiOddsRate())){
            config.setAwayMultiOddsRate(config.getAwayMultiOddsRate().abs());
        }
        if (!ObjectUtils.isEmpty(config.getAwaySingleOddsRate())){
            config.setAwaySingleOddsRate(config.getAwaySingleOddsRate().abs());
        }
    }

//    @Trace
//    public void putTradeTournamentConfig(RcsTournamentMarketConfig rcsTournamentMarketConfig) {
//        TradeMarketConfigDTO tradeMarketConfigDTO = new TradeMarketConfigDTO();
//        tradeMarketConfigDTO.setActive(1);
//        tradeMarketConfigDTO.setConfigId(MatchConstants.TOURNAMENT_ID + rcsTournamentMarketConfig.getId());
//        tradeMarketConfigDTO.setLevel(MatchLevelEnum.TOURNAMENT.getLevel());
//        tradeMarketConfigDTO.setMarketStatus(null);
//        tradeMarketConfigDTO.setModifyTime(System.currentTimeMillis());
//        //暂无配置修改人
//        tradeMarketConfigDTO.setOperaterId(1L);
//        tradeMarketConfigDTO.setSourceSystem(2);
//        int playId = rcsTournamentMarketConfig.getPlayId();
//        tradeMarketConfigDTO.setTargetId(String.valueOf(playId));
//        tradeMarketConfigDTO.setTradeType(1);
//
//        DataRealtimeApiUtils.handleApi(tradeMarketConfigDTO, new ApiCall() {
//            @Override
//            @Trace
//            public <R> Response<R> callApi(Request request) {
//                return iTradeMarketConfigApi.putTradeMarketConfig(request);
//            }
//        });
//    }

    /**
     * 发送mq数据
     **/
    @Override
    public void sendRcsDataMq(Long tournamentId, String playId, String marketIndex, String match,String subPlayId,Long amount) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("matchId", match);
            map.put("playId", playId);
            map.put("subPlayId",subPlayId);
            map.put("marketIndex",marketIndex);
            map.put("maxSingleBetAmount",amount);
            //通知sdk 清除缓存
            String uuid = UuidUtils.generateUuid();
            log.info("::{}::更新操盘限额配置uuid={}",CommonUtil.getRequestId(),uuid);
            producerSendMessageUtils.sendMessage("TEMPLATE_MARKET_CONFIG_TOPIC",null,uuid, map);
            log.info("::{}::盘口位置限额 trade通知sdk删除缓存 完成 ：{}", CommonUtil.getRequestId(), JSONObject.toJSONString(map));
        } catch (Exception ex) {
            log.error("发送MQ失败!", ex);
        }
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void saveAndUpdateMarketWaterHeadConfig(List<ThreewayOverLoadTriggerItem> configs) throws RcsServiceException {
//        log.info("水差设置入参：{}", JSONObject.toJSONString(configs));
//        List<ThreewayOverLoadTriggerItem> list = mergeAutoChangeRate(configs);
////        rcsTournamentOperateMarketMapper.saveAndUpdateMarketWaterHeadConfig(list);
//        List<RcsMatchMarketConfig> rcsMatchMarketConfigs = new ArrayList<>();
//        list.stream().forEach(model -> {
//            RcsMatchMarketConfig matchMarketConfig = new RcsMatchMarketConfig();
//            matchMarketConfig.setMatchId(model.getMatchId());
//            matchMarketConfig.setPlayId(Long.parseLong(String.valueOf(model.getPlayId())));
//            matchMarketConfig.setMarketId(model.getMarketId());
////            matchMarketConfig.setTieAutoChangeRate(String.valueOf(model.getTieAutoChangeRate()));
////            matchMarketConfig.setHomeAutoChangeRate(String.valueOf(model.getHomeAutoChangeRate()));
//            matchMarketConfig.setAwayAutoChangeRate(String.valueOf(model.getAwayAutoChangeRate()));
//            rcsMatchMarketConfigs.add(matchMarketConfig);
//
//        });
//        if (rcsMatchMarketConfigs.size() > 0) {
//            //发送MQ更新mongo数据
//            producerSendMessageUtils.sendMessage(MARKET_WATER_CONFIG_TOPIC, rcsMatchMarketConfigs);
//            log.info("saveAndUpdateMarketWaterHeadConfig盘口设置水差:{}", JsonFormatUtils.toJson(rcsMatchMarketConfigs));
//        }
//        TradeMarketAutoDiffConfigDTO bean = new TradeMarketAutoDiffConfigDTO();
//        bean.setMatchId(list.get(0).getMatchId());
//        bean.setDiffConfigs(transferDtos(configs));
//        Response response = DataRealtimeApiUtils.handleApi(bean, new DataRealtimeApiUtils.ApiCall() {
//            @Override
//            public <R> Response<R> callApi(Request request) {
//                return iTradeMarketConfigApi.putTradeMarketAutoDiffConfig(request);
//            }
//        });
//    }

//    @Override
//    public void saveAndUpdateMarketWaterHeadConfigs(List<ThreewayOverLoadTriggerItem> configs) throws RcsServiceException {
//        List<RcsMatchMarketConfig> rcsMatchMarketConfigs = new ArrayList<>();
//        configs.stream().forEach(model -> {
//            RcsMatchMarketConfig matchMarketConfig = new RcsMatchMarketConfig();
//            matchMarketConfig.setMatchId(model.getMatchId());
//            matchMarketConfig.setPlayId(Long.parseLong(String.valueOf(model.getPlayId())));
//            matchMarketConfig.setMarketId(model.getMarketId());
////            matchMarketConfig.setTieAutoChangeRate(String.valueOf(model.getTieAutoChangeRate()));
////            matchMarketConfig.setHomeAutoChangeRate(String.valueOf(model.getHomeAutoChangeRate()));
//            matchMarketConfig.setAwayAutoChangeRate(String.valueOf(model.getAwayAutoChangeRate()));
//            rcsMatchMarketConfigs.add(matchMarketConfig);
//
//        });
//        if (rcsMatchMarketConfigs.size() > 0) {
//            //发送MQ更新mongo数据
//            producerSendMessageUtils.sendMessage(MARKET_WATER_CONFIG_TOPIC, rcsMatchMarketConfigs);
//            log.info("saveAndUpdateMarketWaterHeadConfig盘口设置水差:{}", JsonFormatUtils.toJson(rcsMatchMarketConfigs));
//        }
//
//        TradeMarketAutoDiffConfigDTO bean = new TradeMarketAutoDiffConfigDTO();
//        bean.setMatchId(configs.get(0).getMatchId());
//        bean.setDiffConfigs(transferDtos(configs));
//        Response response = DataRealtimeApiUtils.handleApi(bean, new DataRealtimeApiUtils.ApiCall() {
//            @Override
//            public <R> Response<R> callApi(Request request) {
//                return iTradeMarketConfigApi.putTradeMarketAutoDiffConfig(request);
//            }
//        });
//    }

    private List<TradeMarketAutoDiffConfigItemDTO> transferDtos(List<ThreewayOverLoadTriggerItem> list) {
        List<TradeMarketAutoDiffConfigItemDTO> li = Lists.newArrayList();
        for (ThreewayOverLoadTriggerItem l : list) {
            TradeMarketAutoDiffConfigItemDTO dto = new TradeMarketAutoDiffConfigItemDTO();
            dto.setMarketId(l.getMarketId());
            dto.setMarketCategoryId((long) l.getPlayId());
            dto.setOddType(l.getOddsType());
            dto.setDiffValue(l.getHomeAutoChangeRate());
            li.add(dto);
        }
        return li;
    }

//    @Override
//    public Map<Integer, List<Map<String, Object>>> queryMarketWaterHeadConfig(ThreewayOverLoadTriggerItem item) {
//        List<ThreewayOverLoadTriggerItem> items = Lists.newArrayList();
////        List<ThreewayOverLoadTriggerItem> items = rcsTournamentOperateMarketMapper.queryMarketWaterHeadConfig(item);
//        List<ThreewayOverLoadTriggerItem> list = rcsTournamentOperateMarketMapper.queryMarketOddsList(item);
//
//        if (CollectionUtils.isEmpty(list)) {
//            log.info("盘口赔率信息为空：{}", JSONObject.toJSONString(item));
//            return null;
//        }
//        if (CollectionUtils.isEmpty(items)) {
//            log.info("没有配置水差信息：{}", JSONObject.toJSONString(item));
//        }
//        Map<Integer, List<Map<String, Object>>> map = new HashMap<>();
//        List<Map<String, Object>> configs = null;
//        Map<String, Object> marketMap = null;
//        List<ThreewayOverLoadTriggerItem> marketList = null;
//        for (ThreewayOverLoadTriggerItem l : list) {
//            configs = map.get(l.getRollType());
//            splitAutoChangeRate(l, items);
//            if (ObjectUtils.isEmpty(configs)) {
//                configs = new ArrayList<>();
//                marketMap = new HashMap<>();
//                marketList = new ArrayList<>();
//                marketList.add(l);
//                marketMap.put("marketId", l.getMarketId());
//                marketMap.put("playId", l.getPlayId());
//                marketMap.put("list", marketList);
//                configs.add(marketMap);
//            } else {
//                mergeMarket(configs, l);
//            }
//            map.put(l.getRollType(), configs);
//        }
//        Long count = rcsTournamentOperateMarketMapper.queryMarketCount(item);
//        if (!ObjectUtils.isEmpty(count)) {
//            sortMarketOdds(map, count);
//        }
//        return map;
//    }

//    private void sortMarketOdds(Map<Integer, List<Map<String, Object>>> map, Long count) {
//        for (Integer i : map.keySet()) {
//            List<Map<String, Object>> list = map.get(i);
//            for (Map<String, Object> m : list) {
//                List<ThreewayOverLoadTriggerItem> items = (List<ThreewayOverLoadTriggerItem>) m.get("list");
//                if (items.size() != 3) {
//                    for (ThreewayOverLoadTriggerItem item : items) {
//                        for (ThreewayOverLoadTriggerItem t : items) {
//                            if (item.getMarketId().longValue() == t.getMarketId().longValue() &&
//                                    (!item.getOddsType().equalsIgnoreCase(t.getOddsType()))
//                                    && ObjectUtils.isEmpty(item.getDiffOdds())) {
//                                long odds = Long.valueOf(item.getMaxOdds()) - Long.valueOf(t.getMaxOdds());
//                                if (odds < 0) {
//                                    odds = -1 * odds;
//                                }
//                                item.setDiffOdds(odds);
//                                t.setDiffOdds(odds);
//                                m.put("order", odds);
//                            }
//                        }
//                    }
//                }
//            }
//            List<Map<String, Object>> l = list.stream().sorted((Map<String, Object> map1, Map<String, Object> map2) ->
//                    Long.valueOf(map1.get("order").toString()).compareTo(Long.valueOf(map2.get("order").toString())))
//                    .limit(count).collect(Collectors.toList());
//            map.put(i, l);
//        }
//
//    }

//    private void mergeMarket(List<Map<String, Object>> configs, ThreewayOverLoadTriggerItem l) {
//
//        List<ThreewayOverLoadTriggerItem> markets = new ArrayList<>();
//        for (Map<String, Object> m : configs) {
//            if (m.get("marketId").toString().equals(l.getMarketId().toString())) {
//                markets = (List<ThreewayOverLoadTriggerItem>) m.get("list");
//                if (CollectionUtils.isEmpty(markets)) {
//                    markets = new ArrayList<>();
//                    markets.add(l);
//                    m.put("marketId", l.getMarketId());
//                    m.put("playId", l.getPlayId());
//                    m.put("list", markets);
//                    return;
//                } else {
//                    for (ThreewayOverLoadTriggerItem t : markets) {
//                        if (t.getMarketId().longValue() == l.getMarketId().longValue() &&
//                                (!t.getOddsType().equalsIgnoreCase(l.getOddsType()))) {
//                            markets.add(l);
//                            m.put("marketId", l.getMarketId());
//                            m.put("playId", l.getPlayId());
//                            m.put("list", markets);
//                            return;
//                        }
//                    }
//                }
//            }
//        }
//        Map<String, Object> map = new HashMap<>();
//        if (CollectionUtils.isEmpty(markets)) {
//            markets = new ArrayList<>();
//            markets.add(l);
//            map.put("marketId", l.getMarketId());
//            map.put("playId", l.getPlayId());
//            map.put("list", markets);
//            configs.add(map);
//            return;
//        }
//    }

//    public static void splitAutoChangeRate(ThreewayOverLoadTriggerItem item, List<ThreewayOverLoadTriggerItem> items) {
//        if (CollectionUtils.isEmpty(items)) {
//            return;
//        }
//        for (ThreewayOverLoadTriggerItem e : items) {
//            if (e.getMarketId().longValue() == item.getMarketId().longValue()) {
//                if (BaseConstants.ODD_TYPE_X.equalsIgnoreCase(item.getOddsType())) {
//                    item.setHomeAutoChangeRate(e.getTieAutoChangeRate());
//                } else if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(item.getOddsType()) ||
//                        BaseConstants.ODD_TYPE_ODD.equalsIgnoreCase(item.getOddsType()) ||
//                        BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(item.getOddsType())) {
//                    item.setHomeAutoChangeRate(e.getHomeAutoChangeRate());
//                } else if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(item.getOddsType()) ||
//                        BaseConstants.ODD_TYPE_EVEN.equalsIgnoreCase(item.getOddsType()) ||
//                        BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(item.getOddsType())) {
//                    item.setHomeAutoChangeRate(e.getAwayAutoChangeRate());
//                }
//                break;
//            }
//        }
//
//    }

//    public List<ThreewayOverLoadTriggerItem> mergeAutoChangeRate(List<ThreewayOverLoadTriggerItem> items) {
//        List<ThreewayOverLoadTriggerItem> list = new ArrayList<>();
//        Map<String, ThreewayOverLoadTriggerItem> map = new HashMap<>();
//        ThreewayOverLoadTriggerItem item = null;
//
//        for (ThreewayOverLoadTriggerItem e : items) {
//            StandardSportMarket marketBean = standardSportMarketMapper.selectById(e.getMarketId());
//            if (marketBean == null) throw new RuntimeException("盘口不存在");
//            e.setPlayId(marketBean.getMarketCategoryId().intValue());
//            item = map.get(getWaterHead(e));
//            if (ObjectUtils.isEmpty(item)) {
//                item = new ThreewayOverLoadTriggerItem();
//                item.setTournamentId(e.getTournamentId());
//                item.setMatchId(e.getMatchId());
//                item.setPlayId(e.getPlayId());
//                item.setMarketId(e.getMarketId());
//                item.setPlayPhaseType(e.getPlayPhaseType());
//                item.setSwitchAutoChangeRate(e.getSwitchAutoChangeRate());
//                map.put(getWaterHead(e), item);
//            } else if (e.getRollType().intValue() != NumberUtils.INTEGER_ONE.intValue()) {
//                throw new RcsServiceException("只能设置一个投注项的水差");
//            }
//            if (BaseConstants.ODD_TYPE_X.equalsIgnoreCase(e.getOddsType())) {
//                item.setTieAutoChangeRate(e.getHomeAutoChangeRate());
//            } else if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(e.getOddsType()) ||
//                    BaseConstants.ODD_TYPE_ODD.equalsIgnoreCase(e.getOddsType()) ||
//                    BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(e.getOddsType())) {
//                item.setHomeAutoChangeRate(e.getHomeAutoChangeRate());
//            } else if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(e.getOddsType()) ||
//                    BaseConstants.ODD_TYPE_EVEN.equalsIgnoreCase(e.getOddsType()) ||
//                    BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(e.getOddsType())) {
//                item.setAwayAutoChangeRate(e.getHomeAutoChangeRate());
//            }
//            list.add(item);
//        }
//        return list;
//    }

    public static String getWaterHead(ThreewayOverLoadTriggerItem item) {
        StringBuffer sb = new StringBuffer();
        sb.append(item.getMatchId()).append("_").append(item.getPlayId()).append("-").append(item.getMarketId());
        return sb.toString();
    }

    /*@Override
    public Map<Integer, List<Map<String, Object>>> queryMarketWaterHeadConfigNew(Map<String, Object> map) {
        log.info("水差查询接口入参{}", JSONObject.toJSONString(map));
        String objString = JSONObject.toJSONString(map);
        MarketLiveOddsQueryVo marketLiveOddsQueryVo = JSONObject.parseObject(objString, MarketLiveOddsQueryVo.class);
        HttpResponse<PageResult> result = marketViewController.getRollMatchList(marketLiveOddsQueryVo);
        PageResult<MatchMarketLiveOddsVo> marketList = result.getData();
        if (ObjectUtils.isEmpty(marketList)){
            log.info("没有查到对应的赛事信息{}",JSONObject.toJSONString(map));
            return new HashMap<>();
        }
        List<MatchMarketLiveOddsVo> markets = marketList.getList();
        if (CollectionUtils.isEmpty(markets)){
            log.info("没有查到赛事对应的盘口值{}",JSONObject.toJSONString(map));
            return new HashMap<>();
        }
        Integer playPhaseType = Integer.valueOf(map.get("playPhaseType").toString());
        for (MatchMarketLiveOddsVo vo : markets) {
            if (playPhaseType.intValue() != vo.getPlayPhaseType().intValue()) {
                continue;
            }
            Long matchId = vo.getMatchId();
            Long standardTournamentId = vo.getStandardTournamentId();
            Map<Integer, List<Map<String, Object>>> resultMap = new HashMap<>();
            ThreewayOverLoadTriggerItem item = new ThreewayOverLoadTriggerItem();
            item.setMatchId(vo.getMatchId());
            item.setPlayPhaseType(vo.getPlayPhaseType());
            List<ThreewayOverLoadTriggerItem> items = rcsTournamentOperateMarketMapper.queryMarketWaterHeadConfig(item);
            List<MatchMarketLiveOddsVo.MatchMarketCategoryVo> marketCategoryList = vo.getMarketCategoryList();
            for (MatchMarketLiveOddsVo.MatchMarketCategoryVo market : marketCategoryList) {
                List<MatchMarketLiveOddsVo.MatchMarketVo> matchMarketVoList = market.getMatchMarketVoList();
                Integer rollType = market.getRollType();
                List<Map<String, Object>> listMap = new ArrayList<>();
                for (MatchMarketLiveOddsVo.MatchMarketVo m : matchMarketVoList) {
                    Map<String, Object> marketMap = new HashMap<>();
                    List<MatchMarketLiveOddsVo.MatchMarketOddsFieldVo> oddsFieldsList = m.getOddsFieldsList();
                    Map<String, Object> mapOdds = new HashMap<>();
                    mapOdds.put("playId", m.getMarketCategoryId());
                    mapOdds.put("matchId", matchId);
                    mapOdds.put("tournamentId", standardTournamentId);
                    mapOdds.put("marketId", m.getId());
                    mapOdds.put("rollType", rollType);
                    mapOdds.put("diffOdds", m.diffOddsValue());
                    mapOdds.put("playPhaseType", playPhaseType);
                    List<ThreewayOverLoadTriggerItem> itemList = setWaterHead(mapOdds, oddsFieldsList, items);
                    marketMap.put("playId", m.getMarketCategoryId());
                    marketMap.put("marketId", m.getId().toString());
                    marketMap.put("order", itemList.get(0).getDiffOdds());
                    marketMap.put("list", itemList);
                    listMap.add(marketMap);
                }
                resultMap.put(rollType, listMap);
            }
            return resultMap;
        }
        return new HashMap<>();
    }*/

    private List<ThreewayOverLoadTriggerItem> setWaterHead(Map<String, Object> map, List<MatchMarketLiveOddsVo.MatchMarketOddsFieldVo> oddsFieldsList, List<ThreewayOverLoadTriggerItem> items) {
        List<ThreewayOverLoadTriggerItem> marketList = new ArrayList<>();
        for (MatchMarketLiveOddsVo.MatchMarketOddsFieldVo vo : oddsFieldsList) {
            ThreewayOverLoadTriggerItem mapOdds = new ThreewayOverLoadTriggerItem();
            for (ThreewayOverLoadTriggerItem item : items) {
                Double homeAutoChangeRate = null;
                if (item.getMarketId().longValue() == Long.valueOf(map.get("marketId").toString()).longValue()) {
                    if (BaseConstants.ODD_TYPE_X.equalsIgnoreCase(vo.getOddsType())) {
                        homeAutoChangeRate = item.getTieAutoChangeRate();
                    } else if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(vo.getOddsType()) ||
                            BaseConstants.ODD_TYPE_ODD.equalsIgnoreCase(vo.getOddsType()) ||
                            BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(vo.getOddsType())) {
                        homeAutoChangeRate = item.getHomeAutoChangeRate();
                    } else if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(vo.getOddsType()) ||
                            BaseConstants.ODD_TYPE_EVEN.equalsIgnoreCase(vo.getOddsType()) ||
                            BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(vo.getOddsType())) {
                        homeAutoChangeRate = item.getAwayAutoChangeRate();
                    }
                    mapOdds.setHomeAutoChangeRate(homeAutoChangeRate);
                    break;
                }
            }
            mapOdds.setPlayId(Integer.valueOf(map.get("playId").toString()));
            mapOdds.setMatchId((Long) map.get("matchId"));
            mapOdds.setTournamentId(Integer.valueOf(map.get("tournamentId").toString()));
            mapOdds.setMarketId((Long) map.get("marketId"));
            mapOdds.setRollType((Integer) map.get("rollType"));
            mapOdds.setDiffOdds(Long.valueOf(map.get("diffOdds").toString()));
            mapOdds.setNameExpressionValue(vo.getNameExpressionValue());
            mapOdds.setPlayPhaseType((Integer) map.get("playPhaseType"));
            mapOdds.setOddsType(vo.getOddsType());
            mapOdds.setMaxOdds(vo.getFieldOddsValue());
            marketList.add(mapOdds);
        }
        return marketList;
    }
}
