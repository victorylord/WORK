package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.SysUser;
import org.springframework.stereotype.Service;

/**
 * 用户查询的mapper接口
 * 基于mybatis plus定义的
 * 不需要实现
 */
@Service
public interface UserMapper extends BaseMapper<SysUser> {
//    List<User> getAll();
//
//    User getOne(Integer id);
//
//    void insert(User user);
}
