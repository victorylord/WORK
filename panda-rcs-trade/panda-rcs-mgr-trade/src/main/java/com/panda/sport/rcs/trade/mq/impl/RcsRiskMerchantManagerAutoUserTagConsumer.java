package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerStatusEnum;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.IRiskMerchantManagerService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.userprofile.AddUserTagVo;
import com.panda.sport.rcs.vo.riskmerchantmanager.UserChangeTagVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 1912 自动化标签-实时标签V1.0
 * @author: Magic
 * @create: 2022-07-11 17:05
 **/
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "rcs_risk_merchant_manager_task_auto_tag",
        consumerGroup = "rcs_risk_merchant_manager_task_auto_tag_group",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class RcsRiskMerchantManagerAutoUserTagConsumer extends RcsConsumer<UserChangeTagVo> {


    @Autowired
    private IRiskMerchantManagerService riskMerchantManagerService;

    @Override
    protected String getTopic() {
        return "rcs_risk_merchant_manager_task_auto_tag";
    }

    @Override
    public Boolean handleMs(UserChangeTagVo data) {
        log.info("::{}::rcs_risk_merchant_manager_task_auto_tag:{}",CommonUtil.getRequestId(data.getUserId()),JSONObject.toJSONString(data));
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("type", 1);
            map.put("remark", data.getRemark());
            map.put("userId", data.getUserId());
            map.put("tagType", 2);
            map.put("tagId", data.getTagId());
            riskMerchantManagerService.initRiskMerchantManager(data.getUserId(), RiskMerchantManagerTypeEnum.Type_1.getCode(), data.getTagName(), data.getTagName(), data.getSupplementExplain(), JSONObject.toJSONString(map), RiskMerchantManagerStatusEnum.Type_0.getCode());

        } catch (Exception e) {
            log.error("::{}::rcs_risk_merchant_manager_task_auto_tag:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
