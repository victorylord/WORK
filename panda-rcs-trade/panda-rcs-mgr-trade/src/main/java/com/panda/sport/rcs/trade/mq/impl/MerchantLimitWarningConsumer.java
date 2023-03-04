package com.panda.sport.rcs.trade.mq.impl;

import com.panda.sport.rcs.pojo.RcsMerchantLimitWarning;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsMerchantLimitWarningService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-03-06 16:25
 **/
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "rcs_merchant_limit_warning_data",
        consumerGroup = "rcs_merchant_limit_warning_data",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class MerchantLimitWarningConsumer extends RcsConsumer<RcsMerchantLimitWarning> {
    @Autowired
    private RcsMerchantLimitWarningService rcsMerchantLimitWarningService;

    @Override
    protected String getTopic() {
        return "rcs_merchant_limit_warning_data";
    }

    @Override
    public Boolean handleMs(RcsMerchantLimitWarning rcsMerchantLimitWarning) {
        try {
            rcsMerchantLimitWarningService.save(rcsMerchantLimitWarning);
            return true;
        } catch (Exception e) {
            log.error("::{}::服务器错误{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return false;
        }
    }
}
