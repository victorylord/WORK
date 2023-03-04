package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.pojo.MatchPeriod;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.TradeCommonService;
import com.panda.sport.rcs.trade.service.TradeOddsCommonService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.utils.MarketUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.panda.sport.rcs.enums.PlayStateEnum.e;

/**
 * 盘口位置变化消息通知
 *
 * @author black
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "RCS_TASK_DATASOURCE_MARKET_CLOSE",
        consumerGroup = "RCS_TASK_DATASOURCE_MARKET_CLOSE",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class AutoCloseMarketConsumer extends RcsConsumer<MatchPeriod> {

    @Autowired
    RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private TradeCommonService tradeCommonService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;
    @Autowired
    RedisClient redisClient;

	@Override
	protected String getTopic() {
		return "RCS_TASK_DATASOURCE_MARKET_CLOSE";
	}

	@Override
    public Boolean handleMs(MatchPeriod map) {
			log.info("::{}::RCS_TASK_DATASOURCE_MARKET_CLOSE:{}",CommonUtil.getRequestId(map.getStandardMatchId()), JSONObject.toJSONString(map));
			if (ObjectUtils.isEmpty(map) || CollectionUtils.isEmpty(map.getCategoryIds())) {
				return Boolean.TRUE;
			}
			RcsMatchMarketConfig config = new RcsMatchMarketConfig();
			config.setMatchId(map.getStandardMatchId());
			String subPlayId = "0";
			for (Long playId : map.getCategoryIds()){
				try {

					config.setPlayId(playId);
					if (TradeConstant.BASKETBALL_X_PLAYS.contains(playId.intValue()) ||
							TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(playId.intValue()) ||
							TradeConstant.FOOTBALL_X_NO_INSERT_PLAYS.contains(playId.intValue())){
						Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
						if (MarketUtils.isAuto(dataSource)){
							continue;
						}
						String[] scorce = new String[]{"0","0"};
						if (Lists.newArrayList(336,28,30,109,110,357).contains(playId.intValue())){
							scorce = map.getScore().split(":");
						}else if (Lists.newArrayList(225,120,125,230,224).contains(playId.intValue())){
							scorce = map.getCornerScore().split(":");
						}else if (Lists.newArrayList(224).contains(playId.intValue())){
							if (ObjectUtils.isEmpty(map.getYellowCardScore())){
								scorce = map.getYellowCardScore().split(":");
							}
							if (ObjectUtils.isEmpty(map.getRedCardScore())){
								String[] red = map.getRedCardScore().split(":");
								scorce[0] = Integer.parseInt(scorce[0]) + Integer.parseInt(red[0]) + "";
								scorce[1] = Integer.parseInt(scorce[1]) + Integer.parseInt(red[1]) + "";
							}
						}else if (Lists.newArrayList(235).contains(playId.intValue())){
							scorce = map.getExtraTimeScore().split(":");
						}else if (Lists.newArrayList(133,237).contains(playId.intValue())){
							scorce = map.getPenaltyShootout().split(":");
						}else if (TradeConstant.FOOTBALL_X_A3_PLAYS.contains(playId.intValue())){
							scorce[0] = new BigDecimal(map.getSecondsFromStart() / (60.0*15)).setScale(NumberUtils.INTEGER_ZERO,BigDecimal.ROUND_HALF_UP).toPlainString();
						}
						subPlayId = playId * 100 + Integer.parseInt(scorce[0])+Integer.parseInt(scorce[1]) +"";
//						List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
						List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
						if (!CollectionUtils.isEmpty(playAllMarketList)){
							for (RcsStandardMarketDTO market :playAllMarketList){
								if (market.getChildStandardCategoryId() <= Long.parseLong(subPlayId)){
									market.setThirdMarketSourceStatus(NumberUtils.INTEGER_TWO);
								}
							}
							List<StandardMarketDTO> marketList = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList),StandardMarketDTO.class);
							tradeCommonService.putTradeMarketOdds(config, marketList,null);
						}
					}
				}catch (Exception e) {
					log.error("::{}::RCS_TASK_DATASOURCE_MARKET_CLOSE:{}", CommonUtil.getRequestId(map.getStandardMatchId()), e.getMessage(), e);
				}
			}
		return true;
	}

}
