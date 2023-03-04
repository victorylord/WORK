package com.panda.sport.rcs.trade.mq.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
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
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.dto.StandardCategoryIdsDiffDTO;
import com.panda.sport.rcs.pojo.enums.FootBallPlayEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.LiveStandardMatchMarketMessageVO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 足球AO面板Apply后清除足球所有玩法水差
 *
 * @author black
 */
@Component
@Slf4j
@RocketMQMessageListener(
		topic = "STANDARD_CATEGORYID_CLEAR_DIFF_RISK",
		consumerGroup = "STANDARD_CATEGORYID_CLEAR_DIFF_RISK_GROUP")
public class ClearMatchAllPlayWaterDiffConsumer extends RcsConsumer<Request<StandardCategoryIdsDiffDTO>> {

	@Autowired
	RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;

	@Reference(check = false, lazy = true, retries = 3, timeout = 5000)
	private ITradeMarketConfigApi tradeMarketConfigApi;

	@Autowired
	RedisClient redisClient;

	@Autowired
	RcsStandardOutrightMatchInfoMapper rcsStandardOutrightMatchInfoMapper;

	@Override
	protected String getTopic() {
		return "STANDARD_CATEGORYID_CLEAR_DIFF_RISK";
	}

	@Override
	public Boolean handleMs(Request<StandardCategoryIdsDiffDTO> requestData) {
		try {
			String linkId = requestData.getLinkId();
			StandardCategoryIdsDiffDTO categoryIdsDiffDTO = requestData.getData();
			log.info("::{}::,::{}::STANDARD_CATEGORYID_CLEAR_DIFF_RISK:{}", linkId,categoryIdsDiffDTO.getStandardCategoryIds(), JsonFormatUtils.toJson(categoryIdsDiffDTO));
			if (null==categoryIdsDiffDTO||CollectionUtils.isEmpty(categoryIdsDiffDTO.getStandardCategoryIds())) {
				return Boolean.TRUE;
			}
			Long matchId=categoryIdsDiffDTO.getStandardMatchId();
			Long sportId=categoryIdsDiffDTO.getSportId();
			List<Long> standardCategoryIds = new ArrayList<>();
			if(!CollectionUtils.isEmpty(categoryIdsDiffDTO.getStandardCategoryIds())){
				standardCategoryIds = new ArrayList<>(categoryIdsDiffDTO.getStandardCategoryIds());
			}
			// 清空水差
			clearMarketDiff(standardCategoryIds, matchId);
			
			//调用融合清除水差触发赔率下发
			clearRonhe(standardCategoryIds, matchId,sportId);

		} catch (Exception e) {
			log.error("::{}::STANDARD_CATEGORYID_CLEAR_DIFF_RISK:{}",requestData.getLinkId(),e.getMessage(),e);
		}
		return true;
	}

	/**
	 * 调用融合清除水差触发赔率下发
	 * @param categoryList
	 * @param matchId
	 * @param sportId
	 */
	private void clearRonhe(List<Long> categoryList,Long matchId,Long sportId) {
		TradeClearDiffValueDTO diffValueDTO = new TradeClearDiffValueDTO();
		diffValueDTO.setSportId(sportId.intValue());
		diffValueDTO.setStandardMatchId(matchId);
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
	 * 清空水差
	 * @param list
	 */
	private void clearMarketDiff(List<Long> list,Long matchId) {
		List<ClearSubDTO> clearSubDTOList = new ArrayList();
		for (Long playId : list) {
			ClearSubDTO clearSubDTO = new ClearSubDTO();
			clearSubDTO.setPlayId(playId);
			clearSubDTO.setMatchId(matchId);
			clearSubDTOList.add(clearSubDTO);
		}
		List<List<ClearSubDTO>> partition = Lists.partition(clearSubDTOList, 500);
		for (List<ClearSubDTO> clearSubDTOS : partition) {
			rcsMatchMarketConfigMapper.clearMarketDiffByMatchAndPlay(clearSubDTOS);
		}
		redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_WATER,matchId));
	}

}
