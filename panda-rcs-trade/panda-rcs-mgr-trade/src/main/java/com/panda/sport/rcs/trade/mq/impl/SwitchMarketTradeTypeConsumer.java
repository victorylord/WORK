package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.constants.MqConstant;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.TradeModeService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(
        topic = MqConstant.Topic.RCS_MARKET_TRADE_TYPE,
        consumerGroup = MqConstant.Topic.RCS_MARKET_TRADE_TYPE,
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class SwitchMarketTradeTypeConsumer extends RcsConsumer<MarketStatusUpdateVO> {

    @Autowired
    private TradeModeService tradeModeService;

    @Override
    protected String getTopic() {
        return MqConstant.Topic.RCS_MARKET_TRADE_TYPE;
    }

    @Override
    public Boolean handleMs(MarketStatusUpdateVO msg) {
        try {
            log.info("::{}::RCS_MARKET_TRADE_TYPE:{}",CommonUtil.getRequestId(msg.getMatchId()), JSONObject.toJSONString(msg));
            tradeModeService.updateTradeMode(msg);
        } catch (Exception e) {
            log.error("::{}::RCS_MARKET_TRADE_TYPE:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }

}
