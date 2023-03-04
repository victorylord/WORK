package com.panda.sport.rcs.mapper;

import com.panda.sport.rcs.pojo.RcsBusinessUserPaidConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.vo.UserPaidVo;

import java.util.List;

/**
 * <p>
 * 用户最大赔付设置 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
public interface RcsBusinessUserPaidConfigMapper extends BaseMapper<RcsBusinessUserPaidConfig> {

    List<UserPaidVo> getUserPaidListView(Long businessId);

}
