package com.panda.rcs.warning.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.message.StandardMatchStatusMessage;
import com.panda.rcs.warning.mapper.RcsMatchMonitorListMapper;
import com.panda.rcs.warning.mapper.RcsMatchMonitorMqLicenseMapper;
import com.panda.rcs.warning.vo.RcsMatchMonitorList;
import com.panda.rcs.warning.vo.RcsMatchMonitorMqLicense;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = MqConstants.STANDARD_MATCH_STATUS,
        consumerGroup = "RCS_WARNING_STANDARD_MATCH_STATUS_GROUP",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class MatchStatusConsumer implements RocketMQListener<String>, RocketMQPushConsumerLifecycleListener {
    @Autowired
    private RcsMatchMonitorListMapper rcsMatchMonitorListMapper;
    @Autowired
    private RcsMatchMonitorMqLicenseMapper rcsMatchMonitorMqLicenseMapper;
    @Autowired
    private RedisClient redisClient;

    @Override
    public void onMessage(String str) {

        if (StringUtils.isBlank(str)) {
            return;
        }

        Request<StandardMatchStatusMessage> request = JSON.parseObject(str, new TypeReference<Request<StandardMatchStatusMessage>>() {
        });

        StandardMatchStatusMessage standardMatchStatusMessage = request.getData();
        log.info("::{}::STANDARD_MATCH_STATUS:{}",standardMatchStatusMessage.getStandardMatchId(),JSON.toJSONString(standardMatchStatusMessage));
        //滚球
        String key = String.format("rcs:error:log:matchType:1:match:%s", standardMatchStatusMessage.getStandardMatchId());
        if (standardMatchStatusMessage.getMatchStatus() == 1 && !StringUtils.equals(String.valueOf(standardMatchStatusMessage.getMatchStatus()), redisClient.get(key))) {
            LambdaQueryWrapper<RcsMatchMonitorList> updateQuery = new LambdaQueryWrapper<>();
            //删除早盘的数据
            updateQuery.eq(RcsMatchMonitorList::getMatchId, standardMatchStatusMessage.getStandardMatchId()).eq(RcsMatchMonitorList::getMatchType, 1);
            rcsMatchMonitorListMapper.delete(updateQuery);
            rcsMatchMonitorMqLicenseMapper.delete(new LambdaQueryWrapper<RcsMatchMonitorMqLicense>().eq(RcsMatchMonitorMqLicense::getMatchId, standardMatchStatusMessage.getStandardMatchId()).eq(RcsMatchMonitorMqLicense::getMatchType, 1));
            redisClient.set(key, standardMatchStatusMessage.getMatchStatus());
            redisClient.expireKey(key, 3 * 60 * 60);
        }
        String endKey = String.format("rcs:error:log:matchType:3:match:%s", standardMatchStatusMessage.getStandardMatchId());
        if ((standardMatchStatusMessage.getMatchStatus() == 3 || standardMatchStatusMessage.getMatchStatus() == 4) && !StringUtils.equals(String.valueOf(standardMatchStatusMessage.getMatchStatus()), redisClient.get(endKey))) {
            LambdaQueryWrapper<RcsMatchMonitorList> updateQuery = new LambdaQueryWrapper<>();
            //删除
            updateQuery.eq(RcsMatchMonitorList::getMatchId, standardMatchStatusMessage.getStandardMatchId());
            rcsMatchMonitorListMapper.delete(updateQuery);
            rcsMatchMonitorMqLicenseMapper.delete(new LambdaQueryWrapper<RcsMatchMonitorMqLicense>().eq(RcsMatchMonitorMqLicense::getMatchId, standardMatchStatusMessage.getStandardMatchId()));
            redisClient.set(endKey, standardMatchStatusMessage.getMatchStatus());
            redisClient.expireKey(endKey, 3 * 60 * 60);
        }


    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        consumer.setConsumeThreadMin(32);
        consumer.setConsumeThreadMax(64);
    }
}
