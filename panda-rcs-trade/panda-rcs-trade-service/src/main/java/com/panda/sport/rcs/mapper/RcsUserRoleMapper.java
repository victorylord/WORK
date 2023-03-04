package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsUserRole;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户角色表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Service
public interface RcsUserRoleMapper extends BaseMapper<RcsUserRole> {
    List<String> getAuthorityList(Integer id);
}
