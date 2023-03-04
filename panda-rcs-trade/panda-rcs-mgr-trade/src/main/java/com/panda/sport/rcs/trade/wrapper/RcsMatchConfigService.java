package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchConfig;

import java.util.List;
import java.util.Map;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-11-08 20:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

public interface RcsMatchConfigService extends IService<RcsMatchConfig> {

    RcsMatchConfig selectMatchConfig(Long matchId);

    List<RcsMatchConfig> selectMatchConfigByMatchIds(List<Long> ids);

    Integer getTradeType(Long matchId);

    void insert(RcsMatchConfig rcsMatchConfig);

    void updateRcsMatchConfig(RcsMatchConfig rcsMatchConfig);


    void updateRiskManagerCode(Map<String,Object> map);
}
