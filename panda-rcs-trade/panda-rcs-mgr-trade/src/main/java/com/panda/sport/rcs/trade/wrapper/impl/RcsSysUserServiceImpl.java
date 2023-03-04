package com.panda.sport.rcs.trade.wrapper.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsSysUserMapper;
import com.panda.sport.rcs.pojo.RcsSysUser;
import com.panda.sport.rcs.trade.wrapper.RcsSysUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RcsSysUserServiceImpl extends ServiceImpl<RcsSysUserMapper, RcsSysUser> implements RcsSysUserService {

    @Resource
    private RcsSysUserMapper rcsSysUserMapper;

    @Override
    public int batchInsert(List<RcsSysUser> list) {
        return rcsSysUserMapper.batchInsert(list);
    }

    @Override
    public int insertOrUpdate(RcsSysUser record) {
        return rcsSysUserMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(RcsSysUser record) {
        return rcsSysUserMapper.insertOrUpdateSelective(record);
    }

    @Override
    public List<String> associatingUserName(String userName) {
        return rcsSysUserMapper.associatingUserName(userName);
    }

}


