package com.panda.sport.rcs.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 操盘手设置用户参数限制商户设置表
 * </p>
 *
 * @author ${author}
 * @since 2022-04-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RcsTradeRestrictMerchantSetting implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 玩家ID 对应userId
     */
    private Long userId;

    /**
     * 	体育种类Id 逗号 分隔
     */
    private String sportIds;

    /**
     * 操盘手设置的特殊百分比限额
     */
    private BigDecimal percentageLimit;

    /**
     * 操盘手设置的投注额外延时
     */
    private Integer betExtraDelay;

    /**
     * 操盘手设置的标签行情等级ID（赔率分组）
     */
    private Integer tagMarketLevelId;

    /**
     * 操盘者Id
     */
    private Integer tradeId;

    /**
     * 更新时间
     */
     @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
