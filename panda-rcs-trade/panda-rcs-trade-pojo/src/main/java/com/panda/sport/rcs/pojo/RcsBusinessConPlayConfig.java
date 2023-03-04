package com.panda.sport.rcs.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Data;

/**
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description  :  TODO
 * @author       :  Administrator
 * @Date:  2019-11-22 16:29
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
*/ 

@Data
@TableName(value = "rcs_business_con_play_config")
public class RcsBusinessConPlayConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户Id
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;

    /**
     * 1-单注串关最大赔付值
     * 2-单注串关最低投注额
     * 3-单注串关限额占单关限额比例
     */
    private Integer playType;
    /**
     * 设置值
     */
    private BigDecimal playValue;
    /**
     * 百分比
     */
    private BigDecimal playRate;

    @TableField(exist = false)
    private Timestamp crtTime;

    @TableField(exist = false)
    private Timestamp updateTime;

    private Integer status;
}