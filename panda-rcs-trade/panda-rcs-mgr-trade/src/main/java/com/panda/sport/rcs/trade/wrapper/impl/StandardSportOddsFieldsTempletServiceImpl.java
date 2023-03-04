package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.mapper.StandardSportOddsFieldsTempletMapper;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.pojo.StandardSportOddsFieldsTemplet;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.StandardSportOddsFieldsTempletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 标准玩法投注项表 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Service
@Slf4j
public class StandardSportOddsFieldsTempletServiceImpl extends ServiceImpl<StandardSportOddsFieldsTempletMapper, StandardSportOddsFieldsTemplet> implements StandardSportOddsFieldsTempletService {


    @Autowired
    private RedisClient redisClient;


    /**
     * 缓存玩法数据，需要重新缓存的会用当前数据库数据覆盖原缓存
     *
     * @param reCached 重新缓存：true，否则false；
     */
    @Override
    public void cachedOddsFieldsTemplet(boolean reCached) {
        List<StandardSportOddsFieldsTemplet> oddsFieldsTempletList = this.list();
        if (CollectionUtils.isEmpty(oddsFieldsTempletList)) {
            log.warn("Fail to cache StandardSportMarketCategory: Can not found any data from DB, return.");
            return;
        }
        for (StandardSportOddsFieldsTemplet oddsFieldsTemplet : oddsFieldsTempletList) {
            // 已经缓存的且不需要更新缓存的跳过，其他情况缓存
            if (!reCached && redisClient.hexists(RedisKeys.RCSCACHE_MARKETODDS_ODDSFIELDS_TEMPLET, oddsFieldsTemplet.getId().toString())) {
                continue;
            }
            redisClient.hSetObj(RedisKeys.RCSCACHE_MARKETODDS_ODDSFIELDS_TEMPLET, oddsFieldsTemplet.getId().toString(), oddsFieldsTemplet);
        }
    }

    /**
     * 根据ID获取缓存中的玩法投注项模板，不存在则从数据库获取并缓存
     *
     * @param id
     * @return
     */
    @Override
    public StandardSportOddsFieldsTemplet getCachedOddsFieldsTempletById(Long id) {
        if (id == null || id == 0L) {
            return null;
        }
        Object oddsFieldsTempletObj = redisClient.hGetObj(RedisKeys.RCSCACHE_MARKETODDS_ODDSFIELDS_TEMPLET, id.toString(), StandardSportOddsFieldsTemplet.class);
        if (oddsFieldsTempletObj != null) {
            return (StandardSportOddsFieldsTemplet) oddsFieldsTempletObj;
        }
        StandardSportOddsFieldsTemplet oddsFieldsTemplet = this.getById(id);
        if (oddsFieldsTemplet != null) {
            log.info("::{}::Can not found cached standardSportOddsFieldsTemplet:{}, reload from database.", CommonUtil.getRequestId(), id);
            redisClient.hSetObj(RedisKeys.RCSCACHE_MARKETODDS_ODDSFIELDS_TEMPLET, oddsFieldsTemplet.getId().toString(), oddsFieldsTemplet);
        }
        log.warn("Can not found cached standardSportOddsFieldsTemplet:{}, database also not exists.", id);
        return oddsFieldsTemplet;
    }

}
