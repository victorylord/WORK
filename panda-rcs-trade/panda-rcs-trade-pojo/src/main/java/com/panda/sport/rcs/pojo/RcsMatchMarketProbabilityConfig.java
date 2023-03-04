package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * <p>
 * 盘口设置表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsMatchMarketProbabilityConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 赛事id
     */
    private Long matchId;
    /**
     * 玩法id
     */
    private Long playId;

    /**
     * 盘口id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(exist = false)
    private Long marketId;

    /**
     * 创建时间
     */
    @TableField(exist = false)
    private Timestamp createTime;

    /**
     * 创建人
     */
    private String operaterId;

    /**
     * 修改时间
     */
    @TableField(exist = false)
    private Timestamp modifyTime;
    /**
     * 投注项
     */
    private String oddsType;
    /**
     * 概率差
     */
    private BigDecimal probability;

    public RcsMatchMarketProbabilityConfig(Long matchId, Long playId, Long marketId) {
        this.matchId = matchId;
        this.playId = playId;
        this.marketId = marketId;
    }
}
