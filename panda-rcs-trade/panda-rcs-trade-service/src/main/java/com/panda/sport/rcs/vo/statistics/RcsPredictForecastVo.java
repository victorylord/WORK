package com.panda.sport.rcs.vo.statistics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.utils.ForecastSortUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 预测forecast表
 * </p>
 *
 * @author lithan auto
 * @since 2020-07-18
 */
@Data
@TableName("rcs_predict_forecast")
public class RcsPredictForecastVo extends RcsBaseEntity<RcsPredictForecastVo> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 运动种类
     */
    private Integer sportId;

    /**
     * 标准赛事id
     */
    private Long matchId;
    /**
     * 赛事开始时间
     */
    @TableField(exist = false)
    private String matchStartTime;
    /**
     * 赛事类型:1赛前;2滚球; null不区分
     */
    private Integer matchType;
    /**
     * 玩法类型 1 让球 2 大小球
     */
    @TableField(exist = false)
    private Integer playType;

    /**
     * 玩法id
     */
    private Integer playId;

    /**
     * 盘口id
     */
    private Long marketId;

    /**
     * 投注项
     */
    private String oddsItem;

    /**
     * 基准分 投注时比分
     */
    private String betScore;

    /**
     * forecast预测分数(1.大小玩法时,表示两队进球和. 2.让球玩法时,表示进球数量差值)
     */
    private Integer forecastScore;

    /**
     * 预测盈利(庄家视角)
     */
    private BigDecimal profitAmount;
    /**
     * 是否收藏 1 收藏 0 非收藏
     */
    @TableField(exist = false)
    private Integer isFavorite;
    /**
     * 标准赛事id
     */
    @TableField(exist = false)
    private List<Long> matchIds;
    /**
     * 玩法阶段 null：全部；1：全场；2：半场
     */
    @TableField(exist = false)
    private Integer playPhaseType;

    /**
     * 完整让球(盘口值)
     */
    private String marketValueComplete;

    /**
     * 当前让球(盘口值)
     */
    private String marketValueCurrent;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 让球期望
     */
    @TableField(exist = false)
    private Map<String,Object> handicapMap;
    /**
     * 大小球期望
     */
    @TableField(exist = false)
    private Map<String,Object> overUnderMap;
    /**
     * 球队名称
     */
    @TableField(exist = false)
    private Map<String,String> teamNames;
    /**
     * 联赛名称
     */
    @TableField(exist = false)
    private Map<String,String> tournamentNames;
    /**
     * 1：全场；2：半场
     */
    @TableField(exist = false)
    private String playPhaseTypeGroup;

    public Double getMarketValueCompleteNumber(){
        return ForecastSortUtils.getMarketValueCompleteNumber(this.marketValueComplete);
    }

    public Double getMarketValueCurrentNumber(){
        return ForecastSortUtils.getMarketValueCompleteNumber(this.marketValueCurrent);
    }

    public Integer getBetScoreNumber(){
        return ForecastSortUtils.getBetScoreNumber(this.betScore);
    }

    public String getPlayPhaseTypeGroup(){
        return ForecastSortUtils.getPlayPhaseTypeGroup(this.playId);
    }
}
