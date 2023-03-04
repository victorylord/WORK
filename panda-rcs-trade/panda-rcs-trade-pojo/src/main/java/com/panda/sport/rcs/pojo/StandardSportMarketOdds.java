package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <p>
 * 赛事盘口投注项表
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
public class StandardSportMarketOdds extends RcsBaseEntity<StandardSportMarketOdds> {
    /**
     * 表ID, 自增
     */
//      @TableId(value = "id", type = IdType.AUTO)  禁用自增长，采用标准盘口投注项ID作为主键
    private Long id;

    /**
     * 盘口ID  standard_sport_market.id
     */
    private Long marketId;

    /**
     * 标准投注项模板id   standard_sport_odds_fields_templet.id
     */
    private Long oddsFieldsTempletId;

    /**
     * 投注项结算结果文本
     */
    private String settlementResultText;

    /**
     * 投注项结算结果文本
     */
    private String settlementResult;

    /**
     * 赛果已确认: Confirmed, 盘中事件确认: LiveScouted, 未知: Unknown
     */
    private String betSettlementCertainty;

    /**
     * 投注项类型
     */
    private String oddsType;

    /**
     * 让球 false/受让  true
     */
    @TableField(exist = false)
    private Boolean passive;

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
     * 投注项名称编码
     */
    private Long nameCode;

    /**
     * 投注项名称.
     */
    private String name;

    /**
     * 投注项名称中包含的表达式的值
     */
    private String nameExpressionValue;

    /**
     * 投注项赔率. 单位: 0.00001
     */
    private Integer oddsValue;

    /**
     * 前一次投注项赔率. 单位: 0.0001
     */
    private Integer previousOddsValue;

    /**
     * 三方源投注项模板id
     */
    private String thirdTemplateSourceId;

    /**
     * 投注项原始赔率(第三方未调整水位的赔率). 单位: 0.0001
     */
    private Integer originalOddsValue;

    /**
     * 投注给哪一方: T1主队, T2客队,
     */
    private String targetSide;

    /**
     * 用于排序, 大于1, 越小越靠前
     */
    private Integer orderOdds;

    /**
     * 取值:  SR BC分别代表: SportRadar、FeedConstruc. 详情见data_source
     */
    private String dataSourceCode;

    /**
     * 该字段用于做风控时, 需要替换成风控服务商提供的投注项id.  如果数据源发生切换, 当前字段需要更新.
     */
    private String thirdOddsFieldSourceId;

    /**
     * 是否需要人工确认开奖. 1 需要;  0 不需要
     */
    private Integer managerConfirmPrize;

    private String remark;

    private Long createTime;

    private Long modifyTime;
    /**
     * 当前投注项是否被激活.1激活; 0未激活(锁盘)
     */
    private Integer active;

    /**
     * 投注项名称多语言数组json串。交易项可能有也可能没有该字段。需要的时候填入
     */
    private String i18nNames;

    /**
     * 扩展参数：用于上游需通过融合向下游透传的参数，融合不做任何处理及存储
     */
    private String extraInfo;
    /**
     * margin值
     */
    private BigDecimal margin;
    /**
     * 水差
     */
    private BigDecimal marketDiffValue;
    /**
     * 概率差
     */
    private BigDecimal probability;
    /**
     * 概率赔率
     */
    private BigDecimal probabilityOdds;
    /**
     * 描点：0(否),1(是)
     */
    private Integer anchor;

    /**
     * margin概率赔率
     */
    private BigDecimal marginProbabilityOdds;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StandardSportMarketOdds that = (StandardSportMarketOdds) o;
        return Objects.equals(id, that.id) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
