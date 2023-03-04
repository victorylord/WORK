package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.panda.sport.rcs.pojo.RcsUserConfigNew;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsUserConfigNewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Description : 根据用户id修改玩家组 消费
 * @Author : lithan
 * @Date : 2022-4-11 20:44:28
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "rcs_risk_user_odds_group_task_send",
        consumerGroup = "rcs_risk_user_odds_group_task_send_group",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class UserOddsGroupUpdateConsumer extends RcsConsumer<JSONObject> {

    @Autowired
    IRcsUserConfigNewService rcsUserConfigNewService;

    @Override
    protected String getTopic() {
        return "rcs_risk_user_odds_group_task_send";
    }

    @Override
    public Boolean handleMs(JSONObject msg) {
        try {
            log.info("进入rcs_risk_user_odds_group_task_send消费者:{}", msg.toString());
            Long userId = msg.getLong("userId");
            Integer groupId = msg.getInteger("groupId");

            LambdaUpdateWrapper<RcsUserConfigNew> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(RcsUserConfigNew::getTagMarketLevelId, groupId);
            lambdaUpdateWrapper.set(RcsUserConfigNew::getUpdateTime,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            lambdaUpdateWrapper.eq(RcsUserConfigNew::getUserId, userId);
            rcsUserConfigNewService.update(lambdaUpdateWrapper);
            log.info("rcs_risk_user_odds_group_task_send更新成功:{}", msg.toString());
        } catch (Exception e) {
            log.error("::{}::rcs_risk_user_odds_group_task_send异常：{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
