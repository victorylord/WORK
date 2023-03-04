package com.panda.sport.rcs.pojo.tourTemplate;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.panda.sport.rcs.utils.DateFormatSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.tourTemplate
 * @Description :  联赛模板玩法与margain之间引用
 * @Date: 2020-05-12 16:41
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsTournamentTemplatePlayMargainRef implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * margain表id
     */
    private Long margainId;
    /**
     * 时间轴值
     */
    private Long timeVal;
    /**
     * margain值
     */
    private String margain;
    /**
     * 0 投注额差值 1 投注额/赔付混合差值
     */
    private Integer balanceOption;
    /**
     * 最大赔率
     */
    private BigDecimal maxOdds;
    /**
     * 最小赔率
     */
    private BigDecimal minOdds;
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
     * 单注投注/赔付限额 单注赔付限额
     */
    private Long orderSinglePayVal;
    /**
     * 单注投注限额
     */
    private Long orderSingleBetVal;
    /**
     *预约赔付累计限额
     */
    private Long pendingOrderPayVal;
    /**
     * 用户累计赔付限额
     */
    private Long userMultiPayVal;
    /**
     * 累计差值
     */
    private Long multiDiffVal;
    /**
     * 累计概率变化
     */
    private BigDecimal multiOddsRate;
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
     * 操盘设置1：勾选   0：未勾选
     */
    private Integer isMarketConfig;
    /**
     * 额度设置1：勾选   0：未勾选
     */
    private Integer isQuotaConfig;
    /**
     * 暂停margin
     */
    private String pauseMargain;
    /**
     * 常规接单等待时间
     */
    private Integer normalWaitTime;
    /**
     * 暂停接单等待时间
     */
    private Integer pauseWaitTime;
    /**
     * 赔率（水差）变动幅度
     */
    @TableField(exist = false)
    private BigDecimal oddsAdjustRange;
    /**
     * 盘口调整幅度
     */
    @TableField(exist = false)
    private BigDecimal marketAdjustRange;
    /**
     * 相邻盘口差
     */
    @TableField(exist = false)
    private BigDecimal marketNearDiff;
//    /**
//     * 盘口调整幅度
//     */
//    @TableField(exist = false)
//    private String viceMarketRatio;

    @JsonSerialize(using = DateFormatSerializer.class)
    private Date createTime;

    @JsonSerialize(using = DateFormatSerializer.class)
    private Date updateTime;
    /**
     * 1:有效  2：无效  3:盘口数更改，副盘比例调整
     */
    private Integer status;
    /**
     * 一级累计盘口跳水金额
     **/
    private BigDecimal levelFirstMarketAmount;
    /**
     * 二级累计盘口跳水金额
     **/
    private BigDecimal levelSecondMarketAmount;
    /**
     * 累计上盘一级盘口变化值
     **/
    private BigDecimal homeLevelFirstMarketRate;
    /**
     * 累计上盘二级盘口变化值
     **/
    private BigDecimal homeLevelSecondMarketRate;
    /**
     * 累计下盘一级盘口变化值
     **/
    private BigDecimal awayLevelFirstMarketRate;
    /**
     * 累计下盘二级盘口变化值
     **/
    private BigDecimal awayLevelSecondMarketRate;
    /**
     * 单枪盘口跳水金额
     **/
    private BigDecimal marketSingleMaxAmount;
    /**
     * 累计盘口跳水金额
     **/
    private BigDecimal marketCumulativeMaxAmount;
    /**
     * 上盘单枪盘口变化值
     **/
    private BigDecimal homeSingleMarketRate;
    /**
     * 上盘累计盘口变化值
     **/
    private BigDecimal homeCumulativeMarketRate;
    /**
     * 下盘单枪盘口变化值
     **/
    private BigDecimal awaySingleMarketRate;
    /**
     * 下盘累计盘口变化值
     **/
    private BigDecimal awayCumulativeMarketRate;
    /**
     * 抽水方式  0:每项除Margin 1:Margin转概率均分
     */
    private Integer pumpingOption;
    /**
     * 玩法提前结算开关 0:关 1:开
     */
    private Integer categoryPreStatus;
    /**
     * CashOut Margin
     */
    private Long cashOutMargin;
    /**
     * 最大盘口数
     */
    private Integer marketCount;
    /**
     * 副盘限额比列
     */
    private String viceMarketRatio;

    public static void main(String[] args) {
        String str = "{\"1.01-1.05\":1,\"1.06-1.25\":1,\"1.26-1.39\":1,\"1.40-1.60\":1,\"1.61-1.85\":1,\"1.86-1.88\":1,\"1.89-2.00\":1}";
        Map<String,String> map = JSON.parseObject(str, Map.class);
        System.out.println(map);
        System.out.println(map.get("1") ==null);


    }
}
