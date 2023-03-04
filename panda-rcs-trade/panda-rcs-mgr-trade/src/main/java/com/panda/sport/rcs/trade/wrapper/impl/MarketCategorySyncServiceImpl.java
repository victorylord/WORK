package com.panda.sport.rcs.trade.wrapper.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.StandardSportMarketCategoryCacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.sport.rcs.constants.RcsErrorInfoConstants;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.StandardSportMarketCategoryMapper;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.pojo.StandardSportOddsFieldsTemplet;
import com.panda.sport.rcs.trade.wrapper.DataSyncService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketCategoryService;
import com.panda.sport.rcs.trade.wrapper.StandardSportOddsFieldsTempletService;

import lombok.extern.slf4j.Slf4j;

/**
 * 玩法、投注项数据同步服务类
 */
@Service("marketCategorySyncService")
@Slf4j
public class MarketCategorySyncServiceImpl implements DataSyncService<Long> {

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private StandardSportMarketCategoryService marketCategoryService;
    @Autowired
    private StandardSportOddsFieldsTempletService oddsFieldsTempletService;
    
    @Autowired
    private StandardSportMarketCategoryMapper standardSportMarketCategoryMapper;


    @Override
    public Map<String, String> receive(Long data) {
        if (data == null) {
            throw new RcsServiceException(RcsErrorInfoConstants.PARAM_VALIDATE_EXCEPTION, "玩法ID为空");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", data);
        map.put("sportId", "1");//默认为1
        StandardSportMarketCategory marketCategory = standardSportMarketCategoryMapper.queryCategoryInfoByMap(map);
        String key = String.format("%s_%s", 1,data);
        // 数据库中不存在直接返回
        StandardSportMarketCategoryCacheUtils.timedCache.remove(RedisKeys.RCSCACHE_MARKETODDS_MARKET_CATEGORY+ key);
        if (marketCategory == null) {
            log.info("::{}::Can not found marketCategory:{} from database, not required trigger cache refresh.", CommonUtil.getRequestId());
            return Collections.emptyMap();
        }
        // 删除已有缓存，待下次重新获取
        log.info("::{}::Remove category cache:{}",CommonUtil.getRequestId(),marketCategory.getId());
        redisClient.delete(RedisKeys.RCSCACHE_MARKETODDS_MARKET_CATEGORY+ key);
        // 删除对应的国际化语言
        log.info("::{}::Remove language code:{}  cascade market category cache.",CommonUtil.getRequestId(), marketCategory.getNameCode());
        redisClient.hashRemove(RedisKeys.RCSCACHE_BASEDATA_LANGUAGEINTERNATION, key);
        QueryWrapper<StandardSportOddsFieldsTemplet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("market_category_id", data);
        List<StandardSportOddsFieldsTemplet> oddsFieldsTemplist = oddsFieldsTempletService.list(queryWrapper);
        if (!CollectionUtils.isEmpty(oddsFieldsTemplist)) {
            for (StandardSportOddsFieldsTemplet oddsFieldsTemplet : oddsFieldsTemplist) {
                //log.info("::{}::Remove oddsFieldsTemplet cache:{}",CommonUtil.getRequestId(), oddsFieldsTemplet.getId());
                redisClient.hashRemove(RedisKeys.RCSCACHE_MARKETODDS_ODDSFIELDS_TEMPLET, oddsFieldsTemplet.getId().toString());
                // 删除对应的国际化语言
                //log.info("::{}::Remove language code:{}  cascade template cache.",CommonUtil.getRequestId(), oddsFieldsTemplet.getNameCode());
                redisClient.hashRemove(RedisKeys.RCSCACHE_BASEDATA_LANGUAGEINTERNATION, oddsFieldsTemplet.getNameCode().toString());
            }
        }
        return Collections.emptyMap();
    }
}
