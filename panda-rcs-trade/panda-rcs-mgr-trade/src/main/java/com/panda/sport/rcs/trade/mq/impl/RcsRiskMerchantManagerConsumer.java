package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerStatusEnum;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.IRiskMerchantManagerService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.userprofile.UserBetTagChangeReqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * @description: 投注特征预警审核消费
 * @author: lithan
 * @create: 2022-03-29 12:05
 **/
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "rcs_risk_merchant_manager_tag",
        consumerGroup = "rcs_risk_merchant_manager_tag_group",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class RcsRiskMerchantManagerConsumer extends RcsConsumer<UserBetTagChangeReqVo> {


    @Autowired
    private IRiskMerchantManagerService riskMerchantManagerService;

    @Override
    protected String getTopic() {
        return "rcs_risk_merchant_manager_tag";
    }

    @Override
    public Boolean handleMs(UserBetTagChangeReqVo data) {
        try {
            log.info("::{}::rcs_risk_merchant_manager_tag:{}",CommonUtil.getRequestId(),JSONObject.toJSONString(data));
            if (!ObjectUtils.isEmpty(data.getSubmitType()) && data.getSubmitType().equals(1)) {
                //需要提交到商户后台审核
                riskMerchantManagerService.initRiskMerchantManager(Long.valueOf(data.getUserId()), RiskMerchantManagerTypeEnum.Type_6.getCode(), data.getChangeTagName(), data.getChangeTagName(),
                        data.getSupplementExplain(), JSONObject.toJSONString(data), RiskMerchantManagerStatusEnum.Type_0.getCode());
                return true;
            } else if (!ObjectUtils.isEmpty(data.getSubmitType()) && data.getSubmitType().equals(2)) {
                //强制执行，不需要审核
                riskMerchantManagerService.initRiskMerchantManager(Long.valueOf(data.getUserId()), RiskMerchantManagerTypeEnum.Type_6.getCode(), data.getChangeTagName(), data.getChangeTagName(),
                        data.getSupplementExplain(), JSONObject.toJSONString(data), RiskMerchantManagerStatusEnum.Type_3.getCode());
            }
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
