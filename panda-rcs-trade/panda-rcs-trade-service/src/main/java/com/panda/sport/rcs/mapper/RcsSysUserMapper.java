package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsSysUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RcsSysUserMapper extends BaseMapper<RcsSysUser> {
    int batchInsert(@Param("list") List<RcsSysUser> list);

    int insertOrUpdate(RcsSysUser record);

    int insertOrUpdateSelective(RcsSysUser record);

    List<String> associatingUserName(@Param("userName") String userName);

}