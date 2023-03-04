package com.panda.sport.rcs.trade.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.pojo.odds.BalanceReqVo;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author holly
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@LogFormatAnnotion
public class MarketBalanceVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛种ID
     */
    private Long sportId;

    /**
     * 赛事ID
     */
    @LogFormatAnnotion(name = "赛事ID" )
    private Long matchId;

    /**
     * 玩法ID
     */
    @LogFormatAnnotion(name = "玩法ID" )
    private Long playId;

    /**
     * 盘口位置
     */
    @LogFormatAnnotion(name = "坑位N" )
    private Integer placeNum;

    /**
     * 位置ID
     */
    private String placeNumId;

    /**
     * 盘口ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    @LogFormatAnnotion(name = "盘口M" )
    private Long marketId;
    /**
     * 盘口值
     */
    private String marketValue;
    /**
     * 主队投注额
     */
    private Long homeAmount = 0L;
    /**
     * 客队投注额
     */
    private Long awayAmount = 0L;
    /**
     * 主队margin
     */
    private BigDecimal homeMargin;
    /**
     * 客队margin
     */
    private BigDecimal awayMargin;
    /**
     * 平局margin
     */
    private BigDecimal tieMargin;
    /**
     * 水差
     */
    private String awayAutoChangeRate;

    /**
     * 平衡值
     */
    @LogFormatAnnotion(name = "平衡值" )
    private Long balanceValue;
    /**
     * 当前投注方 1 主 2 客 * 平
     */
    private String currentSide;
    /**
     * 跳盘平衡值
     */
    private Long jumpMarketBalance;
    /**
     * 跳盘平衡值所在投注项
     */
    private String jumpMarketOddsType;
    /**
     * 创建人
     */
    @LogFormatAnnotion(name = "操作者账号" )
    private String createUser;
    /**
     * 类型：1 ：早盘 ，0： 滚球盘， 3： 冠军盘
     */
    @TableField(exist = false)
    private Integer matchType;
    /**
     * 修改时间
     */
    @LogFormatAnnotion(name = "操作时间" )
    private Long updateTime;

    public MarketBalanceVo(BalanceReqVo reqVo) {
        this.sportId = reqVo.getSportId();
        this.matchId = reqVo.getMatchId();
        this.playId = reqVo.getPlayId();
        this.placeNum = reqVo.getPlaceNum();
        this.placeNumId = reqVo.getPlaceNumId();
        this.marketId = reqVo.getMarketId();
    }
}
