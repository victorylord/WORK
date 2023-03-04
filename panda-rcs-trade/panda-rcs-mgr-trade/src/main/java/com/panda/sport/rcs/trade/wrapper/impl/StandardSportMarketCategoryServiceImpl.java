package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beust.jcommander.internal.Maps;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.StandardSportMarketCategoryMapper;
import com.panda.sport.rcs.pojo.RcsCode;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.StandardSportMarketCategoryCacheUtils;
import com.panda.sport.rcs.trade.wrapper.RcsCodeService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketCategoryService;
import com.panda.sport.rcs.vo.CategoryTemplateVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.panda.sport.rcs.constants.RedisKeys.CATEGORY_PHASE_CACHE;
import static com.panda.sport.rcs.constants.RedisKeys.EXP_TIME;

/**
 * <p>
 * 标准玩法表 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Service
@Slf4j
public class StandardSportMarketCategoryServiceImpl extends ServiceImpl<StandardSportMarketCategoryMapper, StandardSportMarketCategory> implements StandardSportMarketCategoryService {

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RcsCodeService rcsCodeService;

    /**
     * 根据ID获取缓存中的玩法，不存在则从数据库获取并缓存
     *
     * @param id
     * @return
     */
    @Override
    public StandardSportMarketCategory getCachedMarketCategoryById(Long id) {
        return getCachedMarketCategoryById(1, id);
    }

    @Override
    public StandardSportMarketCategory getCachedMarketCategoryById(Integer sportId, Long id) {
        if (id == null || id == 0L) {
            return null;
        }

        String key = String.format("%s_%s", sportId, id);
        StandardSportMarketCategory standardSportMarketCategory = StandardSportMarketCategoryCacheUtils.timedCache.get(RedisKeys.RCSCACHE_MARKETODDS_MARKET_CATEGORY + key,false);
        if(standardSportMarketCategory != null){
            return standardSportMarketCategory;
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sportId", sportId);
        map.put("id", id);
        StandardSportMarketCategory marketCategory = this.baseMapper.queryCategoryInfoByMap(map);
        if (marketCategory != null) {
            log.info("::{}::Can not found cached category:{}, reload from database.", CommonUtil.getRequestId(), id);
            StandardSportMarketCategoryCacheUtils.timedCache.put(RedisKeys.RCSCACHE_MARKETODDS_MARKET_CATEGORY + key,marketCategory);
        }
        log.warn("::{}::Can not found cached category:{}, database also not exists.",CommonUtil.getRequestId(), id);
        return marketCategory;
    }

    @Override
    public Integer selectPlayPhase(Long categoryId) {
        String result = "";
        if (StringUtils.isNotBlank(redisClient.get(CATEGORY_PHASE_CACHE))) {
            List<RcsCode> rcsCodes = JsonFormatUtils.fromJsonArray(redisClient.get(CATEGORY_PHASE_CACHE), RcsCode.class);
            for (RcsCode rcsCode : rcsCodes) {
                if (String.valueOf(categoryId).equals(rcsCode.getChildKey())) {
                    result = rcsCode.getRemark();
                }
            }
        } else {
            List<RcsCode> ballPhase = rcsCodeService.selectRcsCods("play_phase");
            if (ballPhase.size() > 0) {
                redisClient.setExpiry(CATEGORY_PHASE_CACHE, JsonFormatUtils.toJson(ballPhase), EXP_TIME);
            }
        }
        if (StringUtils.isNotBlank(result)) {
            return Integer.parseInt(result);
        }
        return 0;
    }

    @Override
    public List<Integer> selectCategoryIds(List<Integer> playPhases) {
        List<Integer> categoryIds = new ArrayList<>();
        List<RcsCode> collect = new ArrayList<>();
        if (StringUtils.isNotBlank(redisClient.get(CATEGORY_PHASE_CACHE))) {
            List<RcsCode> rcsCodes = JsonFormatUtils.fromJsonArray(redisClient.get(CATEGORY_PHASE_CACHE), RcsCode.class);
            if (null != playPhases && playPhases.size() > 0) {
                collect = rcsCodes.stream().filter(line -> playPhases.contains(Integer.parseInt(line.getRemark()))).collect(Collectors.toList());
            } else {
                collect = rcsCodes;
            }
        } else {
            QueryWrapper<RcsCode> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(RcsCode::getFatherKey, "play_phase");
            if (null != playPhases && playPhases.size() > 0) {
                wrapper.lambda().in(RcsCode::getRemark, playPhases);
            }
            collect = rcsCodeService.list(wrapper);
        }
        if (collect.size() > 0) {
            collect.stream().forEach(model -> {
                categoryIds.add(Integer.parseInt(model.getChildKey()));
            });
        }
        return categoryIds;
    }

    @Override
    public CategoryTemplateVo queryCategoryTemplate(final Long sportId, final Long categoryId) {
        CategoryTemplateVo templateVo = new CategoryTemplateVo();
        try {
            List<CategoryTemplateVo> templateList = this.baseMapper.queryCategoryTemplate(sportId);
            if (!CollectionUtils.isEmpty(templateList)) {
                return templateList.stream().filter(vo -> categoryId.equals(vo.getCategoryId())).findFirst().orElse(templateVo);
            }
        } catch (Exception e) {
            log.error("获取玩法模板异常:", e);
        }
        return null;
    }

    @Override
    public Map<Long, CategoryTemplateVo> getCategoryTemplateByCache(Long sportId, List<Long> categoryIds) {
        List<CategoryTemplateVo> templateList = this.baseMapper.queryCategoryTemplate(sportId);
        if (CollectionUtils.isEmpty(templateList)) {
            return Maps.newHashMap();
        }
        return templateList.stream().filter(vo -> categoryIds.contains(vo.getCategoryId())).collect(Collectors.toMap(CategoryTemplateVo::getCategoryId, Function.identity()));
    }

    @Override
    public Map<Long, Integer> getAllCategoryTemplateId(Long sportId) {
        List<CategoryTemplateVo> templateList = this.baseMapper.queryCategoryTemplate(sportId);
        if (CollectionUtils.isEmpty(templateList)) {
            return Maps.newHashMap();
        }
        return templateList.stream().collect(Collectors.toMap(CategoryTemplateVo::getCategoryId, CategoryTemplateVo::getTemplateId));
    }

}
