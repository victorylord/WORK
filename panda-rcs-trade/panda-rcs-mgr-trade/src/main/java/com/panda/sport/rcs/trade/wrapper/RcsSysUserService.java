package com.panda.sport.rcs.trade.wrapper;


import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsSysUser;

import java.util.List;

public interface RcsSysUserService extends IService<RcsSysUser> {


    int batchInsert(List<RcsSysUser> list);

    int insertOrUpdate(RcsSysUser record);

    int insertOrUpdateSelective(RcsSysUser record);

    List<String> associatingUserName(String userName);
}


