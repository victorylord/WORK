package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.cache.RcsCacheContant;
import com.panda.sport.rcs.pojo.dto.StandardMarketPlaceDto;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 盘口服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Slf4j
@Service
public class StandardSportMarketServiceImpl extends ServiceImpl<StandardSportMarketMapper, StandardSportMarket> implements StandardSportMarketService {

    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;

    @Override
    public Map<Long, List<StandardSportMarket>> getEffectiveMarket(Long matchId, List<Long> playIds) {
        LambdaQueryWrapper<StandardSportMarket> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StandardSportMarket::getStandardMatchInfoId, matchId)
                .in(StandardSportMarket::getMarketCategoryId, playIds)
                .in(StandardSportMarket::getThirdMarketSourceStatus, Lists.newArrayList(0, 1));
        List<StandardSportMarket> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        Map<Long, List<StandardSportMarket>> map = list.stream().collect(Collectors.groupingBy(StandardSportMarket::getChildMarketCategoryId));
        map.values().forEach(values -> {
            for (int i = 0; i < values.size(); i++) {
                values.get(i).setPlaceNum(i + 1);
            }
        });
        return map;
    }

    @Override
    public Map<Integer, Long> getOddsFieldsTemplateId(Long playId) {
        return RcsCacheContant.ODDS_FIELDS_TEMPLATE_ID_CACHE.get(playId, id -> {
            List<StandardMarketOddsDTO> list = this.baseMapper.selectOddsFieldsTempletId(playId);
            if (CollectionUtils.isEmpty(list)) {
                return Maps.newHashMap();
            }
            return list.stream().collect(Collectors.toMap(StandardMarketOddsDTO::getOrderOdds, StandardMarketOddsDTO::getOddsFieldsTemplateId));
        });
    }

    @Override
    public StandardSportMarket getStandardSportMarketById(long id) {
        return standardSportMarketMapper.selectById(id);
    }

    @Override
    public List<StandardSportMarket> getStandardSportMarketByMatchId(Long matchId) {
        if (matchId == null) {
            return null;
        }
        QueryWrapper<StandardSportMarket> standardSportMarketQueryWrapper = new QueryWrapper<>();
        standardSportMarketQueryWrapper.lambda().eq(StandardSportMarket::getStandardMatchInfoId, matchId);
        return standardSportMarketMapper.selectList(standardSportMarketQueryWrapper);
    }

    @Override
    public Long selectStandardSportMarketIdByMarketValue(Long matchId, Long playId, String marketValue) {
        return standardSportMarketMapper.selectStandardSportMarketIdByMarketValue(matchId, playId, marketValue);
    }

    @Override
    public List<StandardSportMarket> selectStandardSportMarketByMap(Map<String, Object> columnMap) {
        return standardSportMarketMapper.selectByMap(columnMap);
    }

    @Override
    public List<Long> selectPlayIdByMatchId(Long matchId) {
        return standardSportMarketMapper.selectPlayIdByMatchId(matchId);
    }

    @Override
    public List<StandardSportMarketOdds> selectStandardSportMarketByGiveWay(Long matchId, Long playId) {
        return standardSportMarketMapper.selectStandardSportMarketByGiveWay(matchId, playId);
    }

    @Override
    public StandardSportMarket selectById(Long id) {
        return standardSportMarketMapper.selectById(id);
    }

    @Override
    public List<StandardSportMarket> list(Long matchId) {
        return this.list(new LambdaQueryWrapper<StandardSportMarket>()
                .eq(StandardSportMarket::getStandardMatchInfoId, matchId));
    }

    @Override
    public List<StandardSportMarket> list(Long matchId, Long categoryId) {
        return this.list(new LambdaQueryWrapper<StandardSportMarket>()
                .eq(StandardSportMarket::getStandardMatchInfoId, matchId)
                .eq(StandardSportMarket::getMarketCategoryId, categoryId));
    }

    @Override
    public List<StandardSportMarket> list(Long matchId, Collection<Long> categoryIds) {
        return this.list(new LambdaQueryWrapper<StandardSportMarket>()
                .eq(StandardSportMarket::getStandardMatchInfoId, matchId)
                .in(StandardSportMarket::getMarketCategoryId, categoryIds));
    }

    @Override
    public StandardSportMarket get(Long matchId, Long marketId) {
        return this.getOne(new LambdaQueryWrapper<StandardSportMarket>()
                .eq(StandardSportMarket::getId, marketId)
                .eq(StandardSportMarket::getStandardMatchInfoId, matchId));
    }

    @Override
    public List<StandardSportMarket> queryMarketInfo(Long matchId, Long playId) {
        return this.baseMapper.queryMarketInfo(matchId, Lists.newArrayList(playId));
    }

    @Override
    public List<StandardSportMarket> queryMarketInfo(Long matchId, Collection<Long> playIds) {
        return this.baseMapper.queryMarketInfo(matchId, playIds);
    }

    @Override
    public StandardSportMarket queryMainMarketInfo(Long matchId, Long playId) {
        return this.baseMapper.queryMainMarketInfo(matchId, playId);
    }

    @Override
    public Map<Long, Map<Long, StandardSportMarket>> listMainMarketInfo(Long matchId, Collection<Long> playIds) {
        List<StandardSportMarket> list = this.baseMapper.listMainMarketInfo(matchId, playIds);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        Map<Long, Map<Long, StandardSportMarket>> resultMap = Maps.newHashMap();
        Map<Long, List<StandardSportMarket>> groupMap = list.stream().collect(Collectors.groupingBy(StandardSportMarket::getMarketCategoryId));
        groupMap.forEach((playId, subPlayList) -> {
            Map<Long, StandardSportMarket> map = subPlayList.stream().collect(Collectors.toMap(StandardSportMarket::getChildMarketCategoryId, Function.identity()));
            resultMap.put(playId, map);
        });
        return resultMap;
    }

    @Override
    public StandardSportMarket selectMainMarketInfo(Long matchId, Long categoryId,String subPlayId) {
        return this.baseMapper.selectMainMarketInfo(matchId, categoryId,subPlayId);
    }

    @Override
    public StandardMarketPlaceDto getMainMarketPlaceInfo(Long matchId, Long playId) {
        List<StandardMarketPlaceDto> list = this.baseMapper.selectMarketPlaceInfo(matchId, Lists.newArrayList(playId), 1);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<StandardSportMarket> list(Long matchId, Long playId, Integer marketType) {
        LambdaQueryWrapper<StandardSportMarket> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StandardSportMarket::getStandardMatchInfoId, matchId)
                .eq(StandardSportMarket::getMarketCategoryId, playId)
                .eq(StandardSportMarket::getMarketType, marketType);
        return this.list(wrapper);
    }

    @Override
    public boolean updatePaStatus(Long marketId, Integer paStatus) {
        LambdaUpdateWrapper<StandardSportMarket> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(StandardSportMarket::getId, marketId)
                .set(StandardSportMarket::getPaStatus, paStatus);
        return this.update(wrapper);
    }

    @Override
    public List<Long> getMarketIdList(Long matchId, Long playId) {
        LambdaQueryWrapper<StandardSportMarket> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StandardSportMarket::getStandardMatchInfoId, matchId)
                .eq(StandardSportMarket::getMarketCategoryId, playId)
                .select(StandardSportMarket::getId);
        List<StandardSportMarket> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(StandardSportMarket::getId).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<Long>> getSubPlayId(Long matchId, Collection<Long> playIds) {
        LambdaQueryWrapper<StandardSportMarket> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StandardSportMarket::getStandardMatchInfoId, matchId)
                .in(StandardSportMarket::getMarketCategoryId, playIds)
                .select(StandardSportMarket::getMarketCategoryId, StandardSportMarket::getChildMarketCategoryId);
        List<StandardSportMarket> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        Map<Long, List<Long>> resultMap = Maps.newHashMap();
        list.forEach(market -> {
            Long playId = market.getMarketCategoryId();
            Long subPlayId = market.getChildMarketCategoryId();
            if (resultMap.containsKey(playId)) {
                resultMap.get(playId).add(subPlayId);
            } else {
                List<Long> subPlayIdList = Lists.newArrayList();
                subPlayIdList.add(subPlayId);
                resultMap.put(playId, subPlayIdList);
            }
        });
        return resultMap;
    }
}