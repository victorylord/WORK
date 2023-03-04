package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.mq.SpecialSpreadCalculatePlayVO;
import com.panda.sport.rcs.pojo.mq.SpecialSpreadCalculateVO;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.service.OddsRangeService;
import com.panda.sport.rcs.trade.service.TradeCommonService;
import com.panda.sport.rcs.trade.service.TradeOddsCommonService;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.impl.RcsOddsConvertMappingServiceImpl;
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
		topic = "TRADE_SPECIAL_SPREAD_CALCULATE",
		consumerGroup = "RCS_TRADE_SPECIAL_SPREAD_CALCULATE",
		messageModel = MessageModel.CLUSTERING,
		consumeMode = ConsumeMode.CONCURRENTLY)
public class SpecialSpreadCalculateConsumer extends RcsConsumer<SpecialSpreadCalculateVO> {

	@Autowired
	RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
	@Autowired
	private StandardSportMarketMapper standardSportMarketMapper;
	@Autowired
	private StandardMatchInfoMapper standardMatchInfoMapper;
	@Autowired
	private TradeVerificationService tradeVerificationService;
	@Autowired
	private RcsTradeConfigService rcsTradeConfigService;
	@Autowired
	private TradeCommonService tradeCommonService;
	@Autowired
	private OddsRangeService oddsRangeService;
	@Autowired
	RedisClient redisClient;
	@Autowired
	private TradeOddsCommonService tradeOddsCommonService;

	@Override
	protected String getTopic() {
		return "TRADE_SPECIAL_SPREAD_CALCULATE";
	}

	@Override
	public Boolean handleMs(SpecialSpreadCalculateVO vo) {
		log.info("::{}::TRADE_SPECIAL_SPREAD_CALCULATE:{}",CommonUtil.getRequestId(vo.getMatchId()), JSONObject.toJSONString(vo));
		if (ObjectUtils.isEmpty(vo) || ObjectUtils.isEmpty(vo.getMatchId()) || CollectionUtils.isEmpty(vo.getPlays())) {
			return Boolean.TRUE;
		}
		StandardMatchInfo matchInfo = standardMatchInfoMapper.selectById(vo.getMatchId());
		if (ObjectUtils.isEmpty(matchInfo)){
			return Boolean.TRUE;
		}
		Integer matchType = NumberUtils.INTEGER_ONE;
		if (RcsConstant.LIVE_MATCH_STATUS.contains(matchInfo.getMatchStatus())) {
			matchType = NumberUtils.INTEGER_ZERO;
		}
		if (matchType.intValue() != vo.getMatchType()){
			return Boolean.TRUE;
		}
		RcsMatchMarketConfig config = new RcsMatchMarketConfig();
		config.setMatchId(vo.getMatchId());
		config.setMatchType(vo.getMatchType());
			for (SpecialSpreadCalculatePlayVO play : vo.getPlays()){
				config.setPlayId(play.getPlayId());
				config.setIsSpecialPumping(play.getIsSpecialPumping());
				config.setSpecialOddsInterval(play.getSpecialOddsInterval());
				try {
					Integer dataSource = rcsTradeConfigService.getDataSource(vo.getMatchId(), play.getPlayId());
					if (MarketUtils.isAuto(dataSource)){
						continue;
					}
					config.setDataSource(dataSource.longValue());
//					List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
					List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
					specialSpreadCalculate(playAllMarketList,config);
					List<StandardMarketDTO> dtos = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList),StandardMarketDTO.class);
					tradeCommonService.putTradeMarketOdds(config, dtos,null);
				}catch (Exception e) {
					log.error("::{}::TRADE_SPECIAL_SPREAD_CALCULATE:{}", CommonUtil.getRequestId(), e.getMessage(), e);
				}
			}
		return true;
	}
	/**
	 * @Description   //计算赔率
	 * @Param [playAllMarketList, config]
	 * @Author  sean
	 * @Date   2021/8/13
	 * @return void
	 **/
	private void specialSpreadCalculate(List<RcsStandardMarketDTO> playAllMarketList,RcsMatchMarketConfig config) {

		for (RcsStandardMarketDTO market : playAllMarketList){
			config.setOddsType(tradeVerificationService.getOddsType(market));
			log.info("::{}::specialSpreadCalculate1，marketOddsList={},config={}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(market.getMarketOddsList()),JSONObject.toJSONString(config));
			if (ObjectUtils.isEmpty(config.getIsSpecialPumping()) || config.getIsSpecialPumping() == 0){
				oddsRangeService.caluSpreadOddsBySpread(market.getMarketOddsList(),config);
			}else {
				oddsRangeService.caluSpecialOddsBySpread(market.getMarketOddsList(),config);
			}
			log.info("::{}::specialSpreadCalculate2，marketOddsList={},config={}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(market.getMarketOddsList()),JSONObject.toJSONString(config));
		}
	}

}
