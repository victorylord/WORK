package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.message.StandardMatchSwitchStatusMessage;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.constants.MqConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 早盘切滚球
 * @Author : Paca
 * @Date : 2022-06-24 20:25
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = MqConstant.Topic.STANDARD_MATCH_SWITCH_STATUS,
        consumerGroup = MqConstant.RCS_TRADE_PREFIX + MqConstant.Topic.STANDARD_MATCH_SWITCH_STATUS,
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class SwitchLiveConsumer extends RcsConsumer<Request<StandardMatchSwitchStatusMessage>> {

    @Autowired
    private TradeStatusService tradeStatusService;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    protected String getTopic() {
        return MqConstant.Topic.STANDARD_MATCH_SWITCH_STATUS;
    }

    @Override
    protected Boolean handleMs(Request<StandardMatchSwitchStatusMessage> message) {
        try {
            StandardMatchSwitchStatusMessage data = message.getData();
            Long matchId = data.getStandardMatchId();
            log.info("::{}::STANDARD_MATCH_SWITCH_STATUS:{}", matchId, JSONObject.toJSONString(data));
            Integer oddsLive = data.getOddsLive();
            if (!NumberUtils.INTEGER_ONE.equals(oddsLive)) {
                return true;
            }
            String key = RedisKey.getSwitchLiveFlagKey(matchId);
            long count = redisUtils.incrBy(key, 1L);
            if (count == 1L) {
                // 5分钟之内只能执行一次
                redisUtils.expire(key, 5L, TimeUnit.MINUTES);
                tradeStatusService.switchLive(matchId, true);
            }
        } catch (Exception e) {
            log.error("::{}::早盘切滚球异常：{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
