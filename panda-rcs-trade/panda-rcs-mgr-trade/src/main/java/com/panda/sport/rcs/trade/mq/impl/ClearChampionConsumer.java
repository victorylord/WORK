package com.panda.sport.rcs.trade.mq.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.TradeClearDiffValueDTO;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.RcsMatchMarketProbabilityConfigMapper;
import com.panda.sport.rcs.mapper.RcsStandardOutrightMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsStandardOutrightMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.enums.FootBallPlayEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.vo.statistics.BalanceVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 盘口位置变化消息通知
 *
 * @author black
 */
@Component
@Slf4j
@RocketMQMessageListener(
		topic = "RCS_CLEAR_CHAMPION_MARKET",
		consumerGroup = "RCS_TRADE_RCS_CLEAR_CHAMPION_MARKET_GROUP",
		messageModel = MessageModel.CLUSTERING,
		consumeMode = ConsumeMode.CONCURRENTLY)
public class ClearChampionConsumer extends RcsConsumer<ClearDTO> {

	@Autowired
	RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
	@Autowired
	private StandardSportMarketMapper standardSportMarketMapper;
	@Autowired
	private RcsMatchMarketProbabilityConfigMapper rcsMatchMarketProbabilityConfigMapper;
	@Autowired
	private ConsumetUtil consumetUtil;
	@Reference(check = false, lazy = true, retries = 3, timeout = 5000)
	private ITradeMarketConfigApi tradeMarketConfigApi;
	@Autowired
	private ProducerSendMessageUtils producerSendMessageUtils;
	@Autowired
	RedisClient redisClient;
	@Autowired
	RcsStandardOutrightMatchInfoMapper rcsStandardOutrightMatchInfoMapper;

	@Override
	protected String getTopic() {
		return "RCS_CLEAR_CHAMPION_MARKET";
	}

	@Override
	public Boolean handleMs(ClearDTO clearDTO) {
		try {
			log.info("::{}::playIds={},RCS_CLEAR_CHAMPION_MARKET:{}", CommonUtil.getRequestId(clearDTO.getMatchId()),clearDTO.getPlayIds(), JsonFormatUtils.toJson(clearDTO));
			if (null==clearDTO||CollectionUtils.isEmpty(clearDTO.getList())) {
				return Boolean.TRUE;
			}
			//三项盘自动模式数据源赔率变化清除  累计和概率差
			//进球事件，早盘进滚球，数据源切换清除水差 概率差相关数据
			//多项盘才清除投注项
			//1.来自融合的自动清理标识  2.手动清理标识   3，task比分清理标识  4.赛前切滚球清识标识  5.数据源切换清理标识
			Integer clearType=clearDTO.getClearType();
			Long matchId=clearDTO.getMatchId();
			List<ClearSubDTO> list = clearDTO.getList();
			RcsStandardOutrightMatchInfo rcsStandardOutrightMatchInfo = rcsStandardOutrightMatchInfoMapper.selectById(matchId);
			clearDTO.setSportId(rcsStandardOutrightMatchInfo.getSportId());
			if(null==clearType){
				clearType = 0;
			}
			if(1 ==clearType||2 ==clearType){
				//平衡值累积清理
				playMarketOddsClear(list, clearType, rcsStandardOutrightMatchInfo, matchId);
				// 清空配置水差
				//clearMarketDiff(list, clearType);
				//本地数据库清理数据库概率差
				clearLocalProbability(list, clearType);
				//清空融合数据
				//clearRonhe(list, clearType);
			}
		} catch (Exception e) {
			log.error("::{}::RCS_CLEAR_CHAMPION_MARKET:{}",CommonUtil.getRequestId(),e.getMessage(),e);
		}
		return true;
	}

	/**
	 * 本地数据库清理数据库概率差
	 * @param list
	 * @param clearType
	 */
	private void clearLocalProbability(List<ClearSubDTO> list, Integer clearType) {
		if(!(1==clearType||2==clearType||5==clearType)){
			for (ClearSubDTO rcsMatchMarketConfig : list) {
				rcsMatchMarketConfig.setOddsType(null);
			}
		}
		log.info("::{}::clearLocalProbability{}","RTRCCMG_"+list.get(0).getMatchId()+"_"+clearType,JsonFormatUtils.toJson(list));
		rcsMatchMarketProbabilityConfigMapper.updateProbabilityBySelectivetToZero(list);
	}

	/**
	 * 清空融合数据
	 * @param list
	 * @param clearType
	 */
	private void clearRonhe(List<RcsMatchMarketConfig> list, Integer clearType) {
		TradeClearDiffValueDTO diffValueDTO = new TradeClearDiffValueDTO();
		diffValueDTO.setSportId(NumberUtils.INTEGER_ONE);
		diffValueDTO.setStandardMatchId(list.get(NumberUtils.INTEGER_ZERO).getMatchId());
		List<Long> categoryList = list.stream().map(e -> e.getPlayId()).collect(Collectors.toList());
		diffValueDTO.setCategoryList(categoryList);
		DataRealtimeApiUtils.handleApi(diffValueDTO, new DataRealtimeApiUtils.ApiCall() {
			@Override
			@Trace
			public <R> Response<R> callApi(Request request) {
				return tradeMarketConfigApi.clearDiffValue(request);
			}
		});
	}

	/**
	 * 清空配置水差
	 * 三项多项盘没有水差
	 * @param list
	 * @param clearType
	 */
	private void clearMarketDiff(List<ClearSubDTO> list, Integer clearType) {
		rcsMatchMarketConfigMapper.clearMarketDiffByMatchAndPlay(list);
	}

	/**
	 * 平衡值累积清理
	 * @param list
	 * @param clearType
	 * @param standardMatchInfo
	 * @param matchId
	 */
	private void playMarketOddsClear(List<ClearSubDTO> list, Integer clearType, RcsStandardOutrightMatchInfo standardMatchInfo, Long matchId) {
		log.info("::{}::playMarketOddsClear","RTRCCMG_"+standardMatchInfo.getId()+"_"+clearType);
		QueryWrapper<StandardSportMarket> queryWrapper = new QueryWrapper();
		queryWrapper.lambda().eq(StandardSportMarket :: getStandardMatchInfoId , list.get(NumberUtils.INTEGER_ZERO).getMatchId());
		if(!(4==clearType||5==clearType)){
			List<Long> playids = list.stream().map(e -> e.getPlayId()).collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(playids)){
				queryWrapper.lambda().in(StandardSportMarket::getMarketCategoryId, playids);
			}
			List<Long> marketIds = list.stream().map(e -> e.getMarketId()).collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(marketIds)){
				queryWrapper.lambda().in(StandardSportMarket::getId, marketIds);
			}
		}
		List<StandardSportMarket> markets = standardSportMarketMapper.selectList(queryWrapper);
		consumetUtil.setPlaceNum(markets);
		if (!CollectionUtils.isEmpty(markets)){
			if (ObjectUtils.isEmpty(standardMatchInfo.getStandrdOutrightMatchBegionTime())) {
				standardMatchInfo.setStandrdOutrightMatchBegionTime(System.currentTimeMillis());
			}
			String dateExpect = DateUtils.getDateExpect(standardMatchInfo.getStandrdOutrightMatchBegionTime());
			for (StandardSportMarket market : markets){
				//清理平衡值累积
				if(1==clearType||2==clearType){
					for (ClearSubDTO rcsMatchMarketConfig : list) {
						if(market.getId().longValue()==rcsMatchMarketConfig.getMarketId()){
							consumetUtil.clearChampionBalanceValue(dateExpect, market,clearType,rcsMatchMarketConfig.getOddsType());

							BalanceVo balanceVo = new BalanceVo(matchId, market.getMarketCategoryId(), market.getId());
							balanceVo.setPlaceNum(market.getPlaceNum());
							balanceVo.setMarketId(market.getId());
							balanceVo.setBalanceValue(NumberUtils.LONG_ZERO);
							balanceVo.setCurrentSide(FootBallPlayEnum.getOddsType(rcsMatchMarketConfig.getPlayId()));
							//log.info("::{}::平衡值存入MQ消息队列:{}","RTRCCMG_"+standardMatchInfo.getId()+"_"+clearType,JsonFormatUtils.toJson(balanceVo));
							producerSendMessageUtils.sendMessage(MqConstants.REALTIME_SYNC_BALANCE_TOPIC, MqConstants.REALTIME_SYNC_BALANCE_TAG, "", balanceVo);
						}
					}
				}else {
					consumetUtil.clearChampionBalanceValue(dateExpect, market,clearType,null);
					BalanceVo balanceVo = new BalanceVo(matchId, market.getMarketCategoryId(), market.getId());
					balanceVo.setPlaceNum(market.getPlaceNum());
					balanceVo.setMarketId(market.getId());
					balanceVo.setBalanceValue(NumberUtils.LONG_ZERO);
					balanceVo.setCurrentSide(FootBallPlayEnum.getOddsType(market.getMarketCategoryId()));
					//log.info("::{}::平衡值存入MQ消息队列:{}","RTRCCMG_"+standardMatchInfo.getId()+"_"+clearType,JsonFormatUtils.toJson(balanceVo));
					producerSendMessageUtils.sendMessage(MqConstants.REALTIME_SYNC_BALANCE_TOPIC, MqConstants.REALTIME_SYNC_BALANCE_TAG, "", balanceVo);
				}
			}
		}
	}




}
