package com.panda.sport.rcs.vo.statistics;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.enums.BalanceTypeEnum;
import com.panda.sport.rcs.pojo.odds.BalanceReqVo;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.vo.statistics
 * @Description :  TODO
 * @Date: 2019-11-11 18:42
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceVo extends RcsBaseEntity<BalanceVo> {

    private static final long serialVersionUID = 1L;

    /**
     * 1-跳赔平衡值，2-跳盘平衡值
     */
    private Integer type;

    /**
     * 赛种ID
     */
    private Long sportId;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 玩法ID
     */
    private Long marketCategoryId;

    /**
     * 盘口位置
     */
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
    private Long marketId;
    /**
     * 盘口是否关联 1关联
     */
    private Integer relevanceType;
    /**
     * @Description 主队水差
     * @Param
     * @Author Sean
     * @Date 15:36 2020/1/11
     * @return
     **/
    private Integer changeRate;
    /**
     * 主队投注额
     */
    private Long homeAmount;
    /**
     * 客队投注额
     */
    private Long awayAmount;
    /**
     * 和投注额
     */
    private Long tieAmount;
    /**
     * 水差差值
     */
    private BigDecimal waterValue;
    /**
     * 改变水差状态
     */
    private Integer changeRateStatus;
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

    public BalanceVo(BalanceReqVo reqVo) {
        this.sportId = reqVo.getSportId();
        this.matchId = reqVo.getMatchId();
        this.marketCategoryId = reqVo.getPlayId();
        this.placeNum = reqVo.getPlaceNum();
        this.placeNumId = reqVo.getPlaceNumId();
        this.marketId = reqVo.getMarketId();
    }

    public BalanceVo(Long matchId, Long playId, Long marketId) {
        this.matchId = matchId;
        this.marketCategoryId = playId;
        this.marketId = marketId;
    }

    public BalanceVo(Long matchId, Long marketCategoryId, Long marketId, Long balanceValue, Integer relevanceType) {
        this.matchId = matchId;
        this.marketCategoryId = marketCategoryId;
        this.marketId = marketId;
        this.balanceValue = balanceValue;
        this.relevanceType = relevanceType;
    }

    public BalanceVo(Long matchId, Long marketCategoryId, Long marketId, BigDecimal waterValue) {
        this.matchId = matchId;
        this.marketCategoryId = marketCategoryId;
        this.marketId = marketId;
        this.waterValue = waterValue;
    }

    public BalanceVo(Long matchId, Long marketCategoryId, Long marketId, Integer changeRate) {
        this.matchId = matchId;
        this.marketCategoryId = marketCategoryId;
        this.marketId = marketId;
        this.changeRate = changeRate;
    }

    //同时减去最小的
    public BalanceVo(Long matchId, Long marketId, Long balanceValue, Long homeAmount, Long awayAmount, Long tieAmount) {
        this.matchId = matchId;
        this.marketId = marketId;
        this.balanceValue = balanceValue;
        this.homeAmount = homeAmount;
        this.awayAmount = awayAmount;
        this.tieAmount = tieAmount;
    }

    public String generateKey() {
        if (BalanceTypeEnum.isJumpMarket(type)) {
            return String.format("jumpMarketBalance_%s_%s_trade", getMatchId(), getMarketCategoryId());
        } else {
            return String.format("jumpOddsBalance_%s_%s_trade", getMatchId(), getMarketCategoryId());
        }
    }
}
