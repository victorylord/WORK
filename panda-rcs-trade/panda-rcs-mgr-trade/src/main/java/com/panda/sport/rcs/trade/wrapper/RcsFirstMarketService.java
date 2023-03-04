package com.panda.sport.rcs.trade.wrapper;


import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsFirstMarket;
import com.panda.sport.rcs.pojo.StandardMatchInfo;

import java.util.List;
import java.util.Map;

public interface RcsFirstMarketService extends IService<RcsFirstMarket> {


    /**
     * 得到赛前终盘盘口值
     * @param dto
     * @return
     */
    Map getPreEndMarketValue(StandardMatchInfo dto);


    /**
     * 插入终盘
     * @param list
     * @return
     */
    int batchInsertOrUpdateEndMarket(List<RcsFirstMarket> list);

}


