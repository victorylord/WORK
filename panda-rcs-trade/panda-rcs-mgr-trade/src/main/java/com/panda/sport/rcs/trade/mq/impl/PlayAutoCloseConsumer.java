package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Maps;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.message.StandardCategoryAutoCloseMessage;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.constants.MqConstant;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.enums.YesNoEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.trade.mq.impl
 * @Description : 玩法自动关盘
 * @Author : Paca
 * @Date : 2020-10-30 15:13
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = MqConstant.Topic.STANDARD_CATEGORY_AUTOCLOSE,
        consumerGroup = MqConstant.RCS_TRADE_PREFIX + MqConstant.Topic.STANDARD_CATEGORY_AUTOCLOSE,
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class PlayAutoCloseConsumer extends RcsConsumer<Request<StandardCategoryAutoCloseMessage>> {

    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    protected String getTopic() {
        return MqConstant.Topic.STANDARD_CATEGORY_AUTOCLOSE;
    }

    @Override
    public Boolean handleMs(Request<StandardCategoryAutoCloseMessage> msg) {
        log.info("::{}::STANDARD_CATEGORY_AUTOCLOSE:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(msg));
        try {
            StandardCategoryAutoCloseMessage data = msg.getData();
            Long matchId = data.getStandardMatchId();
            List<Long> playIds = data.getStandardCategoryList();
            StandardMatchInfo matchInfo = standardMatchInfoService.getById(matchId);
            if (matchInfo == null) {
                throw new RcsServiceException("赛事不存在：" + matchId);
            }
            Long sportId = matchInfo.getSportId();
            MarketStatusUpdateVO vo = new MarketStatusUpdateVO();
            vo.setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel());
            vo.setSportId(sportId);
            vo.setMatchType(RcsConstant.getMatchType(matchInfo));
            vo.setMatchId(matchId);
            vo.setCategoryIdList(playIds);
            vo.setMarketStatus(TradeStatusEnum.CLOSE.getStatus());
            if (SportIdEnum.isFootball(sportId)) {
                // 足球自动关盘改为收盘
                vo.setEndFlag(YesNoEnum.Y.getValue());
            }
            vo.setSourceCloseFlag(YesNoEnum.Y.getValue());
            vo.setLinkedType(LinkedTypeEnum.AUTO_CLOSE.getCode());
            vo.setRemark(LinkedTypeEnum.AUTO_CLOSE.getRemark());
            cacheAutoCloseStatus(sportId, matchId, playIds);
            String linkId = tradeStatusService.updateTradeStatus(vo);
            log.info("玩法自动关盘：matchId={},linkId={}", matchId, linkId);
        } catch (Exception e) {
            log.error("::{}::STANDARD_CATEGORY_AUTOCLOSE{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }

    /**
     * 缓存联动普通玩法自动关盘状态，自动关盘的玩法不再联动
     *
     * @param sportId
     * @param matchId
     * @param playIds
     */
    private void cacheAutoCloseStatus(Long sportId, Long matchId, List<Long> playIds) {
        if (!SportIdEnum.isBasketball(sportId)) {
            return;
        }
        Map<String, String> map = Maps.newHashMap();
        Basketball.Linkage.getNormalPlay().forEach(linkage -> linkage.getTargetPlayIds().forEach(playId -> {
            if (playIds.contains(playId)) {
                map.put(playId.toString(), TradeStatusEnum.CLOSE.getStatus().toString());
            }
        }));
        if (CollectionUtils.isNotEmpty(map)) {
            String key = RedisKey.getAutoCloseStatusKey(matchId);
            redisUtils.hmset(key, map);
            redisUtils.expire(key, 90L, TimeUnit.DAYS);
        }
    }
}
