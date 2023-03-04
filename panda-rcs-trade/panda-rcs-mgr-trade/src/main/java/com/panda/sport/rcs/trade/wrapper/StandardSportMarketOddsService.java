package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 赛事盘口交易项表 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
public interface StandardSportMarketOddsService extends IService<StandardSportMarketOdds> {

    List<StandardSportMarketOdds> selectByMap(Map<String, Object> map);

    /**
     * 根据盘口ID查询投注项列表
     *
     * @param marketId
     * @return
     * @author Paca
     */
    List<StandardSportMarketOdds> list(Long marketId);

    /**
     * 根据盘口ID集合查询投注项列表并分组
     *
     * @param marketIdList
     * @return
     * @author Paca
     */
    Map<Long, List<StandardSportMarketOdds>> listAndGroup(Collection<Long> marketIdList);

}
