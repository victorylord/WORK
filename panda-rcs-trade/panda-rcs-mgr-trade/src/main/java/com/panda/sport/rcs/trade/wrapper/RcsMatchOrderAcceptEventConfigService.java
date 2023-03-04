package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchOrderAcceptEventConfig;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2020-02-01 18:53
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsMatchOrderAcceptEventConfigService extends IService<RcsMatchOrderAcceptEventConfig> {
    /**
     * @return void
     * @Description //批量插入
     * @Param [list]
     * @Author kimi
     * @Date 2020/2/1
     **/
    void insertRcsMatchOrderAcceptEventConfigs(List<RcsMatchOrderAcceptEventConfig> list);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchOrderAcceptEventConfig>
     * @Description //根据赛事id查找
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/1
     **/
    List<RcsMatchOrderAcceptEventConfig> selectRcsMatchOrderAcceptEventConfig(Long matchId);

    /**
     * @Description   批量添加或者更改
     * @Param [list]
     * @Author  toney
     * @Date  11:34 2020/5/3
     * @return void
     **/
    void insertOrUpdate(List<RcsMatchOrderAcceptEventConfig> list);
}
