package com.panda.sport.rcs.trade.mq.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.panda.merge.dto.Request;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.trade.enums.TempTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.vo.mq.PlayOddsConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 已开售的玩法，接收融合传递的数据，更新玩法赔率源
 *
 * @author carver
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "STANDARD_MARKET_CATEGORY_SELL",
        consumerGroup = "RCS_TRADE_STANDARD_MARKET_CATEGORY_SELL",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class PlaySellOddsConfigConsumer extends RcsConsumer<JSONObject> {
    @Autowired
    private RcsTournamentTemplateMapper templateMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper marginMapper;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;

    @Override
    protected String getTopic() {
        return "STANDARD_MARKET_CATEGORY_SELL";
    }

    @Override
    public Boolean handleMs(JSONObject msg) {
        log.info("::{}::STANDARD_MARKET_CATEGORY_SELL:{}",CommonUtil.getRequestId(),JSONObject.toJSONString(msg));
        try {
            if (ObjectUtil.isNotNull(msg) && ObjectUtil.isNotNull(msg.getJSONObject("data"))) {
                JSONObject obj = msg.getJSONObject("data");
                PlayOddsConfigVo vo = JSONObject.toJavaObject(obj, PlayOddsConfigVo.class);
                QueryWrapper<RcsTournamentTemplate> queryWrapper = new QueryWrapper();
                queryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId())
                        .eq(RcsTournamentTemplate::getMatchType, vo.getMatchType())
                        .eq(RcsTournamentTemplate::getTypeVal, vo.getMatchId());
                RcsTournamentTemplate template = templateMapper.selectOne(queryWrapper);
                log.info("::{}::PlaySellOddsConfigConsumer-模板:{}，玩法赔率源：{}",template.getId(), JsonFormatUtils.toJson(template), JsonFormatUtils.toJson(vo.getPlayDataSource()));
                if (ObjectUtil.isNotNull(template) && CollectionUtils.isNotEmpty(vo.getPlayDataSource())) {
                    Map<String, List<Long>> map = vo.getPlayDataSource();
                    for (String dataSource : map.keySet()) {
                        List<Long> playIds = map.get(dataSource);
                        UpdateWrapper<RcsTournamentTemplatePlayMargain> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("template_id", template.getId());
                        updateWrapper.in("play_id", playIds);
                        RcsTournamentTemplatePlayMargain playMargin = new RcsTournamentTemplatePlayMargain();
                        playMargin.setDataSource(dataSource);
                        playMargin.setUpdateTime(new Date());
                        marginMapper.update(playMargin, updateWrapper);
                    }

                    //数据源更改，发送mq推送至前端
                    Request r = new Request();
                    String linkId = UUID.randomUUID().toString().replace("-", "") + "_play_odds_config";
                    r.setLinkId(linkId);
                    r.setData(vo);
                    producerSendMessageUtils.sendMessage("RCS_CATEGORY_ODDS_CONFIG_TOPIC", linkId, String.valueOf(vo.getMatchId()), r);
                }
            }
        } catch (Exception e) {
            log.error("::{}::STANDARD_MARKET_CATEGORY_SELL:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
