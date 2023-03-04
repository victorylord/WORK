package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.message.StandardMarketMessage;
import com.panda.merge.dto.message.StandardMatchMarketMessage;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.constants.MqConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.enums.YesNoEnum;
import com.panda.sport.rcs.factory.BeanFactory;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.BuildMarketService;
import com.panda.sport.rcs.trade.service.LinkageCommonService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.vo.MatchStatusAndDataSuorceVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <a href="http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=32369340">联动模式</a>
 *
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : L联动模式
 * @Author : Paca
 * @Date : 2021-04-13 11:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = MqConstant.Topic.STANDARD_MARKET_ODDS,
        consumerGroup = MqConstant.RCS_TRADE_PREFIX + MqConstant.Topic.STANDARD_MARKET_ODDS,
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class LinkageModeConsumer extends RcsConsumer<Request<StandardMatchMarketMessage>> {

    @Autowired
    private BuildMarketService buildMarketService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private LinkageCommonService linkageCommonService;

    @Autowired
    protected ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    protected String getTopic() {
        return MqConstant.Topic.STANDARD_MARKET_ODDS;
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        consumer.setConsumeThreadMin(64);
        consumer.setConsumeThreadMax(128);
    }

    private boolean msgCheck(Request<StandardMatchMarketMessage> msg) {
        if ("HeartBeat".equalsIgnoreCase(msg.getDataType())) {
            log.warn("心跳数据不做处理！");
            return false;
        }
        Long dataSourceTime = msg.getDataSourceTime();
        if (dataSourceTime == null) {
            log.warn("没有带时间戳，当前数据不做更新！");
            return false;
        }
        StandardMatchMarketMessage matchMsg = msg.getData();
        if (matchMsg == null) {
            log.warn("赔率数据为空！");
            return false;
        }
        Long sportId = matchMsg.getSportId();
        Long matchId = matchMsg.getStandardMatchInfoId();
        if (SportIdEnum.BASKETBALL.isNo(sportId)) {
            log.warn("::{}::篮球才有L联动模式",matchId);
            return false;
        }
        if (CollectionUtils.isEmpty(matchMsg.getMarketList())) {
            log.warn("::{}::无盘口数据",matchId);
            return false;
        }
        return true;
    }

    private void mainMarketCache(String linkId, Long dataSourceTime, Long matchId, Integer matchType, Map<Long, StandardMarketMessage> mainMarketMap) {
        String key = RedisKey.MainMarket.getLinkageMainMarketInfoKey(matchId);
        mainMarketMap.forEach((playId, mainMarket) -> {
            Long subPlayId = mainMarket.getChildMarketCategoryId();
            String dataSourceTimeKey = RedisKey.MainMarket.getLinkageDataSourceTimeKey(matchId, playId, subPlayId);
            String oldDataSourceTime = redisUtils.get(dataSourceTimeKey);
            if (NumberUtils.toLong(oldDataSourceTime) > dataSourceTime) {
                log.warn("::{}::缓存时间大于当前数据时间，老数据不处理：playId={},subPlayId={},linkId={}", matchId, playId, subPlayId, linkId);
                return;
            }
            redisUtils.set(dataSourceTimeKey, String.valueOf(dataSourceTime));
            redisUtils.expire(dataSourceTimeKey, 5L, TimeUnit.MINUTES);

            String hashKey = RedisKey.MainMarket.getLinkageMainMarketInfoHashKey(playId, subPlayId);
            JSONObject jsonObject = (JSONObject) JSON.toJSON(mainMarket);
            jsonObject.put("linkId", linkId);
            jsonObject.put("dataSourceTime", dataSourceTime);
            redisUtils.hset(key, hashKey, jsonObject.toJSONString());
        });
        if (NumberUtils.INTEGER_ZERO.equals(matchType)) {
            redisUtils.expire(key, 3L, TimeUnit.DAYS);
        } else {
            redisUtils.expire(key, 30L, TimeUnit.DAYS);
        }
    }

    private boolean linkageCheck(Long matchId, Long playId, Basketball.Linkage linkage) {
        if (linkage == null) {
            log.warn("::{}::盘口不匹配：playId={}", matchId, playId);
            return false;
        }
        if (!tradeStatusService.isLinkage(matchId, linkage.getTotalT1()) && !tradeStatusService.isLinkage(matchId, linkage.getTotalT2())) {
            log.warn("::{}::对应球队总分玩法不是联动模式：playId={},t1={},t2={}", matchId, playId, linkage.getTotalT1(), linkage.getTotalT2());
            return false;
        }
        return true;
    }

    private boolean pre2live(Long matchId, Integer matchType) {
        String key = String.format(RedisKey.ODDS_MATCH_TYPE_KEY, matchId);
        String oldMatchType = redisUtils.get(key);
        String newMatchType = String.valueOf(matchType);
        log.info("::{}::早盘/滚球标志：oldMatchType={},newMatchType={}",matchId, oldMatchType, newMatchType);
        boolean pre2live = (StringUtils.isBlank(oldMatchType) || "1".equals(oldMatchType)) && "0".equals(newMatchType);
        redisUtils.set(key, newMatchType);
        redisUtils.expire(key, 1L, TimeUnit.DAYS);
        return pre2live;
    }

    private void clearChuZhangWarnSign(Long matchId, Integer matchType, List<StandardMarketMessage> marketList, String linkId) {
        String key = RedisKey.getChuZhangWarnSignKey(matchId, matchType);
        Map<String, String> hashMap = redisUtils.hgetAll(key);
        log.info("::{}::货量出涨预警标志：matchType={},key={},hashMap={}", matchId, matchType, key, JSON.toJSONString(hashMap));
        if (CollectionUtils.isEmpty(hashMap)) {
            return;
        }
        String matchWarnSign = hashMap.get(matchId.toString());
        if (!YesNoEnum.Y.getCode().equals(matchWarnSign)) {
            return;
        }
        List<String> clearKeyList = Lists.newArrayList();
        Map<Long, List<StandardMarketMessage>> groupByPlayIdMap = marketList.stream().collect(Collectors.groupingBy(StandardMarketMessage::getMarketCategoryId));
        groupByPlayIdMap.forEach((playId, list) -> {
            Map<Long, List<StandardMarketMessage>> groupBySubPlayIdMap = list.stream().collect(Collectors.groupingBy(StandardMarketMessage::getChildMarketCategoryId));
            groupBySubPlayIdMap.forEach((subPlayId, subList) -> {
                String playKey = playId + "_" + subPlayId;
                String playWarnSign = hashMap.get(playKey);
                if (YesNoEnum.Y.getCode().equals(playWarnSign) && isClearWarnSign(subList)) {
                    clearKeyList.add(playKey);
                }
            });
        });
        if (CollectionUtils.isEmpty(clearKeyList)) {
            return;
        }
        boolean isClearAllWarnSign = true;
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (k.equals(matchId.toString()) || clearKeyList.contains(k)) {
                continue;
            }
            if (YesNoEnum.Y.getCode().equals(v)) {
                isClearAllWarnSign = false;
            }
        }
        if (isClearAllWarnSign) {
            clearKeyList.add(matchId.toString());
        }
        Map<String, String> chuZhangWarnSignMap = Maps.newHashMap();
        clearKeyList.forEach(clearKey -> chuZhangWarnSignMap.put(clearKey, YesNoEnum.N.getCode()));
        MatchStatusAndDataSuorceVo wsMq = BeanFactory.chuZhangWarnSignWsInfo(SportIdEnum.BASKETBALL.getId(), matchId, chuZhangWarnSignMap, linkId + "_clearChuZhang");
        producerSendMessageUtils.sendMessage(MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TOPIC, MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TAG, wsMq.getLinkId(), wsMq);
        String[] fields = clearKeyList.toArray(new String[0]);
        redisUtils.hdel(key, fields);
        log.info("::{}::清除货量出涨预警标志：key={},fields={}",matchId, key, JSON.toJSONString(fields));
    }

    private boolean isClearWarnSign(List<StandardMarketMessage> list) {
        // 所有盘口数据源关盘 或者 所有未数据源关盘的盘口 位置状态都是开
        list = list.stream().filter(market -> !TradeStatusEnum.isClose(market.getThirdMarketSourceStatus())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            // 所有盘口数据源关盘，清除标志
            return true;
        }
        list = list.stream().filter(market -> !TradeStatusEnum.isOpen(market.getPlaceNumStatus())).collect(Collectors.toList());
        // 所有盘口位置状态都是开，清除标志
        return CollectionUtils.isEmpty(list);
    }

    @Override
    public Boolean handleMs(Request<StandardMatchMarketMessage> msg) {
        try {
            if (!msgCheck(msg)) {
                return true;
            }
            String linkId = msg.getLinkId();
            log.info("::{}::STANDARD_MARKET_ODDS:{}",linkId,JSONObject.toJSONString(msg));
            Long dataSourceTime = msg.getDataSourceTime();
            StandardMatchMarketMessage matchMsg = msg.getData();
            Long sportId = matchMsg.getSportId();
            Long matchId = matchMsg.getStandardMatchInfoId();
            List<StandardMarketMessage> marketList = matchMsg.getMarketList();
            Integer matchType = marketList.get(0).getMarketType();
            boolean pre2live = pre2live(matchId, matchType);
            clearChuZhangWarnSign(matchId, matchType, marketList, linkId);
            Map<Long, StandardMarketMessage> mainMarketMap = getSourceMainMarket(marketList);
            if (CollectionUtils.isEmpty(mainMarketMap)) {
                return true;
            }
            mainMarketCache(linkId, dataSourceTime, matchId, matchType, mainMarketMap);
            Set<Basketball.Linkage> marketValueChangeSet = new HashSet<>();
            Set<Basketball.Linkage> statusChangeSet = new HashSet<>();
            mainMarketMap.forEach((playId, mainMarket) -> {
                Basketball.Linkage linkage = Basketball.Linkage.getBySourcePlayId(playId);
                if (!linkageCheck(matchId, playId, linkage)) {
                    return;
                }
                String marketValue = mainMarket.getAddition1();
                String marketType = String.valueOf(mainMarket.getMarketType());
                String statusSign = linkageCommonService.generateStatusSign(mainMarket);
                String marketSign = linkageCommonService.generateMarketSign(dataSourceTime, mainMarket, linkId);
                String oldMarketSign = linkageCommonService.getMarketSignFromRedis(matchId, playId);
                log.info("::{}::新旧盘口标志：playId={},marketSign={},oldMarketSign={}", matchId, playId, marketSign, oldMarketSign);
                if (StringUtils.isNotBlank(oldMarketSign)) {
                    String[] array = oldMarketSign.split("_");
                    Long oldDataSourceTime = NumberUtils.toLong(array[0]);
                    String oldMarketValue = array[1];
                    String oldStatusSign = array[2];
                    String oldMarketType = array[3];
                    if (oldDataSourceTime > dataSourceTime) {
                        log.warn("::{}::缓存时间大于当前数据时间，老数据不处理：playId={},oldDataSourceTime={},dataSourceTime={}", matchId, playId, oldDataSourceTime, dataSourceTime);
                        return;
                    }
                    linkageCommonService.setMarketSignToRedis(matchId, playId, marketSign);
                    if (!StringUtils.equals(marketValue, oldMarketValue) || !StringUtils.equals(marketType, oldMarketType)) {
                        // 盘口值不相等/早盘到滚球盘口类型不相等，需要重新计算
                        marketValueChangeSet.add(linkage);
                        return;
                    }
                    if (!StringUtils.equals(statusSign, oldStatusSign)) {
                        // 状态不相等，需要处理状态
                        statusChangeSet.add(linkage);
                    }
                } else {
                    marketValueChangeSet.add(linkage);
                    linkageCommonService.setMarketSignToRedis(matchId, playId, marketSign);
                }
            });
            log.info("::{}::联动模式开始计算：marketValueChangeSet={},statusChangeSet={}",matchId, marketValueChangeSet, statusChangeSet);
            if (CollectionUtils.isEmpty(marketValueChangeSet) && CollectionUtils.isEmpty(statusChangeSet)) {
                return true;
            }
            Set<Basketball.Linkage> linkages = new HashSet<>();
            if (CollectionUtils.isNotEmpty(marketValueChangeSet)) {
                linkages.addAll(marketValueChangeSet);
            }
            if (CollectionUtils.isNotEmpty(statusChangeSet)) {
                linkages.addAll(statusChangeSet);
            }
            buildMarketService.linkageBuildMarket(sportId, matchId, linkages, pre2live);
        } catch (Exception e) {
            log.error("::{}::STANDARD_MARKET_ODDS:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }

    private Map<Long, StandardMarketMessage> getSourceMainMarket(List<StandardMarketMessage> marketList) {
        return marketList.stream()
                // 获取源玩法主盘口
                .filter(market -> Basketball.Linkage.isSourcePlay(market.getMarketCategoryId()) && NumberUtils.INTEGER_ONE.equals(market.getPlaceNum()))
                .collect(Collectors.toMap(StandardMarketMessage::getMarketCategoryId, market -> {
                    if (market.getThirdMarketSourceStatus() == null) {
                        market.setThirdMarketSourceStatus(TradeStatusEnum.OPEN.getStatus());
                    }
                    if (market.getPaStatus() == null) {
                        market.setPaStatus(TradeStatusEnum.OPEN.getStatus());
                    }
                    if (market.getPlaceNumStatus() == null) {
                        market.setPlaceNumStatus(TradeStatusEnum.OPEN.getStatus());
                    }
                    if (market.getStatus() == null) {
                        market.setStatus(TradeStatusEnum.OPEN.getStatus());
                    }
                    return market;
                }));
    }

}