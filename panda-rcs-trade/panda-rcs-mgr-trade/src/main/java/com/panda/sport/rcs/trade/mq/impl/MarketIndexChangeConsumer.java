package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mq.utils.ConsumerAdapter;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOperateMarketService;
import com.panda.sport.rcs.vo.MatchMarketLiveOddsVo;
import com.panda.sport.rcs.vo.MatchMarketLiveOddsVo.MatchMarketVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 盘口位置变化消息通知
 *
 * @author black
 */
@Component
@Slf4j
public class MarketIndexChangeConsumer extends ConsumerAdapter<List<MatchMarketVo>> {

	@Autowired
	private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
	@Autowired
	RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
	@Autowired
	private StandardSportMarketMapper standardSportMarketMapper;
	@Autowired
	private StandardMatchInfoMapper standardMatchInfoMapper;
	@Autowired
	private ProducerSendMessageUtils producerSendMessageUtils;
	@Autowired
	private RedisClient redisClient;
	@Autowired
	private RcsOddsConvertMappingService rcsOddsConvertMappingService;
	@Autowired
	RcsTournamentOperateMarketService rcsTournamentOperateMarketService;

	private final static String WS_UPDATE_MARKET_INDEX_TOPIC = "WS_UPDATE_MARKET_INDEX_TOPIC";
	public static final String DATE_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

//	private Map<String,String> oddsMap;

	public MarketIndexChangeConsumer() {
		super("TRADE_MARKET_INDEX_CHANGE", "");
//		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//
//		Runnable task = () -> {
//			try {
//				this.fixMarketConfig();
//			}catch (Exception e){
//				log.error(e.getMessage(),e);
//			}
//		};
//
//		executor.scheduleWithFixedDelay(task, 10, 10, TimeUnit.SECONDS);
	}
	/**
	 * @Description   //定时处理赛事不同步数据
	 * @Param []
	 * @Author  Sean
	 * @Date  11:13 2020/10/3
	 * @return void
	 **/
//	private void fixMarketConfig() {
//		List<RcsMatchMarketConfig> list = rcsMatchMarketConfigMapper.selectIncompleteList();
//		for (RcsMatchMarketConfig config : list){
//			config.setActive(NumberUtils.INTEGER_ZERO);
//			rcsMatchMarketConfigMapper.updateActive(config);
//			//4:最大投注最大赔付缓存更新
//			rcsTournamentOperateMarketService.sendRcsDataMq(config.getTournamentId(), config.getPlayId() + "", config.getMarketId().toString(), config.getMatchId().toString());
//		}
//	}

	@Override
	public Boolean handleMs(List<MatchMarketVo> msg, Map<String, String> paramsMap) throws Exception {
		try {
			log.info("::{}::WS_UPDATE_MARKET_INDEX_TOPIC:msg:{},map:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(msg),JSONObject.toJSONString(paramsMap));
			List<Long> ids = Lists.newArrayList();
//			List<RcsMatchMarketConfig> configs = Lists.newArrayList();
			msg.forEach(e -> ids.add(e.getMarketId()));
//			// 获取赛事id
			StandardSportMarket market = new StandardSportMarket();
			String matchId = paramsMap.get("matchId");
			if (StringUtils.isNotBlank(matchId)){
				market.setStandardMatchInfoId(Long.parseLong(matchId));
//				QueryWrapper<StandardMatchInfo> queryWrapper = new QueryWrapper<>();
//				queryWrapper.lambda().eq(StandardMatchInfo ::getId,matchId);
//				StandardMatchInfo info = standardMatchInfoMapper.selectOne(queryWrapper);
//				if (!ObjectUtils.isEmpty(info)){
//					market.setStandardTournamentId(info.getStandardTournamentId());
//				}
			}else {
				String match = redisClient.get(String.format(TradeConstant.RCS_TRADE_MARKET_S_MATCH,ids.get(0)));
				if (StringUtils.isNotBlank(match)){
					market.setStandardMatchInfoId(Long.parseLong(match));
				}else {
					market = standardSportMarketMapper.selectTournamentAndMatchById(ids.get(0));
					redisClient.setExpiry(String.format(TradeConstant.RCS_TRADE_MARKET_S_MATCH,ids.get(0)),market.getStandardMatchInfoId(), 24 * 60 * 60L);
				}
				// 没有赛事id需要根据盘口id查询
			}
//			Long standardMatchInfoId = market.getStandardMatchInfoId();
//			msg.forEach(e -> {
//				RcsMatchMarketConfig config = new RcsMatchMarketConfig();
//				config.setMarketIndex(e.getMarketIndex());
//				config.setPlayId(e.getMarketCategoryId());
//				config.setMatchId(standardMatchInfoId);
//			});
			// 查询原盘口配置
//			configs = rcsMatchMarketConfigMapper.selectMarketListByIds(configs);
			// 标识没有同步到联赛数据的盘口
//			market.setStatus(NumberUtils.INTEGER_ZERO);
//			if (ObjectUtils.isEmpty(market) ||
//					ObjectUtils.isEmpty(market.getStandardMatchInfoId()) ||
//					ObjectUtils.isEmpty(market.getStandardTournamentId())) {
//				log.info("盘口id查不到赛事或联赛信息数据{}", ids.get(0));
//				market.setStatus(NumberUtils.INTEGER_ONE);
//			}

//			rcsMatchMarketConfigService.updateMarketConfigFromDataCenter(msg, configs,ids,market);

			// 发送最新盘口到页面
//			StandardSportMarket m = market;
//			msg.forEach(e -> {
//				RcsMatchMarketConfig marketConfig =  queryMarketData(e,configs,msg,m);
//				String key = UuidUtils.generateUuid();
//				log.info("发送新盘口到页面key={}",key);
//				producerSendMessageUtils.sendMessage(WS_UPDATE_MARKET_INDEX_TOPIC, null,key, marketConfig);
//			});
            RcsMatchMarketConfig marketConfig=new RcsMatchMarketConfig();
            for (MatchMarketVo matchMarketVo:msg){
                if (matchMarketVo.getMarketIndex()!=null &&matchMarketVo.getMarketIndex()==1){
                    marketConfig.setMarketId(matchMarketVo.getMarketId());
                    marketConfig.setMarketIndex(matchMarketVo.getMarketIndex());
                    marketConfig.setPlayId(matchMarketVo.getMarketCategoryId());
                    marketConfig.setSubPlayId(matchMarketVo.getChildMarketCategoryId());
                    marketConfig.setMatchId(market.getStandardMatchInfoId());
                    String key = UuidUtils.generateUuid();
                  	producerSendMessageUtils.sendMessage(WS_UPDATE_MARKET_INDEX_TOPIC, null,key, marketConfig);
                }
            }
		} catch (Exception e) {
			log.error("::{}::WS_UPDATE_MARKET_INDEX_TOPIC:{}", CommonUtil.getRequestId(), e.getMessage(), e);
		}
		return true;
	}

	public static void main(String[] args) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("id", "test_2");
//		map.put("name", "name_2");
//		map.put("age", 22);
//
//		System.out.println(JSONObject.toJSONString(map));
//		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//
//		Runnable task = () -> {
//			try {
//				TimeUnit.SECONDS.sleep(10);
//				System.out.println("Scheduling: " + System.nanoTime());
//			}
//			catch (InterruptedException e) {
//				System.err.println("task interrupted");
//			}
//		};
//
//		executor.scheduleWithFixedDelay(task, 10, 10, TimeUnit.SECONDS);

	}

	private RcsMatchMarketConfig queryMarketData(Long marketId,List<RcsMatchMarketConfig> configs,List<MatchMarketVo> msg,StandardSportMarket market) {
		RcsMatchMarketConfig config = new RcsMatchMarketConfig();
		config.setMarketId(marketId);
//		// 没有赛事id需要根据盘口id查询
//		StandardSportMarket market = standardSportMarketMapper.selectTournamentAndMatchById(marketId);
//		if (ObjectUtils.isEmpty(market) || ObjectUtils.isEmpty(market.getStandardMatchInfoId())) {
//			log.info("盘口id查不到赛事数据{}", marketId);
//			return new RcsMatchMarketConfig();
//		}
		// 设置水差
//		for (RcsMatchMarketConfig marketConfig : configs){
//			if (marketConfig.getMarketId().longValue() == marketId.longValue()){
//				config.setAwayAutoChangeRate(marketConfig.getAwayAutoChangeRate());
//			}
//		}
		// 设置位置
		for (MatchMarketVo vo : msg) {
			if (vo.getMarketId().longValue() == marketId.longValue()) {
				config.setMarketIndex(vo.getMarketIndex());
				config.setPlayId(vo.getMarketCategoryId());
				//设置赔率列表和margin
				setOddsList(config, vo);
				config.setMatchId(market.getStandardMatchInfoId());
				config.setMarketType(MarketKindEnum.Malaysia.getValue());
				String addition1 = vo.getAddition1();
				if (StringUtils.isNotBlank(addition1) && com.panda.sport.rcs.common.NumberUtils.isNumber(addition1)) {
					if (Double.parseDouble(addition1) >= 0) {
						config.setAwayMarketValue(new BigDecimal(addition1));
						config.setHomeMarketValue(new BigDecimal("0"));
					} else if (Double.parseDouble(addition1) < 0) {
						config.setHomeMarketValue(new BigDecimal(addition1).multiply(new BigDecimal(-1)));
						config.setAwayMarketValue(new BigDecimal("0"));
					}
				}
			}
		}
		return config;
	}

	private void setOddsList(RcsMatchMarketConfig config,MatchMarketVo vo) {
		List<Map<String, Object>> oddsList = Lists.newArrayList();
//		if (MapUtils.isEmpty(oddsMap)){
//			QueryWrapper<RcsOddsConvertMapping> queryWrapper = new QueryWrapper<>();
//			List<RcsOddsConvertMapping> list = rcsOddsConvertMappingMapper.selectList(queryWrapper);
//			oddsMap = list.stream().collect(Collectors.toMap(e -> e.getEurope(),e ->e.getMalaysia()));
//		}
		for (MatchMarketLiveOddsVo.MatchMarketOddsFieldVo odds : vo.getOddsFieldsList()){
			Map<String, Object> map = JSONObject.parseObject(JSONObject.toJSONString(odds),Map.class);
			BigDecimal originalOddsValue = new BigDecimal(odds.getFieldOddsOriginValue());
			originalOddsValue = originalOddsValue.divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),2,BigDecimal.ROUND_DOWN);
			String oddsMY = rcsOddsConvertMappingService.getMyOdds(originalOddsValue.toPlainString());
			if (StringUtils.isEmpty(oddsMY)){
				log.info("欧赔{}没有知道对应的马来赔率",originalOddsValue.toPlainString());
				oddsMY = "0";
			}
			BigDecimal fieldOddsValue = new BigDecimal(odds.getFieldOddsValue());
			fieldOddsValue = fieldOddsValue.divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),2,BigDecimal.ROUND_DOWN);
			String fieldOddsValueStr = rcsOddsConvertMappingService.getMyOdds(fieldOddsValue.toPlainString());
			if (StringUtils.isEmpty(fieldOddsValueStr)){
				log.info("欧赔{}没有知道对应的马来赔率",fieldOddsValue.toPlainString());
				oddsMY = "0";
			}
			map.put("originalOddsValue", originalOddsValue.subtract(BigDecimal.ONE));
			map.put("originalMYOddsValue", oddsMY);
			map.put("fieldOddsValue", fieldOddsValueStr);
			map.put("nameExpressionValue", ObjectUtils.isEmpty(map.get("nameExpressionValue"))?"0":map.get("nameExpressionValue"));
			oddsList.add(map);
		}
		config.setOddsList(oddsList);
	}

}
