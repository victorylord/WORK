package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Service
public interface RcsRoleMapper extends BaseMapper<RcsRole> {
    List<RcsRole> selectByUrl(@Param("url") String url);

    List<RcsRole> selectByUserId(@Param("id") Integer id);
}
