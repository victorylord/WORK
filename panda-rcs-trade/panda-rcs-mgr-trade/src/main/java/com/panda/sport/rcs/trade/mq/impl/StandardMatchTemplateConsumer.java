package com.panda.sport.rcs.trade.mq.impl;

import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainRefMapper;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 融合下发标准赛事id，风控下发赛事模板给融合
 *
 * @author forever
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "STANDARD_MATCH_TEMPLATE",
        consumerGroup = "RCS_TRADE_STANDARD_MATCH_TEMPLATE",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class StandardMatchTemplateConsumer extends RcsConsumer<List<Long>> {

    @Autowired
    private RcsTournamentTemplatePlayMargainRefMapper playMargainRefMapper;
    @Override
    protected String getTopic() {
        return "STANDARD_MATCH_TEMPLATE";
    }

    @Override
    public Boolean handleMs(List<Long> matchIds) {
        String linkId = com.panda.sport.rcs.utils.CommonUtils.getLinkId();
        log.info("::{}::-融合下发标准赛事id，风控下发赛事模板给融合，matchIds：{}", linkId, JsonFormatUtils.toJson(matchIds));
        try {
            int result = playMargainRefMapper.updatePlayMargainRefStatusByMatchIds(matchIds,3);
            log.info("::{}::-融合下发标准赛事id，风控下发赛事模板给融合，matchIds:{}，修改结果:{}", linkId, JsonFormatUtils.toJson(matchIds), result);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(linkId), e.getMessage(), e);
        }
        return true;
    }
}
