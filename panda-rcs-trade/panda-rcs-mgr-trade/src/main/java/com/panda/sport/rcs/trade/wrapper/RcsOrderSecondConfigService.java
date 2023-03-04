package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsOrderSecondConfig;
import com.panda.sport.rcs.vo.OrderSecondConfigVo;

import java.util.List;

/**
 * @author :  carver
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2020-11-17 20:19
 */
public interface RcsOrderSecondConfigService extends IService<RcsOrderSecondConfig> {

    List<OrderSecondConfigVo> queryOrderSecondConfig(OrderSecondConfigVo param);

    void saveOrderSecondConfig(OrderSecondConfigVo vo);

    List<String> selectOrderSecondTraders(OrderSecondConfigVo vo);
}
