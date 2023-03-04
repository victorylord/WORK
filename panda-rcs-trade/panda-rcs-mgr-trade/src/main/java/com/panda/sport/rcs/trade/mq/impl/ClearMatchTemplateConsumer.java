package com.panda.sport.rcs.trade.mq.impl;

import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.tournament.TournamentTemplateService;
import com.panda.sport.data.rcs.dto.tournament.TournamentTemplateDTO;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

/**
 * 融合合并或取消关联赛事，清除赛事模板
 *
 * @author carver
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "DELETE_SELL_MATCH",
        consumerGroup = "RCS_TRADE_DELETE_SELL_MATCH",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class ClearMatchTemplateConsumer extends RcsConsumer<List<TournamentTemplateDTO>> {
    @Autowired
    private TournamentTemplateService tournamentTemplateService;

    @Override
    protected String getTopic() {
        return "DELETE_SELL_MATCH";
    }

    @Override
    public Boolean handleMs(List<TournamentTemplateDTO> list) {
        String linkId = UUID.randomUUID().toString().replace("-", "");
        log.info("::{}::，DELETE_SELL_MATCH：{}", linkId, JsonFormatUtils.toJson(list));
        try {
            if (!CollectionUtils.isEmpty(list)) {
                list.forEach(o -> {
                    //根据赛事id，清除早盘和滚球赛事模板
                    MatchTypeEnum[] businessModeEnums = MatchTypeEnum.values();
                    for (MatchTypeEnum businessModeEnum : businessModeEnums) {
                        TournamentTemplateDTO dto = new TournamentTemplateDTO();
                        dto.setSportId(o.getSportId());
                        dto.setMatchType(businessModeEnum.getId());
                        dto.setStandardMatchId(o.getStandardMatchId());
                        Request<TournamentTemplateDTO> request = new Request<>();
                        request.setData(dto);
                        request.setGlobalId(linkId);
                        tournamentTemplateService.putMatchTemplateCancel(request);
                    }
                });
            }
        } catch (Exception e) {
            log.error("::{}::DELETE_SELL_MATCH:{}", linkId, e.getMessage(), e);
        }
        return true;
    }
}
