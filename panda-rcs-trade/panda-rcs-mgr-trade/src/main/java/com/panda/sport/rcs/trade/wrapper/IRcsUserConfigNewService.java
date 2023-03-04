package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsUserConfig;
import com.panda.sport.rcs.pojo.RcsUserConfigNew;
import com.panda.sport.rcs.trade.vo.RcsUserConfigVo;

import java.util.List;
import java.util.Map;

/**
 * user-config 优化配置
 *
 * @description:
 * @author: magic
 * @create: 2022-07-09 10:15
 **/
public interface IRcsUserConfigNewService extends IService<RcsUserConfigNew> {

    Map<Long, RcsUserConfigVo> getByUserIds(List<Long> userIds);

    Map<Long, RcsUserConfigVo> getByUserIds(Long... userIds);

    void save(RcsUserConfigVo rcsUserConfigVo,Integer traderId);
}