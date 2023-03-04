package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsUserSpecialBetLimitConfig;
import com.panda.sport.rcs.vo.UserReferenceLimitVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-12-11 16:17
 **/
@Component
public interface RcsUserSpecialBetLimitConfigMapper extends BaseMapper<RcsUserSpecialBetLimitConfig> {
    /**
     * 查询篮球和足球的最大限额
     * @return
     */
    List<UserReferenceLimitVo> selectByBasketballAndFootball();

    /**
     * 其他球类
     * @return
     */
    List<UserReferenceLimitVo> selectSingeOrderByOther();
    /**
     * 其他球类
     * @return
     */
    List<UserReferenceLimitVo> selectSingeMatchByOther();

    /**
     * 查询商户比例
     * @param userId
     * @return
     */
    BigDecimal selectUserQuotaRatio(@Param("userId") Long userId);

    /**
     * 串关的
     */
    BigDecimal selectSserQuotaCrossLimit();

    /**
     * 更改状态
     * @param userId
     */
    void  updateRcsUserSpecialBetLimitConfigStatus(@Param("userId") Long userId);

    /**
     * 更新数据
     * @param rcsUserSpecialBetLimitConfigList
     */
    void insertOrUpdateUserSpecialBetLimitConfig(@Param("rcsUserSpecialBetLimitConfigList") List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList);

    /**
     * 查询串关数据
     * @return
     */
    BigDecimal selectCrossDayCompensation();

    int updateUserSpecialBetLimitConfigBy(RcsUserSpecialBetLimitConfig config);
}
