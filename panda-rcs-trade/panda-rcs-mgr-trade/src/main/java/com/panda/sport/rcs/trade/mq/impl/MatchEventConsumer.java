package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.merge.dto.message.MatchEventInfoMessage;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.log.annotion.monnitor.MonitorAnnotion;

import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.apache.skywalking.apm.toolkit.trace.TraceCrossThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-order-group
 * @Package Name :  com.panda.sport.rcs.predict.mq
 * @Description :  TODO
 * @Date: 2022-03-13 11:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
@Slf4j
@MonitorAnnotion(code = "RCS_EVENT_TO_ORDER_TOPIC")
@TraceCrossThread
@RocketMQMessageListener(
        topic = "RCS_EVENT_TO_ORDER_TOPIC",
        consumerGroup = "TRADE_RCS_EVENT_TO_ORDER_TOPIC",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.ORDERLY)
public class MatchEventConsumer implements RocketMQListener<MatchEventInfoMessage>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    private RedisClient redisClient;
    private static final String RCS_GOAL_TIME_REDIS_KEY = "rcs:goal:time:redis:key:matchId:%s:sportId:%s";
    private static Long endTime = 20000L;

    @Override
    public void onMessage(MatchEventInfoMessage data) {
        log.info("::{}::RCS_EVENT_TO_ORDER_TOPIC:{}", CommonUtil.getRequestId(), JSONObject.toJSONString(data));
        String time = String.format(RCS_GOAL_TIME_REDIS_KEY, data.getStandardMatchId(), data.getSportId());
        if (StringUtils.equals(String.valueOf(data.getSportId()), "1") && StringUtils.equals("goal", data.getEventCode()) && data.getCanceled() == 0) {
            log.info("::赛事:{},开始处理进球时间", data.getStandardMatchId());
            //进球时间缓存
            redisClient.setExpiry(time, System.currentTimeMillis(), endTime);

        } else if (StringUtils.equals(String.valueOf(data.getSportId()), "1") && StringUtils.equals("goal", data.getEventCode()) && data.getCanceled() == 1) {
            log.info("::赛事:{},开始处理取消进球时间", data.getStandardMatchId());
            //删除时间key
            redisClient.delete(time);
        }
    }
    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        consumer.setConsumeThreadMin(64);
        consumer.setConsumeThreadMax(256);
    }
}
