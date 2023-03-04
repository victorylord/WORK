package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.mapper.RcsMarketNumStatisMapper;
import com.panda.sport.rcs.pojo.RcsMarketNumStatis;
import com.panda.sport.rcs.trade.wrapper.RcsMarketNumStatisService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 盘口位置统计表
 *
 * @author Enzo
 * @since 2020-10-04
 */
@Service
public class RcsMarketNumStatisServiceImpl extends ServiceImpl<RcsMarketNumStatisMapper, RcsMarketNumStatis> implements RcsMarketNumStatisService {

    @Override
    public Map<Long, Map<String, List<RcsMarketNumStatis>>> queryPalceBetNums(List<Long> matchIds) {
        Map<Long, Map<String, List<RcsMarketNumStatis>>> matchBet = new HashMap<>();
        List<RcsMarketNumStatis> bets = baseMapper.queryBetNums(matchIds);
        if (CollectionUtils.isEmpty(bets)) return matchBet;
        Map<Long, List<RcsMarketNumStatis>> matchGroup = bets.stream().collect(Collectors.groupingBy(RcsMarketNumStatis::getMatchId));
        for (Map.Entry<Long, List<RcsMarketNumStatis>> map : matchGroup.entrySet()) {
            List<RcsMarketNumStatis> mapValue = map.getValue();
            if (!CollectionUtils.isEmpty(mapValue)) {
                Map<String, List<RcsMarketNumStatis>> collect = mapValue.stream().collect(Collectors.groupingBy(e -> e.getMarketCategoryId() + "_" + e.getPlaceNum()));
                // Map<String, RcsMarketNumStatis> oddsConfigMap = mapValue.stream().collect(Collectors.toMap(e -> e.getMarketCategoryId()+"_"+e.getPlaceNum()+"_"+e.getOddsType(), e -> e));
                matchBet.put(map.getKey(), collect);
            }
        }
        return matchBet;
    }

    @Override
    public Map<Integer, Map<Integer, Map<String, RcsMarketNumStatis>>> queryPlaceBetNums(Long matchId) {
        List<RcsMarketNumStatis> list = baseMapper.queryBetNums(Lists.newArrayList(matchId));
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        Map<Integer, Map<Integer, Map<String, RcsMarketNumStatis>>> result = Maps.newHashMapWithExpectedSize(list.size());
        Map<Integer, List<RcsMarketNumStatis>> playMap = list.stream().collect(Collectors.groupingBy(RcsMarketNumStatis::getMarketCategoryId));
        playMap.forEach((playId, placeList) -> {
            Map<Integer, Map<String, RcsMarketNumStatis>> resultPlaceMap = Maps.newHashMap();
            Map<Integer, List<RcsMarketNumStatis>> placeMap = placeList.stream().collect(Collectors.groupingBy(RcsMarketNumStatis::getPlaceNum));
            placeMap.forEach((placeNum, oddsTypeList) -> {
                Map<String, RcsMarketNumStatis> oddsTypeMap = oddsTypeList.stream().collect(Collectors.toMap(RcsMarketNumStatis::getOddsType, Function.identity()));
                resultPlaceMap.put(placeNum, oddsTypeMap);
            });
            result.put(playId, resultPlaceMap);
        });
        return result;
    }
}
