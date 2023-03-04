package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsCategoryOddTemplet;

import java.util.Map;

/**
 * @author Felix
 */
public interface RcsCategoryOddTempletSetService extends IService<RcsCategoryOddTemplet> {

    /**
     * 通过玩法ID获取缓存数据
     *
     * @param categoryId
     * @return
     */
    Map<String, RcsCategoryOddTemplet> getByCache(Long categoryId);

}
