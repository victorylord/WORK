package com.panda.sport.rcs.trade.wrapper.odds;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.api.ITradeMarketOddsApi;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.StandardMatchMarketDTO;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.OddsTypeEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.enums.YesNoEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.config.MarketBuildConfig;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchPlayConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.service.TradeCommonService;
import com.panda.sport.rcs.trade.service.TradeOddsCommonService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.MarginUtils;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.MarketAdditionUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.utils.OddsConvertUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FootBallMatchOddsConfigSubService {

    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private MatchOddsConfigCommonService matchOddsConfigCommonService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;

    public void matchOddsConfig(MatchOddsConfig matchConfig,MatchPlayConfig playConfig,StandardMatchInfo matchInfo,Integer matchType,boolean isActive) {
        try {
            RcsMatchMarketConfig config = new RcsMatchMarketConfig(matchInfo.getId(), Long.parseLong(playConfig.getPlayId()));
//            List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
            List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
            matchOddsConfigCommonService.setActiveLessThreeOddsType(matchType, isActive, playAllMarketList);
            // 设置水差和赔率
            matchOddsConfigCommonService.setMarketOdds(matchInfo.getId(), playAllMarketList, playConfig, matchType);
            // 水差计算
            matchOddsMarketOdds(matchInfo, playAllMarketList, playConfig.getPlaceConfig(),playConfig);
            //更新附加字段
            matchOddsConfigCommonService.updateAdditons(matchConfig, matchInfo, playAllMarketList);

            if (CollectionUtils.isNotEmpty(playConfig.getMarketList())){
                Long childStandardCategoryId = playConfig.getMarketList().get(0).getChildStandardCategoryId();
                String subPlayId = "";
                if (ObjectUtils.isEmpty(childStandardCategoryId)){
                    subPlayId = SubPlayUtil.getRongHeSubPlayId(playConfig.getMarketList().get(0));
                }else {
                    subPlayId = childStandardCategoryId.toString();
                }
                // 封盘处理
                matchOddsConfigCommonService.closeMarket(matchInfo, playConfig,subPlayId);
            }

            List<StandardMarketDTO> marketList = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList), StandardMarketDTO.class);
            marketList.stream().collect(Collectors.groupingBy(StandardMarketDTO::getMarketCategoryId)).forEach((playId, list) -> {
                tradeStatusService.handlePushStatus(SportIdEnum.FOOTBALL.getId(), matchInfo.getId(), playId, list, null, null, 0, 0, 0);
            });
            //发送到融合
            log.info("::{}::配置变化，数据下发，：{}", CommonUtil.getRequestId(matchInfo.getId()), JSONObject.toJSONString(playAllMarketList));


            marketList.stream().collect(Collectors.groupingBy(StandardMarketDTO::getMarketCategoryId)).forEach((playId, list) -> {
                tradeStatusService.handlePushStatus(SportIdEnum.FOOTBALL.getId(), matchInfo.getId(), playId, list, null, null, 0, 0, 0);
            });
            // 修订赔率
            matchOddsConfigCommonService.caluSpecialOddsBySpread(config, marketList);

            matchOddsConfigCommonService.putTradeMarketOdds(matchInfo, marketList);
            } catch (Exception e) {
                log.error("::{}::matchOddsConfig:{}", CommonUtil.getRequestId(matchInfo.getId()), e.getMessage(), e);
            }
    }

    /**
     * 计算投注项级别的数据
     *
     * @param @param matchId
     * @param @param oddsList
     * @param @param placeConfig    设定文件
     * @return void    返回类型
     * @throws
     * @Title: matchOddsMarketOdds
     * @Description: TODO
     */
    private void matchOddsMarketOdds(StandardMatchInfo matchInfo, List<RcsStandardMarketDTO> oddsList, List<MatchMarketPlaceConfig> placeConfig,MatchPlayConfig playConfig) {
        if (CollectionUtils.isEmpty(placeConfig)) {
            log.warn("::{}::位置配置参数为空，不重新构建投注项数据:{}",matchInfo.getId(), JSONObject.toJSONString(placeConfig));
            return;
        }

        Map<Integer, MatchMarketPlaceConfig> placeConfigMap = placeConfig.stream().collect(Collectors.toMap(bean -> bean.getPlaceNum(), bean -> bean));
        oddsList.forEach(bean -> {
            if (!placeConfigMap.containsKey(bean.getPlaceNum())) {
                return;
            }

            MatchMarketPlaceConfig placeNumConfig = placeConfigMap.get(bean.getPlaceNum());

            if (TradeConstant.FOOTBALL_X_EU_PLAYS.contains(bean.getMarketCategoryId().intValue())){
                placeNumConfig.setOldMargin(matchOddsConfigCommonService.getMarginFormOddsValue(bean));
            }

            if (StringUtils.isBlank(placeNumConfig.getSubPlayId()) ||
                    placeNumConfig.getSubPlayId().equalsIgnoreCase(bean.getChildStandardCategoryId().toString())){
                //计算spread
                matchOddsConfigCommonService.matchOddsMarketSpread(matchInfo,bean, placeNumConfig);
                // : 计算最大最小值，和位置状态以及三方数据源状态
                matchOddsMarketFinalOdds(bean, placeNumConfig,matchInfo.getId());
            }
        });
    }

    private void matchOddsMarketFinalOdds(StandardMarketDTO bean, MatchMarketPlaceConfig placeNumConfig,Long matchId) {
        bean.setPlaceNumStatus(tradeStatusService.getPlaceStatusFromRedis(SportIdEnum.FOOTBALL.getId(),matchId,bean.getMarketCategoryId(),bean.getChildStandardCategoryId(),bean.getPlaceNum()));
    }

}
