package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsMarketOddsConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.pojo.RcsMarketOddsConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.trade.wrapper.RcsMarketOddsConfigService;
import com.panda.sport.rcs.vo.OrderDetailStatReportVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-11-01 16:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

@Service
public class RcsMarketOddsConfigServiceImpl extends ServiceImpl<RcsMarketOddsConfigMapper, RcsMarketOddsConfig> implements RcsMarketOddsConfigService {
    @Autowired
    RcsMarketOddsConfigMapper marketOddsConfigMapper;
    @Autowired
    StandardMatchInfoMapper standardMatchInfoMapper;

    @Override
    public RcsMarketOddsConfig getMarketOdds(RcsMarketOddsConfig rcsMarketOddsConfig) {
        QueryWrapper<RcsMarketOddsConfig> queryWrapper = new QueryWrapper();
        if (rcsMarketOddsConfig.getMatchId() != null) {
            queryWrapper.lambda().eq(RcsMarketOddsConfig::getMatchId, rcsMarketOddsConfig.getMatchId());
        }
        if (rcsMarketOddsConfig.getMarketCategoryId() != null) {
            queryWrapper.lambda().eq(RcsMarketOddsConfig::getMarketCategoryId, rcsMarketOddsConfig.getMarketCategoryId());
        }
        if (rcsMarketOddsConfig.getMatchMarketId() != null) {
            queryWrapper.lambda().eq(RcsMarketOddsConfig::getMatchMarketId, rcsMarketOddsConfig.getMatchMarketId());
        }
        if (rcsMarketOddsConfig.getMarketOddsId() != null) {
            queryWrapper.lambda().eq(RcsMarketOddsConfig::getMarketOddsId, rcsMarketOddsConfig.getMarketOddsId());
        }
        if (rcsMarketOddsConfig.getStandardTournamentId() != null) {
            queryWrapper.lambda().eq(RcsMarketOddsConfig::getStandardTournamentId, rcsMarketOddsConfig.getStandardTournamentId());
        }
        return marketOddsConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public List<OrderDetailStatReportVo> queryMarketStatByMarketId(Long marketId) {

        return marketOddsConfigMapper.queryMarketStatByMarketId(marketId);
    }

    @Override
    public RcsMarketOddsConfig getMarketOdds(Long matchId, Long marketOddsId) {
        //先查赛事
        int matchType = 1;
        StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
        if (standardMatchInfo.getMatchStatus() == 1 && standardMatchInfo.getLiveOddBusiness() == 1) {
            matchType = 2;
        }
        //再查数据
        Map<String, Object> columnMap = new HashMap<>(2);
        columnMap.put("match_type", matchType);
        columnMap.put("market_odds_id", marketOddsId);
        List<RcsMarketOddsConfig> rcsMarketOddsConfigs = marketOddsConfigMapper.selectByMap(columnMap);
        if (CollectionUtils.isEmpty(rcsMarketOddsConfigs)) {
            return null;
        } else {
            return rcsMarketOddsConfigs.get(0);
        }
    }

    @Override
    public Map<Long, RcsMarketOddsConfig> queryMathBetNums(Long matchId) {
        Map<Long, RcsMarketOddsConfig> matchBet = new HashMap<>();
        List<RcsMarketOddsConfig> bets = baseMapper.queryBetNums(matchId);
        if (CollectionUtils.isEmpty(bets)) return matchBet;
        matchBet = bets.stream().collect(Collectors.toMap(e -> e.getMarketOddsId(), e -> e));
        return matchBet;
    }

    @Override
    public Map<Long, Map<String, RcsMarketOddsConfig>> queryMathBetNums(List<Long> matchIds) {
        Map<Long, Map<String, RcsMarketOddsConfig>>  matchBet= new HashMap<>();
        List<RcsMarketOddsConfig> bets = marketOddsConfigMapper.queryMathBetNums(matchIds);
        if(CollectionUtils.isEmpty(bets))return matchBet;
        Map<Long, List<RcsMarketOddsConfig>> matchGroup = bets.stream().collect(Collectors.groupingBy(RcsMarketOddsConfig::getMatchId));
        for (Map.Entry<Long, List<RcsMarketOddsConfig>> map:matchGroup.entrySet()){
            List<RcsMarketOddsConfig> mapValue = map.getValue();
            if(!CollectionUtils.isEmpty(mapValue)){
                Map<String, RcsMarketOddsConfig> collect = mapValue.stream().collect(Collectors.toMap(e -> e.getMarketCategoryId() + "_" + e.getMarketOddsId(), e -> e));
                matchBet.put(map.getKey(),collect);
            }
        }
        return matchBet;
    }
}
