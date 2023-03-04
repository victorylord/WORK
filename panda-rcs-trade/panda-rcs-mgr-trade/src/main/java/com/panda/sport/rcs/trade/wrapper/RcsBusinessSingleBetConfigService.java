package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsBusinessSingleBetConfig;

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

public interface RcsBusinessSingleBetConfigService extends IService<RcsBusinessSingleBetConfig> {

    List<RcsBusinessSingleBetConfig> selectBusinessSingleBetConfigList(RcsBusinessSingleBetConfig businessSingleBetConfig);

    List<RcsBusinessSingleBetConfig> getCustomizedConfigList(int playId, int tournamentId);

    void initBusinessSingleBetConfig(RcsBusinessSingleBetConfig businessSingleBetConfig);
}
