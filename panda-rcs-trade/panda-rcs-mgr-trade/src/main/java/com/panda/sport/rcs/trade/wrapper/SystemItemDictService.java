package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.SystemItemDict;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2019-10-16 11:52
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface SystemItemDictService extends IService<SystemItemDict> {
    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.SystemItemDict>
     * @Description //获取玩法所属时段
     * @Param []
     * @Author kimi
     * @Date 2019/10/16
     **/
    List<SystemItemDict> getPlayingTimes();
}
