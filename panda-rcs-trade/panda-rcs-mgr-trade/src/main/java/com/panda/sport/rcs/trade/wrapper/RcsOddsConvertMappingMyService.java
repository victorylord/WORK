package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsOddsConvertMappingMy;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2019-12-27 17:59
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsOddsConvertMappingMyService extends IService<RcsOddsConvertMappingMy> {
    /**
     * @return java.lang.String
     * @Description //马来转欧赔需要查表
     * @Param [oddsValue]
     * @Author kimi
     * @Date 2019/12/27
     **/
    String listRcsOddsConvertMappingMy(String oddsValue);
}
