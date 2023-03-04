package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.panda.sport.rcs.constants.MqConstant;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.pojo.MatchPeriod;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsMarketCategorySetRelationService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.trade.mq.impl
 * @Description : 修改盘口状态消费类
 * @Author : Paca
 * @Date : 2020-08-09 14:12
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = MqConstant.Topic.RCS_TRADE_UPDATE_MARKET_STATUS,
        consumerGroup = MqConstant.Topic.RCS_TRADE_UPDATE_MARKET_STATUS,
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class UpdateMarketStatusConsumer extends RcsConsumer<JSONObject> {

    @Autowired
    protected RcsMarketCategorySetRelationService rcsMarketCategorySetRelationService;
    @Autowired
    protected RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private TradeStatusService tradeStatusService;

    @Override
    protected String getTopic() {
        return MqConstant.Topic.RCS_TRADE_UPDATE_MARKET_STATUS;
    }

    @Override
    public Boolean handleMs(JSONObject jsonMsg) {
        try {
            JSONObject data = jsonMsg.getJSONObject("data");
            if (data == null) {
                log.warn("修改盘口状态消费失败，数据为空");
                return true;
            }
            log.info("::{}::RCS_TRADE_UPDATE_MARKET_STATUS:{}", CommonUtil.getRequestId(), JSONObject.toJSONString(data));
            Integer tradeLevel = data.getInteger("tradeLevel");
            Long matchId = data.getLong("matchId");
            List<Long> playIds = null;
            JSONArray playIdList = data.getJSONArray("playIdList");
            if (CollectionUtils.isNotEmpty(playIdList)) {
                playIds = playIdList.toJavaList(Long.class);
            }
            Integer status = data.getInteger("status");
            Integer linkedType = data.getInteger("linkedType");
            String remark = data.getString("remark");
            Integer sourceCloseFlag = data.getInteger("sourceCloseFlag");
            if (TradeLevelEnum.isScoreEvent(tradeLevel)) {
                if (!data.containsKey("matchPeriod")) {
                    log.info("修改操盘状态消费失败：参数[matchPeriod]不存在");
                    return true;
                }
                MatchPeriod matchPeriod = data.getObject("matchPeriod", MatchPeriod.class);
                if (matchPeriod == null) {
                    log.info("修改操盘状态消费失败：参数[matchPeriod]为空");
                    return true;
                }
                if (CollectionUtils.isEmpty(playIdList)) {
                    log.info("修改操盘状态消费失败：参数[playIdList]为空");
                    return true;
                }
                MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
                updateVO.setTradeLevel(tradeLevel);
                updateVO.setMatchId(matchId);
                updateVO.setCategoryIdList(playIds);
                updateVO.setMarketStatus(status);
                updateVO.setUpdateUserId(-1);
                updateVO.setLinkedType(linkedType);
                updateVO.setRemark(remark);
                String linkId = tradeStatusService.updateTradeStatusEvent(updateVO, matchPeriod);
                log.info("修改操盘状态消费完成：linkId=" + linkId);
                return true;
            }
//            if (TradeLevelEnum.isPlaySetLevel(tradeLevel) && LinkedTypeEnum.EVENT_PLAY_SET.getCode().equals(linkedType)) {
//                eventPlaySetSeal(data);
//                return true;
//            }
            MarketStatusUpdateVO vo = new MarketStatusUpdateVO()
                    .setTradeLevel(tradeLevel)
                    .setMatchId(matchId)
                    .setCategoryId(data.getLong("playId"))
                    .setSubPlayId(data.getLong("subPlayId"))
                    .setMarketPlaceNum(data.getInteger("placeNum"))
                    .setCategorySetId(data.getLong("playSetId"))
                    .setPlaySetCode(data.getString("playSetCode"))
                    .setMarketStatus(status)
                    .setLinkedType(linkedType)
                    .setRemark(remark)
                    .setMatchType(data.getInteger("matchType"))
                    .setCategoryIdList(playIds)
                    .setSourceCloseFlag(sourceCloseFlag);
            if (LinkedTypeEnum.DATA_PROVIDER.getCode().equals(vo.getLinkedType())) {
                vo.setRemark(LinkedTypeEnum.DATA_PROVIDER.getRemark());
            }
            String linkId = tradeStatusService.updateTradeStatus(vo);
            log.info("修改操盘状态消费完成：linkId=" + linkId);
        } catch (Exception e) {
            log.error("::{}::修改盘口状态消费异常：{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }

    /**
     * 事件触发玩法集封盘，只封盘A模式玩法
     *
     * @param data
     */
    private void eventPlaySetSeal(JSONObject data) {
        Long matchId = data.getLong("matchId");
        Long playSetId = data.getLong("playSetId");
        Integer status = data.getInteger("status");
        Integer linkedType = data.getInteger("linkedType");
        String remark = data.getString("remark");
        // 通过玩法集ID查询所有下级玩法ID
        List<Long> playIds = rcsMarketCategorySetRelationService.getCategoryIdByCategorySetId(playSetId);
        if (CollectionUtils.isEmpty(playIds)) {
            log.warn("::{}::玩法集下无玩法：playSetId={}", matchId, playSetId);
            return;
        }
        Map<Long, Integer> tradeModeMap = rcsTradeConfigService.getTradeMode(matchId, playIds);
        playIds = playIds.stream().filter(playId -> {
            Integer tradeMode = tradeModeMap.getOrDefault(playId, TradeEnum.AUTO.getCode());
            return TradeEnum.isAuto(tradeMode);
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(playIds)) {
            log.warn("::{}::玩法集下无A模式玩法：playSetId={}", matchId, playSetId);
            return;
        }
        MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO()
                .setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel())
                .setMatchId(matchId)
                .setCategoryIdList(playIds)
                .setMarketStatus(status)
                .setLinkedType(linkedType)
                .setRemark(remark);
        String linkId = tradeStatusService.updateTradeStatus(updateVO);
        log.info("::{}::事件触发玩法集封盘，只封盘A模式玩法：linkId=" + linkId,matchId);
    }
}
