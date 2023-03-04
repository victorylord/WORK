package com.panda.sport.rcs.repository;


import com.panda.sport.rcs.pojo.vo.OrderTakingVo;
import com.panda.sport.rcs.vo.TOrderDetailExtDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

public interface TOrderDetailExtRepository {
    

    /**
     * @Description   查询订单号根据主单号
     * @Param [berNos]
     * @Author  Sean
     * @Date  16:25 2020/2/21
     **/
    List<String> queryOrderNoByBetNo(List<String> betNos);

    /**
     * @Description   批量处理订单状态
     * @Param [orderStatus, ids]
     * @Author  toney
     * @Date  10:17 2020/2/1
     * @return int
     **/
    int orderTakingBatch(Integer orderStatus, List<String> ids);
    /**
     * 查询订单号
     * @param vo
     * @return
     */
    List<String> queryOrderNo(OrderTakingVo vo);

    int pauseOrderTakingBatch(OrderTakingVo vo, Integer updateStatus);

    List<TOrderDetailExtDO> searchByCriteria(Criteria criteria);
}
