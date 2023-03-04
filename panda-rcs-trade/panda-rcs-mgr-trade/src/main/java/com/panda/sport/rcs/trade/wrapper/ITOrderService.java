package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.TOrder;
import com.panda.sport.rcs.pojo.TOrderForChampion;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-03
 */
public interface ITOrderService extends IService<TOrder> {
    /**
     * @Description   通过订单明细扩展表
     * @Param [orderStatus,ids]
     * @Author  toney
     * @Date  15:56 2020/1/31
     * @return java.util.List<com.panda.sport.rcs.pojo.TOrder>
     **/
    List<TOrderForChampion> queryByOrderDetailExtAndIds(String[] ids);

    /**
     * @Author: Kir
     * @deprecated 根据订单ID查询赛事ID
     * @param orderNo
     * @Date 2021/1/5
     * @return
     */
    Long selectMatchIdByOrderNo(String orderNo);
}
