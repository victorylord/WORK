package com.panda.sport.rcs.vo.statistics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.utils.ForecastSortUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 预测货量表
 * </p>
 *
 * @author lithan auto
 * @since 2020-07-18
 */
@Data
@TableName("rcs_predict_bet_statis")
public class RcsPredictBetStatisVo extends RcsBaseEntity<RcsPredictBetStatisVo> {

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
     * 赛事类型:1赛前,2滚球
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
    private String marketId;
    /**
     * 盘口生成时间
     */
    @TableField(exist = false)
    private String marketCreatedTime;

    /**
     * 投注项
     */
    private String oddsItem;
    /**
     * 球队名称
     */
    @TableField(exist = false)
    private Map<String, String> teamNames;
    /**
     * 联赛名称
     */
    @TableField(exist = false)
    private Map<String, String> tournamentNames;

    /**
     * 基准分 投注时比分
     */
    private String betScore;

    /**
     * 货量
     */
    private BigDecimal betAmount;

    /**
     * 完整让球(盘口值)
     */
    private String marketValueComplete;

    /**
     * 当前让球(盘口值)
     */
    private String marketValueCurrent;

    /**
     * 投注笔数
     */
    private Long betNum;

    /**
     * 赔率和
     */
    private BigDecimal oddsSum;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * @Description 平均赔率
     * @Param
     * @Author Sean
     * @Date 14:15 2020/7/22
     * @return
     **/
    @TableField(exist = false)
    private String averageOdds;
    /**
     * 赛事开始时间
     */
    @TableField(exist = false)
    private String matchStartTime;
    /**
     * @Description 是否带基准分 1：带基准分；0：不带基准分
     * @Param
     * @Author Sean
     * @Date 14:15 2020/7/22
     * @return
     **/
    @TableField(exist = false)
    private Integer iSBenchmarkScore;
    /**
     * 比赛阶段
     */
    private Integer matchPeriodId;

    public Double getMarketValueCompleteNumber() {
        return ForecastSortUtils.getMarketValueCompleteNumber(this.marketValueComplete);
    }

    public Double getMarketValueCurrentNumber() {
        return ForecastSortUtils.getMarketValueCompleteNumber(this.marketValueCurrent);
    }

    public Integer getBetScoreNumber() {
        return ForecastSortUtils.getBetScoreNumber(this.betScore);
    }

    public Integer getPlayOptionsNumber() {
        return ForecastSortUtils.getPlayOptionsNumber(this.oddsItem);
    }

    /**
     * 跟前端保持字段统一增加了这个字段-----不要删了  sean
     */
    public String getPlayOptions() {
        return this.oddsItem;
    }
}
