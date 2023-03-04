package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.pojo.RcsPlayConfig;

import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2020-02-17 22:31
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

public interface RcsPlayConfigService extends IService<RcsPlayConfig> {
    /**
     * @return void
     * @Description //更新配置文件  是否和以前的配置的自动手动不一样   true 一样
     * @Param [rcsPlayConfig]
     * @Author kimi
     * @Date 2020/2/17
     **/
    Boolean insertOrUpdateRcsPlayConfig(RcsPlayConfig rcsPlayConfig);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsPlayConfig>
     * @Description //TODO
     * @Param [columnMap]
     * @Author kimi
     * @Date 2020/2/17
     **/

    List<RcsPlayConfig> selectRcsPlayConfigByMap(Map<String, Object> columnMap);

    /**
     * @return void
     * @Description //TODO
     * @Param [rcsPlayConfig]
     * @Author kimi
     * @Date 2020/2/17
     **/
    void insert(RcsPlayConfig rcsPlayConfig);

    /**
     * @return void
     * @Description //批量更新或者插入
     * @Param [list]
     * @Author kimi
     * @Date 2020/2/18
     **/
    void updateOrInsertRcsPlayConfigList(RcsMatchPlayConfig rcsPlayConfig, List<Long> playIdList);
}
