package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.TOrderDetailExt;
import com.panda.sport.rcs.pojo.vo.OrderTakingVo;
import com.panda.sport.rcs.vo.HttpResponse;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.task.wrapper
 * @Description :  订单明细扩展
 * @Date: 2020-01-31 11:46
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface TOrderDetailExtService extends IService<TOrderDetailExt> {
    /**
     * @Description   批量处理订单状态
     * @Param [orderStatus, ids]
     * @Author  toney
     * @Date  10:15 2020/2/1
     * @return int
     **/
    HttpResponse orderTakingBatch(OrderTakingVo vo);
}
