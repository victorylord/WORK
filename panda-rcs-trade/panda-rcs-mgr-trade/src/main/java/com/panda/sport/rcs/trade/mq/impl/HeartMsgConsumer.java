package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 数据源心跳
 *
 * @author black
 */
@Component
@Slf4j
@RocketMQMessageListener(
		topic = "STANDARD_HEARTBEAT",
		consumerGroup = "RCS_TRADE_STANDARD_HEARTBEAT",
		messageModel = MessageModel.CLUSTERING,
		consumeMode = ConsumeMode.CONCURRENTLY)
public class HeartMsgConsumer extends RcsConsumer<JSONObject> {

	@Autowired
	private RedisClient redisClient;

	private static String redisKey = "rcs:heart:datasource";

	@Override
	protected String getTopic() {
		return "STANDARD_HEARTBEAT";
	}

	@Override
	public Boolean handleMs(JSONObject msg) {
		try {
			log.info("::{}::STANDARD_HEARTBEAT:{}",CommonUtil.getRequestId(),JSONObject.toJSONString(msg));
			String dataSourceCode = msg.getJSONObject("data").getString("dataSourceCode");
			Long time = msg.getJSONObject("data").getLong("timestamp");
			redisClient.hSet(redisKey, dataSourceCode, String.valueOf(time));
		}catch (Exception e) {
			log.error("::{}::STANDARD_HEARTBEAT:{}", CommonUtil.getRequestId(), e.getMessage(), e);
		}
		return true;
	}

   
}
