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

import java.math.BigDecimal;

/**
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description  :  保存盈利值、实货量
 * @author       :  Administrator
 * @Date:  2019-11-01 16:18
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
 */

@Data
public class RcsMarketOddsConfig extends RcsBaseEntity<RcsMarketOddsConfig> {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 体育种类
     */
    private Long sportId;

    /**
     * 标准联赛 id.
     */
    private Long standardTournamentId;

    /**
     * 赛事ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long matchId;

    /**
     * 玩法ID
     */
    private Integer marketCategoryId;

    /**
     * 类型
     */
    private String matchType;

    /**
     * 盘口ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long matchMarketId;

    /**
     * 投注项ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long marketOddsId;

    /**
     * 投注单数（投注项）
     */
    private BigDecimal betOrderNum;

    /**
     * 实货量
     */
    private BigDecimal betAmount;

    /**
     * 是否为主队
     */
    private Boolean isHome;

    /**
     * 最大赔付金额
     */
    private BigDecimal paidAmount;
    /**
     * 玩法阶段
     */
    @TableField(exist = false)
    private Integer playPhaseType;

    /**
     * @Description   实货量除以100
     * @Param []
     * @Author  toney
     * @Date  14:27 2019/12/6
     * @return java.math.BigDecimal
     **/
    public BigDecimal getBetAmount1(){
        if(betAmount == null){
            return BigDecimal.ZERO;
        }
        return betAmount.divide(BigDecimal.valueOf(100));
    }

    /**
     * 盈利值
     */
    private BigDecimal profitValue;

    /**
     * @Description   盈利值除以100
     * @Param []
     * @Author  toney
     * @Date  14:28 2019/12/6
     * @return java.math.BigDecimal
     **/
    public BigDecimal getProfitValue1(){
        if(profitValue == null){
            return BigDecimal.ZERO;
        }
        return profitValue.divide(BigDecimal.valueOf(100));
    }

}