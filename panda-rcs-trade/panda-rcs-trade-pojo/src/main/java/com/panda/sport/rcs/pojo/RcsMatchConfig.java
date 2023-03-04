package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description  :  赛事信息
 * @author       :  Administrator
 * @Date:  2019-11-08 20:18
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
*/ 

@Data
@TableName(value = "rcs_match_config")
public class RcsMatchConfig extends RcsBaseEntity<RcsMatchConfig> {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 赛事ID
     */
    @TableField(value = "match_id")
    private Long matchId;

    /**
     * 赛事状态
     */
    @TableField(value = "operate_match_status")
    private Integer operateMatchStatus;

    /**
     * 修改时间
     */
    @TableField(value = "modify_time")
    private Date modifyTime;

    /**
     * 修改人
     */
    @TableField(value = "modify_user")
    private String modifyUser;

    /**
     * 操盘类型  0是自动  1是手动
     */
    @TableField(value = "trade_type")
    private Integer tradeType;

    /**
     * 自动调价参数
     */
    @TableField(value = "price_adjustment_parameters")
    private BigDecimal priceAdjustmentParameters;

    @TableField(exist = false)
    private Object teamVos;

    /**
     * 赛前操盘平台 MTS,PA
     */
    private String preRiskManagerCode;
    /**
     * 滚球操盘平台 MTS,PA
     */
    private String liveRiskManagerCode;
}