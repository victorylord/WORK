package com.panda.sport.rcs.trade.wrapper.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.mapper.sub.RcsMatchMarketConfigSubMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.trade.wrapper.config.RcsMatchMarketConfigSubService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 赛事设置子表
 * @Author : Paca
 * @Date : 2021-09-28 15:34
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsMatchMarketConfigSubServiceImpl extends ServiceImpl<RcsMatchMarketConfigSubMapper, RcsMatchMarketConfigSub> implements RcsMatchMarketConfigSubService {

    @Override
    public Map<Integer, Map<Long, RcsMatchMarketConfigSub>> getByPlayId(Long matchId, Long playId) {
        LambdaQueryWrapper<RcsMatchMarketConfigSub> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RcsMatchMarketConfigSub::getMatchId, matchId)
                .eq(RcsMatchMarketConfigSub::getPlayId, playId);
        List<RcsMatchMarketConfigSub> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        Map<Integer, Map<Long, RcsMatchMarketConfigSub>> resultMap = Maps.newHashMap();
        Map<Integer, List<RcsMatchMarketConfigSub>> groupMap = list.stream().collect(Collectors.groupingBy(RcsMatchMarketConfigSub::getMarketIndex));
        groupMap.forEach((placeNum, placeConfigList) -> {
            Map<Long, RcsMatchMarketConfigSub> placeMap = placeConfigList.stream().collect(Collectors.toMap(config -> NumberUtils.toLong(config.getSubPlayId()), Function.identity()));
            resultMap.put(placeNum, placeMap);
        });
        return resultMap;
    }

    @Override
    public Map<Long, Map<Integer, Map<Long, RcsMatchMarketConfigSub>>> getByPlayIds(Long matchId, Collection<Long> playIds) {
        LambdaQueryWrapper<RcsMatchMarketConfigSub> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RcsMatchMarketConfigSub::getMatchId, matchId)
                .in(RcsMatchMarketConfigSub::getPlayId, playIds);
        List<RcsMatchMarketConfigSub> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        Map<Long, Map<Integer, Map<Long, RcsMatchMarketConfigSub>>> resultMap = Maps.newHashMap();
        Map<Long, List<RcsMatchMarketConfigSub>> playMap = list.stream().collect(Collectors.groupingBy(RcsMatchMarketConfigSub::getPlayId));
        playMap.forEach((playId, playList) -> {
            Map<Integer, Map<Long, RcsMatchMarketConfigSub>> placeConfigMap = Maps.newHashMap();
            Map<Integer, List<RcsMatchMarketConfigSub>> placeMap = playList.stream().collect(Collectors.groupingBy(RcsMatchMarketConfigSub::getMarketIndex));
            placeMap.forEach((placeNum, placeList) -> {
                Map<Long, RcsMatchMarketConfigSub> subPlayMap = placeList.stream().collect(Collectors.toMap(config -> NumberUtils.toLong(config.getSubPlayId()), Function.identity()));
                placeConfigMap.put(placeNum, subPlayMap);
            });
            resultMap.put(playId, placeConfigMap);
        });
        return resultMap;
    }
}
