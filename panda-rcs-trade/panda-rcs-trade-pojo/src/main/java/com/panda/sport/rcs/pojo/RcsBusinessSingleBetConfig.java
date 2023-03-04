package com.panda.sport.rcs.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
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
 * @Date:  2019-11-22 15:04
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
*/ 

@Data
@TableName(value = "rcs_business_single_bet_config")
public class RcsBusinessSingleBetConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 商户ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;

    /**
     * 体育类型 standard_sport_type .id
     */
    private Long sportId;

    /**
     * 投注阶段  未开赛 和 滚球  0:赛前 1:滚球
     */
    private Integer matchType;

    /**
     * 所属时段  system_item_dict.value
     */
    private Integer timePeriod;

    /**
     * 玩法名称编码 id standard_sport_market_category
     */
    private Integer playId;

    /**
     * 联赛级别
     */
    private Integer tournamentLevel;


    /**
     * 单注最大下注额
     */
    private BigDecimal orderMaxValue;

    /**
     * 百分比
     */
    private BigDecimal orderMaxRate;

    private Date crtTime;

    private Date updateTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序設置
     */
    private Integer orderNumber;

}