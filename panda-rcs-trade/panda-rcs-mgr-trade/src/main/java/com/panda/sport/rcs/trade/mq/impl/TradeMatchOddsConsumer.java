package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.odds.MatchOddsConfigService;
import com.panda.sport.rcs.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 根据配置重新计算赔率数据，只针对手动模式
 *
 * @author black
 * @ClassName: TradeMatchOddsConsumer
 * @Description: TODO
 * @date 2020年11月13日 上午11:19:52
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "RCS_TRADE_MATCH_ODDS_CONFIG",
        consumerGroup = "RCS_TRADE_MATCH_ODDS_CONFIG",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class TradeMatchOddsConsumer extends RcsConsumer<MatchOddsConfig> {

    @Autowired
    private MatchOddsConfigService matchOddsConfigService;

    @Override
    protected String getTopic() {
        return "RCS_TRADE_MATCH_ODDS_CONFIG";
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        consumer.setConsumeThreadMin(64);
        consumer.setConsumeThreadMax(128);
    }

    @Override
    public Boolean handleMs(MatchOddsConfig matchConfig) {
        CommonUtils.mdcPut();
        try {
            log.info("::{}::RCS_TRADE_MATCH_ODDS_CONFIG:{}", CommonUtil.getRequestId(matchConfig.getMatchId()), JSONObject.toJSONString(matchConfig));
            matchOddsConfigService.matchOddsConfig(matchConfig);
        } catch (Exception e) {
            log.error("::{}::{}",matchConfig.getLinkId(),e.getMessage(),e);
        }finally {
            CommonUtils.mdcRemove();
        }
        return true;
    }
}
