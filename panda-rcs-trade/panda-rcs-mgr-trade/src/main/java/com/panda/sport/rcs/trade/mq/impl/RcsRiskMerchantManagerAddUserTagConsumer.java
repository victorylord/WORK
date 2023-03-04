package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerStatusEnum;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.IRiskMerchantManagerService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.userprofile.AddUserTagVo;
import com.panda.sport.rcs.trade.vo.userprofile.UserBetTagChangeReqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * @description: 定时任务标签审核消费
 * @author: lithan
 * @create: 2022-03-29 12:05
 **/
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "rcs_risk_merchant_manager_task_tag",
        consumerGroup = "rcs_risk_merchant_manager_task_tag_group",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class RcsRiskMerchantManagerAddUserTagConsumer extends RcsConsumer<AddUserTagVo> {


    @Autowired
    private IRiskMerchantManagerService riskMerchantManagerService;

    @Override
    protected String getTopic() {
        return "rcs_risk_merchant_manager_task_tag";
    }

    @Override
    public Boolean handleMs(AddUserTagVo data) {
        log.info("::{}::rcs_risk_merchant_manager_task_tag:{}",CommonUtil.getRequestId(data.getUserId()),JSONObject.toJSONString(data));
        try {
            riskMerchantManagerService.initRiskMerchantManager(Long.valueOf(data.getUserId()), RiskMerchantManagerTypeEnum.Type_7.getCode(), data.getTag().getTagName(), data.getTag().getTagName(),
                    "定时任务自动化标签", JSONObject.toJSONString(data), RiskMerchantManagerStatusEnum.Type_0.getCode());
            return true;

        } catch (Exception e) {
            log.error("::{}::rcs_risk_merchant_manager_task_tag:{}", CommonUtil.getRequestId(data.getUserId()), e.getMessage(), e);
        }
        return true;
    }
}
