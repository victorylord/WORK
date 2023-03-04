package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.TUser;
import org.springframework.stereotype.Service;

/**
 * @author :  Sean
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  用户注册对应数据库操作
 * @Date: 2019-10-21 16:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public interface UserRegisteredMapper extends BaseMapper<TUser> {
}
