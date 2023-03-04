package com.panda.sport.rcs.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 盘口位置统计表
 *
 * @author lithon 2020-10-04
 */
@Data
@Accessors(chain = true)
public class RcsMarketNumStatis implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 体育种类
     */
    private Long sportId;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 玩法ID
     */
    private Integer marketCategoryId;

    /**
     * 盘口位置
     */
    private Integer placeNum;

    /**
     * 投注项标识
     */
    private String oddsType;

    /**
     * 投注笔数（投注项）
     */
    private BigDecimal betOrderNum;

    /**
     * 投注量
     */
    private BigDecimal betAmount;

    /**
     * 盈利值
     */
    private BigDecimal profitValue;

    /**
     * 联赛id
     */
    private Long standardTournamentId;

    /**
     * 1 早盘  2 滚球
     */
    private Integer matchType;

    /**
     * 最大派奖金额
     */
    private BigDecimal paidAmount;


    /**
     * 盘口ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(exist = false)
    private Long matchMarketId;
    /**
     * 投注项ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(exist = false)
    private Long marketOddsId;
}
