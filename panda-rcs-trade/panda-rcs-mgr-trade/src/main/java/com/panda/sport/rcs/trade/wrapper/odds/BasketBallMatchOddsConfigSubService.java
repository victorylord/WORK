package com.panda.sport.rcs.trade.wrapper.odds;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchPlayConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.trade.service.TradeOddsCommonService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BasketBallMatchOddsConfigSubService {

    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private MatchOddsConfigCommonService matchOddsConfigCommonService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;

    public void matchOddsConfig(MatchPlayConfig playConfig, StandardMatchInfo matchInfo, Integer matchType, boolean isActive, MatchOddsConfig matchConfig) {
        try {
            RcsMatchMarketConfig config = new RcsMatchMarketConfig(matchInfo.getId(), Long.parseLong(playConfig.getPlayId()));
//            List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
            List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
            matchOddsConfigCommonService.setActiveLessThreeOddsType(matchType, isActive, playAllMarketList);
            String subPlayId = getSubPlayId(playConfig);
            // 新增盘口找不到盘口数据
            List<RcsStandardMarketDTO> oddsList = getSubPlayMarket(playConfig,playAllMarketList,subPlayId);
            if (CollectionUtils.isEmpty(oddsList)){
                matchOddsConfigCommonService.initOddsList(playAllMarketList,playConfig,matchType,subPlayId);
            }
            // 计算盘口差
            if (!StringUtils.isBlank(playConfig.getMarketHeadGap())) {
                //当前盘口差  与  设置的盘口对比， 计算差值，得到新的赔率数据
                matchOddsConfigCommonService.setMarketValueAndMarketDiffValue(matchInfo, playAllMarketList, playConfig,matchConfig,subPlayId);
                log.info("::{}::计算盘口差后的赔率={}",CommonUtil.getRequestId(matchInfo.getId(),playConfig.getPlayId()),JSONObject.toJSONString(playAllMarketList));
            }
            // 设置水差和赔率
            matchOddsConfigCommonService.setMarketOdds(matchInfo.getId(), playAllMarketList, playConfig, matchType);
            // 水差计算
            matchOddsMarketOdds(matchInfo, playAllMarketList, playConfig.getPlaceConfig(),playConfig);

//            if (CollectionUtils.isNotEmpty(playConfig.getMarketList())){
            // 封盘处理
            matchOddsConfigCommonService.closeMarket(matchInfo, playConfig,subPlayId);
//            }
            List<StandardMarketDTO> marketList = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList), StandardMarketDTO.class);
            marketList.stream().collect(Collectors.groupingBy(StandardMarketDTO::getMarketCategoryId)).forEach((playId, list) -> {
                tradeStatusService.handlePushStatus(matchInfo.getSportId(), matchInfo.getId(), playId, list, null, null, 0, 0, 0);
            });
            //发送到融合
            log.info("::{}::配置变化，数据下发，：{}", CommonUtil.getRequestId(matchInfo.getId(),playConfig.getPlayId()), JSONObject.toJSONString(playAllMarketList));
            matchOddsConfigCommonService.putTradeMarketOdds(matchInfo, marketList);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
    }


    private String getSubPlayId(MatchPlayConfig playConfig) {
        String subPlayId = null;
        if (CollectionUtils.isNotEmpty(playConfig.getPlaceConfig())){
            subPlayId = playConfig.getPlaceConfig().get(0).getSubPlayId();
        }else if (!ObjectUtils.isEmpty(playConfig.getRcsTournamentTemplatePlayMargain())){
            subPlayId = playConfig.getRcsTournamentTemplatePlayMargain().getSubPlayId();
        }
        return subPlayId;
    }

    /**
     * @Description   //
     * @Param [playConfig, playAllMarketList]
     * @Author  sean
     * @Date   2021/7/29
     * @return java.util.List<com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO>
     **/
    private List<RcsStandardMarketDTO> getSubPlayMarket(MatchPlayConfig playConfig, List<RcsStandardMarketDTO> playAllMarketList,String subPlayId) {
        List<RcsStandardMarketDTO> list = Lists.newArrayList();
        for (RcsStandardMarketDTO m : playAllMarketList){
            if (StringUtils.isBlank(subPlayId) ||
                    m.getChildStandardCategoryId().toString().equalsIgnoreCase(subPlayId) && CollectionUtils.isNotEmpty(m.getMarketOddsList())){
                list.add(m);
            }
        }
        return list;
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

//            bean.setChildStandardCategoryId(Long.parseLong(placeNumConfig.getSubPlayId()));

            if (TradeConstant.FOOTBALL_X_EU_PLAYS.contains(bean.getMarketCategoryId().intValue()) ||
                TradeConstant.BASKETBALL_X_EU_PLAYS.contains(bean.getMarketCategoryId().intValue())){
                placeNumConfig.setOldMargin(matchOddsConfigCommonService.getMarginFormOddsValue(bean));
            }


            if (StringUtils.isBlank(placeNumConfig.getSubPlayId()) ||
                    placeNumConfig.getSubPlayId().equalsIgnoreCase(bean.getChildStandardCategoryId().toString())){
                //计算spread
                matchOddsConfigCommonService.matchOddsMarketSpread(matchInfo,bean, placeNumConfig);
                // : 计算位置水差，暂无，可以先空着
                matchOddsConfigCommonService.matchOddsMarketDiffOdds(bean, placeNumConfig,playConfig,matchInfo.getId());
                // : 计算最大最小值，和位置状态以及三方数据源状态
                matchOddsMarketFinalOdds(bean, placeNumConfig,matchInfo.getId());
            }

        });
    }

    private void matchOddsMarketFinalOdds(StandardMarketDTO bean, MatchMarketPlaceConfig placeNumConfig,Long matchId) {
        bean.setPlaceNumStatus(tradeStatusService.getPlaceStatusFromRedis(SportIdEnum.BASKETBALL.getId(),matchId,bean.getMarketCategoryId(),bean.getChildStandardCategoryId(),bean.getPlaceNum()));
    }

}
