package com.panda.sport.rcs.pojo.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.panda.sport.rcs.common.NumberUtils;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author :  Sean
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.report
 * @Description :  TODO
 * @Date: 2019-12-25 14:37
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class BaseRcsOrderStatisticTime extends RcsBaseEntity<BaseRcsOrderStatisticTime> {
    /**
     * 赛种ID
     */
    private Integer sportId;
    /**
     * 联赛ID
     */
    private Integer tournamentId;
    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private Integer matchType;
    /**
     * 玩法ID
     */
    private Integer playId;
    /**
     * 注单状态
     */
    private Integer orderStatus;
    /**
     * 受注量
     */
    private BigDecimal orderAmountSum;
    /**
     * 投注笔数
     */
    private Long orderCount;
    /**
     * 投注人数
     */
    private Long customerCount;
    /**
     * 单笔平均投注量
     */
    @TableField(value = "false")
    private BigDecimal amountPerOrderCount;

    public void initAmountPerOrderCount(){
        if(this.getOrderCount() == 0 || this.getOrderAmountSum().equals(BigDecimal.valueOf(0))) {
            this.setAmountPerOrderCount(BigDecimal.valueOf(0l));
        }else{
            this.setAmountPerOrderCount(this.getOrderAmountSum().divide(BigDecimal.valueOf(this.getOrderCount()),0, BigDecimal.ROUND_HALF_UP));
        }

    }
    /**
     * 人均投注笔数
     */
    @TableField(value = "false")
    private BigDecimal orderCountPerCustomer;

    public void initOrderCountPerCustomer(){
        if(this.getCustomerCount() == 0 || this.getOrderCount().equals(BigDecimal.valueOf(0))) {
            this.setOrderCountPerCustomer(BigDecimal.valueOf(0l));
        }else{
            this.setOrderCountPerCustomer(NumberUtils.getBigDecimal(this.getOrderCount()).divide(NumberUtils.getBigDecimal(this.getCustomerCount()), 0, BigDecimal.ROUND_HALF_UP));
        }
    }
    /**
     * 人均投注量
     */
    @TableField(value = "false")
    private BigDecimal amountPerCustomer;

    public void initAmountPerCustomer(){
        if(this.customerCount == 0 || this.getOrderAmountSum().equals(BigDecimal.valueOf(0))) {
            this.setAmountPerCustomer(BigDecimal.valueOf(0l));
        }else{
            this.setAmountPerCustomer(this.getOrderAmountSum().divide(BigDecimal.valueOf(this.getCustomerCount()),0, BigDecimal.ROUND_HALF_UP));
        }
    }
    /**
     * 单注大于10000笔数
     */
    private Long amountGttenThousandCount;
    /**
     * 单注大于5000笔数
     */
    private Long amountGtfiveThousandCount;
    /**
     * 单注大于2000笔数
     */
    private Long amountGttwoThousandCount;
    /**
     * 单注大于1000笔数
     */
    private Long amountGtoneThousandCount;
    /**
     * 单注小于1000笔数
     */
    private Long amountLtoneThousandCount;
    /**
     * 平台盈利
     */
    private BigDecimal amountProfit;
    /**
     * 平台盈利百分比
     */
    private BigDecimal amountProfitPerOrderCount;

    public BaseRcsOrderStatisticTime(){}

    public BaseRcsOrderStatisticTime(CalcSettleItem calcSettleItem){
        this.sportId = calcSettleItem.getSportId();
        this.tournamentId = calcSettleItem.getTournamentId();
        this.matchType = calcSettleItem.getMatchType();
        this.playId = calcSettleItem.getPlayId();
        this.orderStatus = calcSettleItem.getOrderStatus();
        this.orderAmountSum = new BigDecimal("0");
        this.orderCount = 0L;
        this.amountLtoneThousandCount = 0L;
        this.amountGttenThousandCount = 0L;
        this.amountGtfiveThousandCount = 0L;
        this.amountGttwoThousandCount = 0L;
        this.amountGtoneThousandCount = 0L;
        this.amountProfit = new BigDecimal("0");
    }

    public BaseRcsOrderStatisticTime(Integer sportId, Integer tournamentId, Integer matchType, Integer playId, Integer orderStatus) {
        this.sportId = sportId;
        this.tournamentId = tournamentId;
        this.matchType = matchType;
        this.playId = playId;
        this.orderStatus = orderStatus;
        this.orderAmountSum = new BigDecimal("0");
        this.orderCount = 0L;
        this.amountLtoneThousandCount = 0L;
        this.amountGttenThousandCount = 0L;
        this.amountGtfiveThousandCount = 0L;
        this.amountGttwoThousandCount = 0L;
        this.amountGtoneThousandCount = 0L;
    }

    public void dealWithCalcSettleItem(CalcSettleItem calcSettleItem) {
        this.setOrderAmountSum(this.getOrderAmountSum().add(calcSettleItem.getBetAmount()));
        this.setOrderCount(this.getOrderCount() + 1);
        double betAmount = calcSettleItem.getBetAmount().doubleValue()/100;
        if (betAmount < BaseConstants.AMOUNT_ONE_THOUSAND) {
            this.setAmountLtoneThousandCount(this.getAmountGtoneThousandCount() + 1);
        } else {
            if (betAmount >= BaseConstants.AMOUNT_TEN_THOUSAND) {
                this.setAmountGttenThousandCount(this.getAmountGttenThousandCount() + 1);
            }
            if (betAmount >= BaseConstants.AMOUNT_FIVE_THOUSAND) {
                this.setAmountGtfiveThousandCount(this.getAmountGtfiveThousandCount() + 1);
            }
            if (betAmount >= BaseConstants.AMOUNT_TWO_THOUSAND) {
                this.setAmountGttwoThousandCount(this.getAmountGttwoThousandCount() + 1);
            }
            this.setAmountGtoneThousandCount(this.getAmountGtoneThousandCount() + 1);
        }
        if(org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE.intValue() == calcSettleItem.getOrderStatus()){
            this.setAmountProfit(this.getAmountProfit().subtract(calcSettleItem.getSettleAmount()).add(calcSettleItem.getBetAmount()));
        }
    }

    public static void main(String args[]){
        System.out.println(BigDecimal.valueOf(0));
    }
}
