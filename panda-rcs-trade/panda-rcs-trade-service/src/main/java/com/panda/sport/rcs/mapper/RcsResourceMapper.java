package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsResource;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Service
public interface RcsResourceMapper extends BaseMapper<RcsResource> {
    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsResource>
     * @Description //用户id
     * @Param [id]
     * @Author kimi
     * @Date 2019/10/7
     **/

    List<RcsResource> getResourceList(@Param("id") Integer id);
}
