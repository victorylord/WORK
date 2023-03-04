package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  TODO
 * @Date: 2019-11-08 20:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

@Data
public class RcsMatchConfigLogs extends RcsBaseEntity<RcsMatchConfigLogs> {
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
}