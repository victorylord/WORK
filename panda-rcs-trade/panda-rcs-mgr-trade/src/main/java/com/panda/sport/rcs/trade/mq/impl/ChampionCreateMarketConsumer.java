package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.mapper.RcsStandardOutrightMatchInfoMapper;
import com.panda.sport.rcs.mapper.RcsTradeConfigMapper;
import com.panda.sport.rcs.pojo.RcsStandardOutrightMatchInfo;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 冠军赛事手动创建盘口，融合下发mq通知风控记录入库
 *
 * @author carver
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "INIT_MARKET_TRADETYPE_API",
        consumerGroup = "RCS_TRADE_INIT_MARKET_TRADETYPE_API",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class ChampionCreateMarketConsumer extends RcsConsumer<JSONObject> {

    @Autowired
    private RcsStandardOutrightMatchInfoMapper standardOutrightMatchInfoMapper;
    @Autowired
    private RcsTradeConfigMapper rcsTradeConfigMapper;

    @Override
    protected String getTopic() {
        return "INIT_MARKET_TRADETYPE_API";
    }

    @Override
    public Boolean handleMs(JSONObject msg) {
        try {
            JSONObject obj = msg.getJSONObject("data");
            String standardMatchId = obj.getString("standardMatchId");
            String marketId = obj.getString("relationMarketId");
            RcsStandardOutrightMatchInfo outrightMatchInfo = standardOutrightMatchInfoMapper.selectById(standardMatchId);
            if (ObjectUtils.isEmpty(outrightMatchInfo)) {
                log.info("冠军赛事手动创建盘口-冠军赛事不存在:{}",CommonUtil.getRequestId(standardMatchId), JsonFormatUtils.toJson(msg));
            } else {
                log.info("::{}::INIT_MARKET_TRADETYPE_API:{}",CommonUtil.getRequestId(standardMatchId),JSONObject.toJSONString(msg));
                //操作记录保存
                RcsTradeConfig tradeConfig = new RcsTradeConfig()
                        .setMatchId(standardMatchId)
                        .setTraderLevel(TradeLevelEnum.MARKET.getLevel())
                        .setTargerData(marketId)
                        .setDataSource(NumberUtils.INTEGER_ONE.intValue())  //默认手动
                        .setStatus(NumberUtils.INTEGER_ZERO.intValue());  //默认开盘
                rcsTradeConfigMapper.insert(tradeConfig);
            }
        } catch (Exception e) {
            log.error("::{}::INIT_MARKET_TRADETYPE_API:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
