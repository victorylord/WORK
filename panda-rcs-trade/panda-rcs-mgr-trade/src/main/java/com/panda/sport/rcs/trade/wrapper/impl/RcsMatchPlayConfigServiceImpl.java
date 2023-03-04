package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.mapper.statistics.RcsMatchPlayConfigMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.trade.wrapper.RcsMatchPlayConfigService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName MatchPeriodServiceImpl
 * @Description: TODO 
 * @Author Vector
 * @Date 2019/11/19 
**/
@Service
public class RcsMatchPlayConfigServiceImpl extends ServiceImpl<RcsMatchPlayConfigMapper, RcsMatchPlayConfig> implements RcsMatchPlayConfigService {

    @Resource
    private RcsMatchPlayConfigMapper rcsMatchPlayConfigMapper;

    @Override
    public RcsMatchPlayConfig selectRcsMatchPlayConfig(RcsMatchMarketConfig config) {
        QueryWrapper<RcsMatchPlayConfig> wrapper = new QueryWrapper();
        wrapper.lambda().eq(RcsMatchPlayConfig::getMatchId,config.getMatchId());
        wrapper.lambda().eq(RcsMatchPlayConfig::getPlayId,config.getPlayId());
        return rcsMatchPlayConfigMapper.selectOne(wrapper);
    }

    @Override
    public void insertOrUpdateMarketHeadGap(RcsMatchMarketConfig config) {
        rcsMatchPlayConfigMapper.insertOrUpdateMarketHeadGap(config);
    }

    /**
     * @Description   //获取玩法水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return java.lang.String
     **/
    @Override
    public String getPlayWaterDiff(RcsMatchMarketConfig config) {
        String awayAutoChangeRate = NumberUtils.DOUBLE_ZERO.toString();
        QueryWrapper<RcsMatchPlayConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsMatchPlayConfig :: getMatchId,config.getMatchId());
        queryWrapper.lambda().eq(RcsMatchPlayConfig ::getPlayId,config.getPlayId());
        RcsMatchPlayConfig playConfig = rcsMatchPlayConfigMapper.selectOne(queryWrapper);
        if (!ObjectUtils.isEmpty(playConfig) &&
                !ObjectUtils.isEmpty(playConfig.getAwayAutoChangeRate())){
            awayAutoChangeRate = playConfig.getAwayAutoChangeRate().toString();
        }
        return awayAutoChangeRate;
    }

    @Override
    public void clearMarketHeadGap(Long matchId, Collection<Long> playIds) {
        List<Integer> playIdList = playIds.stream().map(Long::intValue).collect(Collectors.toList());
        LambdaUpdateWrapper<RcsMatchPlayConfig> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(RcsMatchPlayConfig::getMatchId, matchId)
                .in(RcsMatchPlayConfig::getPlayId, playIdList);
        RcsMatchPlayConfig entity = new RcsMatchPlayConfig();
        entity.setMarketHeadGap(BigDecimal.ZERO);
        this.baseMapper.update(entity, wrapper);
    }

    @Override
    public Map<String, Integer> queryRelevanceType(List<Long> matchIds) {
        Map<String, Integer> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(matchIds)) {
            QueryWrapper<RcsMatchPlayConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(RcsMatchPlayConfig::getMatchId, matchIds);
            List<RcsMatchPlayConfig> playConfigs = rcsMatchPlayConfigMapper.selectList(queryWrapper);
            if (!CollectionUtils.isEmpty(playConfigs)) {
                map = playConfigs.stream().collect(Collectors.toMap(e -> e.getMatchId() + "_" + e.getPlayId() + "_" + e.getSubPlayId(), e -> e.getRelevanceType(), (oldValue, newValue) -> oldValue));
            }
        }
        return map;
    }

    @Override
    public Map<Long, RcsMatchPlayConfig> getByPlayId(Long matchId, Long playId) {
        LambdaQueryWrapper<RcsMatchPlayConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RcsMatchPlayConfig::getMatchId, matchId)
                .eq(RcsMatchPlayConfig::getPlayId, playId.intValue());
        List<RcsMatchPlayConfig> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(RcsMatchPlayConfig::subPlayId, Function.identity()));
    }

    @Override
    public Map<Long, Map<Long, RcsMatchPlayConfig>> getByPlayIds(Long matchId, Collection<Long> playIds) {
        List<Integer> playIdList = playIds.stream().map(Long::intValue).collect(Collectors.toList());
        LambdaQueryWrapper<RcsMatchPlayConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RcsMatchPlayConfig::getMatchId, matchId)
                .in(RcsMatchPlayConfig::getPlayId, playIdList);
        List<RcsMatchPlayConfig> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        Map<Long, Map<Long, RcsMatchPlayConfig>> resultMap = Maps.newHashMap();
        Map<Integer, List<RcsMatchPlayConfig>> groupMap = list.stream().collect(Collectors.groupingBy(RcsMatchPlayConfig::getPlayId));
        groupMap.forEach((playId, subPlayList) -> {
            Map<Long, RcsMatchPlayConfig> map = subPlayList.stream().collect(Collectors.toMap(RcsMatchPlayConfig::subPlayId, Function.identity()));
            resultMap.put(playId.longValue(), map);
        });
        return resultMap;
    }
}

