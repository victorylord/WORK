package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsMarketCategorySetMarginMapper;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetMargin;
import com.panda.sport.rcs.trade.wrapper.RcsMarketCategorySetMarginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author :  Felix
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-10-04 17:05
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsMarketCategorySetMarginServiceImpl extends ServiceImpl<RcsMarketCategorySetMarginMapper, RcsMarketCategorySetMargin> implements RcsMarketCategorySetMarginService {
    @Autowired
    RcsMarketCategorySetMarginMapper rcsMarketCategorySetMarginMapper;

    /**
     * 根据玩法集ID 查询抽水
     * @param categorySetId
     * @return
     */
    @Override
    public List<RcsMarketCategorySetMargin> findMargin(List<Long> categorySetId) {
        if(CollectionUtils.isEmpty(categorySetId)){
            return null;
        }
        QueryWrapper wrapper=new QueryWrapper();
        wrapper.in("market_category_set_id",categorySetId);
        return this.list(wrapper);
    }

    @Override
    public List<RcsMarketCategorySetMargin> findMargin(Long categorySetId) {
        QueryWrapper wrapper=new QueryWrapper();
        wrapper.in("market_category_set_id",categorySetId);
        return this.list(wrapper);
    }

    /**
     * 根据MarginId修改抽水值
     * @param marginId
     * @return
     */
    @Override
    public boolean updateMargin(List<RcsMarketCategorySetMargin> marginId) {
        return this.updateBatchById(marginId);
    }

    /**
     * 批量删除margin
     * @param marginId
     * @return
     */
    @Override
    public boolean deleteMargin(List<Long> marginId) {
        return this.removeByIds(marginId);
    }

    /**
     * 批量新增抽水值
     * @param margin
     * @return
     */
    @Override
    public boolean addMargin(List<RcsMarketCategorySetMargin> margin) {
        return this.saveBatch(margin);
    }
}
