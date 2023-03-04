package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.pojo.RcsBusinessConPlayConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2019-11-22 15:04
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

public interface RcsBusinessConPlayConfigService extends IService<RcsBusinessConPlayConfig> {

    List<RcsBusinessConPlayConfig> selectConPlays(Long businessId);

    boolean updateConPlayConfig(List<RcsBusinessConPlayConfig> conPlayConfigs);
}


