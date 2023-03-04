package com.panda.sport.rcs.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @program: xindaima
 * @description: 用户特殊投注限额配置
 * @author: kimi
 * @create: 2020-12-11 16:01
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RcsUserSpecialBetLimitConfig extends RcsBaseEntity<RcsUserSpecialBetLimitConfig> implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 用户Id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long  userId;
    /**
     * 订单类型  1单关  2串关
     */
    private Integer  orderType;
    /**
     * 体育种类  0其他   -1全部
     */
    private Integer  sportId;
    /**
     * 单注赔付限额
     */
    private Long  singleNoteClaimLimit;
    /**
     * 单注赔付限额
     */
    @TableField(exist = false)
    private Long  oldSingleNoteClaimLimit;
    /**
     * 单注赔付限额上限值
     */
    @TableField(exist = false)
    private Long  singleNoteClaimLimitMax;
    /**
     * 单场赔付限额
     */
    private Long  singleGameClaimLimit;
    /**
     * 单场赔付限额
     */
    @TableField(exist = false)
    private Long  oldSingleGameClaimLimit;
    /**
     * 单场赔付限额上限值
     */
    @TableField(exist = false)
    private Long  singleGameClaimLimitMax;
    /**
     * 0 无效  1有效
     */
    private Integer status;
    /**
     *  0 无  1标签限额  2特殊百分比限额 3特殊单注单场限额 4特殊vip限额
     */
    private Integer specialBettingLimitType;
    /**
     * 百分比限额数据
     */
    private BigDecimal percentageLimit;
    /**
     * 百分比限额数据
     */
    @TableField(exist = false)
    private BigDecimal oldPercentageLimit;
    
    /**
     * 	更新时间
     */
    private Date updateTime;
}
