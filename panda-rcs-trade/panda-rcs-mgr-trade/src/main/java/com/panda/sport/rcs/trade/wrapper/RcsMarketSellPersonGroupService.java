package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.bean.RcsMarketSellPersonGroup;
import com.panda.sport.rcs.vo.StandardMarketSellQueryV2Vo;

public interface RcsMarketSellPersonGroupService extends IService<RcsMarketSellPersonGroup> {

    boolean saveSpecialGroupPerson(StandardMarketSellQueryV2Vo standardMarketSellQueryVo);
}
