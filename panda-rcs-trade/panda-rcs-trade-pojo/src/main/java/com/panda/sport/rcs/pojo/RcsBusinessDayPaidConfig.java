package com.panda.sport.rcs.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 商户单日最大赔付
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsBusinessDayPaidConfig extends RcsBaseEntity<RcsBusinessDayPaidConfig> {
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;

    private String businessName;

    /**
     * 制动比例  %
     */
    private Integer stopRate;

    /**
     * 制动值
     */
    private BigDecimal stopVal;

    /**
     * 高危 单位 %
     */
    private Integer warnLevel1Rate;

    /**
     * 高危值
     */
    private BigDecimal warnLevel1Val;

    /**
     * 危险  单位 %
     */
    private Integer warnLevel2Rate;

    /**
     * 危险值
     */
    private BigDecimal warnLevel2Val;

    @TableField(exist = false)
    private LocalDateTime crtTime;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    private long expireTime;

    /**
     * 状态
     */
    private Integer status;


    @Override
    public Serializable pkVal() {
        return this.businessId;
    }

}
