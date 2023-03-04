package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.TUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  用户
 * @Date: 2020-06-03 21:42
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
public interface TUserMapper extends BaseMapper<TUser> {

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    TUser selectByUserId(@Param("userId") Long userId);

    int insertOrUpdate(TUser user);

    /**
     * 1782 查询出来之后再到缓存查询具体类型，因为数据库数据不一定准确
     *
     * @return
     */
    List<Long> findByPercentageLimit(@Param("percentageLimit") BigDecimal percentageLimit, @Param("merchantCode") String merchantCode);
}
