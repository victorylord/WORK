package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.mapper.StandardSportMarketOddsMapper;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketOddsService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 赛事盘口交易项表 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Service
public class StandardSportMarketOddsServiceImpl extends ServiceImpl<StandardSportMarketOddsMapper, StandardSportMarketOdds> implements StandardSportMarketOddsService {

    @Override
    public List<StandardSportMarketOdds> selectByMap(Map<String, Object> map) {
        return this.baseMapper.selectByMap(map);
    }

    @Override
    public List<StandardSportMarketOdds> list(Long marketId) {
        return this.list(new LambdaQueryWrapper<StandardSportMarketOdds>()
                .eq(StandardSportMarketOdds::getMarketId, marketId));
    }

    @Override
    public Map<Long, List<StandardSportMarketOdds>> listAndGroup(Collection<Long> marketIdList) {
        if (CollectionUtils.isEmpty(marketIdList)) {
            return Maps.newHashMap();
        }
        LambdaQueryWrapper<StandardSportMarketOdds> wrapper = Wrappers.lambdaQuery();
        wrapper.in(StandardSportMarketOdds::getMarketId, marketIdList);
        List<StandardSportMarketOdds> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.groupingBy(StandardSportMarketOdds::getMarketId));
    }
}
