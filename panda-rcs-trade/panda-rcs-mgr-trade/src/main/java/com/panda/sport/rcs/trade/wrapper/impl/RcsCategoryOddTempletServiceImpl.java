package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.RcsCategoryOddTempletMapper;
import com.panda.sport.rcs.pojo.RcsCategoryOddTemplet;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.RcsLocalCacheUtils;
import com.panda.sport.rcs.trade.wrapper.RcsCategoryOddTempletSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 玩法模板 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Service
@Slf4j
public class RcsCategoryOddTempletServiceImpl extends ServiceImpl<RcsCategoryOddTempletMapper, RcsCategoryOddTemplet> implements RcsCategoryOddTempletSetService {

    @Autowired
    private RedisUtils redisUtils;

    @PostConstruct
    public void initCache() {
        log.info("::{}::开始缓存玩法投注项模板……",CommonUtil.getRequestId());
        List<RcsCategoryOddTemplet> list = this.list();
        if (CollectionUtils.isEmpty(list)) {
            log.warn("::{}::未查询到玩法投注项模板",CommonUtil.getRequestId());
            return;
        }
        Map<Integer, List<RcsCategoryOddTemplet>> groupMap = list.stream().collect(Collectors.groupingBy(RcsCategoryOddTemplet::getCategory));
        for (Map.Entry<Integer, List<RcsCategoryOddTemplet>> entry : groupMap.entrySet()) {
            String key = RedisKey.RCS_CATEGORY_ODDS_TEMPLATE + entry.getKey();
            Map<String, String> hash = entry.getValue().stream().collect(Collectors.toMap(RcsCategoryOddTemplet::getOddType, JsonFormatUtils::toJson));
            redisUtils.hmset(key, hash);
            redisUtils.expire(key, 7L, TimeUnit.DAYS);
        }
        redisUtils.setex(RedisKey.RCS_CATEGORY_ODDS_TEMPLATE, list, 7L, TimeUnit.DAYS);
        log.info("::{}::玩法投注项模板缓存完成", CommonUtil.getRequestId());
    }

    @Override
    public Map<String, RcsCategoryOddTemplet> getByCache(Long categoryId) {
        String key = RedisKey.RCS_CATEGORY_ODDS_TEMPLATE + categoryId;
        Map<String, String> map = RcsLocalCacheUtils.getValue(key, redisUtils::hgetAll, 10 * 60 * 1000L); //redisUtils.hgetAll(key);
        if (CollectionUtils.isEmpty(map)) {
            List<RcsCategoryOddTemplet> list = this.list(new LambdaQueryWrapper<RcsCategoryOddTemplet>()
                    .eq(RcsCategoryOddTemplet::getCategory, categoryId.intValue()));
            if (CollectionUtils.isEmpty(list)) {
                return Maps.newHashMap();
            }
            map = list.stream().collect(Collectors.toMap(RcsCategoryOddTemplet::getOddType, JsonFormatUtils::toJson));
            redisUtils.hmset(key, map);
        }
        return map.values().stream().map(s -> JsonFormatUtils.fromJson(s, RcsCategoryOddTemplet.class)).collect(Collectors.toMap(RcsCategoryOddTemplet::getOddType, Function.identity()));
    }

}
