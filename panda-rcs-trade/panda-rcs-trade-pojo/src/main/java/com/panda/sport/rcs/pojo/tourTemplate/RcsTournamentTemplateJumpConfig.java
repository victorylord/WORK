package com.panda.sport.rcs.pojo.tourTemplate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.panda.sport.rcs.utils.DateFormatSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author :  carver
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.tourTemplate
 * @Description :  综合操盘跳分设置表
 * @Date: 2021-09-29 16:41
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsTournamentTemplateJumpConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 赛种
     */
    private Long sportId;
    /**
     * 联赛id
     */
    private Long tournamentId;
    /**
     * 1：早盘；0：滚球
     */
    private Integer matchType;
    /**
     * 最大投注/最大赔付
     */
    private Long maxSingleBetAmount;
    /**
     * 马来 0 投注额差值 1 投注额/赔付组合差值
     */
    private Integer spreadBalanceOption;
    /**
     * 马来 最大赔率
     */
    private BigDecimal spreadMaxOdds;
    /**
     * 马来 最小赔率
     */
    private BigDecimal spreadMinOdds;
    /**
     * 跳赔规则
     * 0 累计/单枪跳分 1 累计差值跳分
     */
    private Integer oddChangeRule;
    /**
     * 上盘累计限额
     */
    private Long homeMultiMaxAmount;
    /**
     * 上盘单枪限额
     */
    private Long homeSingleMaxAmount;
    /**
     * 上盘单枪赔率变化率
     */
    private BigDecimal homeSingleOddsRate;
    /**
     * 上盘累计赔率变化率
     */
    private BigDecimal homeMultiOddsRate;
    /**
     * 下盘单枪赔率变化率
     */
    private BigDecimal awaySingleOddsRate;
    /**
     * 下盘累计赔率变化率
     */
    private BigDecimal awayMultiOddsRate;
    /**
     * 上盘一级限额
     */
    private Long homeLevelFirstMaxAmount;
    /**
     * 上盘二级限额
     */
    private Long homeLevelSecondMaxAmount;
    /**
     * 上盘一级赔率变化率
     */
    private BigDecimal homeLevelFirstOddsRate;
    /**
     * 上盘二级赔率变化率
     */
    private BigDecimal homeLevelSecondOddsRate;
    /**
     * 下盘一级赔率变化率
     */
    private BigDecimal awayLevelFirstOddsRate;
    /**
     * 下盘二级赔率变化率
     */
    private BigDecimal awayLevelSecondOddsRate;
    /**
     * 欧赔 0 投注额差值 1 投注额/赔付组合差值
     */
    private Integer marginBalanceOption;
    /**
     * 欧赔 最大赔率
     */
    private BigDecimal marginMaxOdds;
    /**
     * 欧赔 最小赔率
     */
    private BigDecimal marginMinOdds;
    /**
     * 累计差值
     */
    private Long multiDiffVal;
    /**
     * 累计概率变化
     */
    private BigDecimal multiOddsRate;

    @JsonSerialize(using = DateFormatSerializer.class)
    private Date createTime;

    @JsonSerialize(using = DateFormatSerializer.class)
    private Date updateTime;
    /**
     * 是否倍数跳赔，0-否，1-是
     */
    private Integer isMultipleJumpOdds;
}
