package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.mapper.statistics.RcsPredictBetOddsMapper;
import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;
import com.panda.sport.rcs.trade.wrapper.RcsPredictBetOddsService;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 货量
 *
 * @author Enzo
 * @since 2021-02-26
 */
@Service
public class RcsPredictBetOddsServiceImpl extends ServiceImpl<RcsPredictBetOddsMapper, RcsPredictBetOdds> implements RcsPredictBetOddsService {


    @Override
    public Map<Long, Map<String, List<RcsPredictBetOdds>>> queryBetOdds(List<Long> matchIds,Integer dataType, Integer seriesType) {
        Map<Long, Map<String, List<RcsPredictBetOdds>>> matchBet = new HashMap<>();
        List<RcsPredictBetOdds> betOdds = baseMapper.queryBetOdds(matchIds, dataType, seriesType);
        if (CollectionUtils.isEmpty(betOdds)) return matchBet;
        Map<Long, List<RcsPredictBetOdds>> matchGroup = betOdds.stream().filter(fi->fi.getSubPlayId()!=null).collect(Collectors.groupingBy(RcsPredictBetOdds::getMatchId));
        for (Map.Entry<Long, List<RcsPredictBetOdds>> map : matchGroup.entrySet()) {
            List<RcsPredictBetOdds> mapValue = map.getValue();
            if (!CollectionUtils.isEmpty(mapValue)) {
                Map<String, List<RcsPredictBetOdds>> collect = mapValue.stream().collect(Collectors.groupingBy(e -> e.getPlayId()+ "_" +e.getSubPlayId() + "_" + e.getDataTypeValue()));
                matchBet.put(map.getKey(), collect);
            }
        }
        return matchBet;
    }
    @Override
    public Map<String, List<RcsPredictBetOdds>> queryPlaceBetNums(Long matchId, Integer seriesType) {
        List<RcsPredictBetOdds> betOdds = baseMapper.queryBetOdds(Lists.newArrayList(matchId), 2, seriesType);
        if (CollectionUtils.isEmpty(betOdds)) {
            return Maps.newHashMap();
        }
        return betOdds.stream().collect(Collectors.groupingBy(RcsPredictBetOdds::groupField));
    }
}
