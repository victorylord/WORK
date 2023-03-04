package com.panda.sport.rcs.wrapper;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 接拒单服务
 *
 * @author carver
 */
public interface OrderAcceptRejectService {

    /**
     * 滚球接拒单，发送MQ
     *
     * @param betNos
     */
    void sendMessage(List<String> betNos);

    /**
     * 滚球接拒单，修改ext扩展表信息
     *
     * @param rejectOrders
     * @param acceptOrders
     */
    void updateOrderDetailExtStatus(Set<String> rejectOrders, Set<Long> acceptOrders);
}
