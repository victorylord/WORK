package com.panda.sport.rcs.mapper.statistics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.data.rcs.dto.OrderItem;
import com.panda.sport.rcs.pojo.statistics.RcsProfitMarket;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.mapper.statistics
 * @Description :  盘口级别期望值
 * @Date: 2019-12-11 15:25
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface RcsProfitMarketMapper extends BaseMapper<RcsProfitMarket> {
    /**
     * 更新
     * @param rcsProfitMarket
     * @return
     */
    Integer update(RcsProfitMarket rcsProfitMarket);

    /**
     * 添加或者新增
     * @param rcsProfitMarket
     * @return
     */
    Integer insertOrUpdate(RcsProfitMarket rcsProfitMarket);

    /**
     * 获取market初始数据
     * @param rcsProfitMarket
     * @return
     */
    RcsProfitMarket getInitProfitMarket(@Param("profitMarket") RcsProfitMarket rcsProfitMarket,@Param("orderNo")String orderNo);

    /**
     * 查询
     * @param rcsProfitMarket
     * @return
     */
    RcsProfitMarket get(RcsProfitMarket rcsProfitMarket);
}
