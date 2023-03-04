package com.panda.sport.rcs.mongo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.common.NumberUtils;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :  赛事盘口信息
 */
@Data
public class MatchMarketVo implements Comparable {

    /**
     * 盘口ID
     */
    @Field(value = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 玩法ID
     */
    private Long marketCategoryId;

    /**
     * 盘口ID
     */
    private Long marketId;

    /**
     * 盘口名称，多语言
     */
    private I18nBean names;

    /**
     * 盘口名称编码
     */
    private Long nameCode;

    /**
     * 平衡值
     */
    private Long balance;

    /**
     * 盘口类型。1-赛前盘，0-滚球盘
     */
    private Integer marketType;

    /**
     * 三方盘口源状态,给风控操盘使用，融合侧不做修改
     */
    private Integer thirdMarketSourceStatus;

    /**
     * 旧三方盘口源状态,给风控操盘使用，融合侧不做修改
     */
    private Integer oldThirdMarketSourceStatus;

    /**
     * 盘口位置id
     */
    private String placeNumId;

    /**
     * 盘口位置
     */
    private Integer placeNum;

    /**
     * 盘口差
     */
    private Double marketHeadGap;

    /**
     * 水差
     */
    private BigDecimal waterValue;

    /**
     * margin值
     */
    private BigDecimal marginValue;

    /**
     * 附加字段1
     */
    private String addition1;

    /**
     * 附加字段2
     */
    private String addition2;
    /**
     * 附加字段3
     */
    private String addition3;
    /**
     * 附加字段4
     */
    private String addition4;
    /**
     * 附加字段5
     */
    private String addition5;

    /**
     * 取值:  SR BC分别代表: SportRadar、FeedConstruc. 详情见data_source
     */
    private String dataSourceCode;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 通过以上三种状态加上操盘赛事状态得出的最终状态
     * 盘口状态0-5. 0:active 开盘, 1:suspended 封盘, 2:deactivated 关盘, 3:settled 已结算, 4:cancelled 已取消, 5:handedOver  盘口的中间状态，该状态的盘口后续不会有赔率过来 11:锁盘状态
     */
    private Integer status;

    /**
     * 操盘后台设置的位置状态，融合侧不做修改，可为空（操盘后台没有设置位置状态为空）
     */
    private Integer placeNumStatus;
    /**
     * pa状态，赔率合法性校验，最大最小值校验设置的状态，可为空(没有经过赔率合法性校验为空)
     */
    private Integer paStatus;

    /**
     * 收盘状态
     */
    private Integer endEdStatus;

    private String paStatusReason;
    
    private String oddsName;

    /**
     * 子玩法id
     */
    private String childMarketCategoryId;

    /**
     * 三方盘口投注项集合
     */
    private List<ThirdSportMarketOdds> thirdSportMarketOddsList;

    /**
     * 盘口赔率
     */
    private List<MatchMarketOddsVo> oddsFieldsList;

    /**
     * 投注项分组
     */
    private Map<Integer, List<MatchMarketOddsVo>> marketOddsGroupMap;

    /**
     * 行标题
     */
    private TemplateTitle colTitle;

    /**
     * 行标题集合
     */
    private List<TemplateTitle> colTitles;

    /**
     * 水差关联标志，0-不关联，1-关联
     */
    private Integer relevanceType=1;

    private Long setId;

    private Integer diffOddsValue = diffOddsValue();

    @Override
    public int compareTo(Object targetObj) {

        if (targetObj == null) {
            return -99;
        }
        MatchMarketVo target = (MatchMarketVo) targetObj;
        if (CollectionUtils.isEmpty(target.getOddsFieldsList())) {
            return -99;
        }
        if (CollectionUtils.isEmpty(this.getOddsFieldsList())) {
            return 99;
        }
        // 按各投注项赔率差值的绝对值排序
        return this.diffOddsValue() - target.diffOddsValue();
    }

    /**
     * 所有赔率求差值
     *
     * @return
     */
    public Integer diffOddsValue() {
        if (CollectionUtils.isEmpty(this.getOddsFieldsList())) {
            return 0;
        }
        Integer differentValue = 0;
        for (int i = 0; i < this.getOddsFieldsList().size(); i++) {
            Integer oddsValue = NumberUtils.getBigDecimal(this.getOddsFieldsList().get(i).getFieldOddsValue()).setScale(0).intValue();
            if (oddsValue == null) {
                continue;
            }
            if (i > 0) {
                oddsValue = (-1) * oddsValue;
            }
            differentValue += oddsValue;
        }
        differentValue = Math.abs(differentValue);
        return differentValue;
    }

    public Integer getPlaceNum() {
        if (placeNum == null) {
            return 99;
        }
        return placeNum;
    }
}
