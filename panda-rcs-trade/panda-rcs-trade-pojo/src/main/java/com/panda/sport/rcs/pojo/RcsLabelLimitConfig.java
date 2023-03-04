package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: xindaima
 * @description: 标签限额配置
 * @author: kimi
 * @create: 2021-02-04 12:18
 **/
@Data
public class RcsLabelLimitConfig {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 标签Id
     */
    private Integer tagId;
    /**
     * 表投注额外延时  单位秒
     */
    private Integer betExtraDelay;
    /**
     * 体育种类
     */
    private Integer sportId;
    /**
     * 是否有特殊投注限额  0没有 1有
     */
    private Integer specialBettingLimit;

    /**
     * 赔率分组等级
     */
    private String tagMarketLevelId;


    /**
     * 限额百分比  单位0
     */
    private BigDecimal limitPercentage;
    /**
     * 提前结算extraMargin
     */
    private BigDecimal extraMargin;
    /**
     * 创建人id
     */
    private Integer updateUserId;
    /**
     * 货量百分比
     */
    private BigDecimal volumePercentage;
}
