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
import java.sql.Timestamp;

/**
 * <p>
 * 赛事最大赔付配置
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsBusinessMatchPaidConfig extends RcsBaseEntity<RcsBusinessMatchPaidConfig> {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户id
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;

    /**
     * 体育类型
     */
    private Long sportId;

    /**
     * 联赛级别
     */
    private Long tournamentLevelId;
    /**
     * 联赛级别
     */
    private Integer tournamentLevel;
    /**
     * 联赛别名
     */
    private String tournamentLevelCode;

    /**
     * 单场最大赔付比例  %
     */
    private BigDecimal matchMaxPayRate;

    /**
     * 单场最大赔付
     */
    private BigDecimal matchMaxPayVal;

    /**
     * 单场串关最大赔付值
     */
    private BigDecimal matchMaxConPayVal;

    /**
     * 单场串关最大赔付比例%
     */
    private BigDecimal matchMaxConPayRate;

    @TableField(exist = false)
    private Timestamp crtTime;

    @TableField(exist = false)
    private Timestamp updateTime;

    /**
     * 状态
     */
    private Integer status;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
