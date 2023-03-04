package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.pojo.vo.OrderTakingVo;
import com.panda.sport.rcs.vo.OrderSecondConfigVo;

/**
 * @ClassName
 * @Description: TODO
 * @Author Enzo
 * @Date 2020/7/16
 **/
public interface LogFormatService {
    /**
     * 暂停接拒单日志
     * @param vo
     */
    void pauseMatchOrderLog(OrderSecondConfigVo vo);



    void saveOrderSecondConfigLog(OrderSecondConfigVo vo);


    void orderTakingBatchLog(OrderTakingVo vo);
}
