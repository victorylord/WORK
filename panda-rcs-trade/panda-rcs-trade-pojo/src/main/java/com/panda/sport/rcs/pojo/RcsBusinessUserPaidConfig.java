package com.panda.sport.rcs.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 用户最大赔付设置
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsBusinessUserPaidConfig extends RcsBaseEntity<RcsBusinessUserPaidConfig> {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户id
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户单日最大赔付比例 %
     */
    private Integer userDayPayRate;

    /**
     * 用户单日最大赔付
     */
    private BigDecimal userDayPayVal;

    /**
     * 用户单场最大赔付比例 %
     */
    private Integer userMatchPayRate;

    /**
     * 用户单场最大赔付
     */
    private BigDecimal userMatchPayVal;

    @TableField(exist = false)
    private LocalDateTime crtTime;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    private Integer status;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
