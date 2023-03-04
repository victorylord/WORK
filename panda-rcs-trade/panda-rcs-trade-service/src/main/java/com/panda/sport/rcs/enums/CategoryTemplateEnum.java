package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.enums
 * @Description : 玩法模板枚举
 * @Author : Paca
 * @Date : 2020-07-30 14:13
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum CategoryTemplateEnum {
    /**
     * 通用模板
     */
    NO_0(0),

    /**
     * 单盘口 & 3个投注项
     */
    NO_1(1),

    /**
     * 多盘口 & 2个投注项
     */
    NO_2(2),

    /**
     * 单盘口 & 2个投注项
     */
    NO_3(3),

    /**
     * 单盘口 & 分主客
     */
    NO_4(4),

    /**
     * 多重条件 & 多个投注项
     */
    NO_5(5),

    /**
     * 单盘口 & 分主客
     */
    NO_6(6),

    /**
     * 多盘口 & 3个投注项
     */
    NO_7(7),

    /**
     * 多重条件 & 多个投注项
     */
    NO_8(8),

    /**
     * 足球比分
     */
    NO_9(9),

    /**
     * 单盘口 & 每行三列多个投注项
     */
    NO_10(10);

    /**
     * 玩法ID
     */
    private Integer id;

    /**
     * 是否按列分组
     *
     * @param templateId 模板ID
     * @return
     */
    public static boolean isGroupByColumn(Integer templateId) {
        return NO_4.getId().equals(templateId) ||
                NO_6.getId().equals(templateId) ||
                NO_9.getId().equals(templateId);
    }

    /**
     * 净胜分玩法配置模板5，需特殊处理
     *
     * @param categoryId 玩法ID
     * @param templateId 模板ID
     * @return
     */
    public static boolean isIrregular(Long categoryId, Integer templateId) {
        return MarketCategoryEnum.isWinningMargin(categoryId) &&
                NO_5.getId().equals(templateId);
    }

    /**
     * 单盘口按列和行分组
     *
     * @param categoryId 玩法ID
     * @param templateId 模板ID
     * @return
     */
    public static boolean isSingleGroupByColAndRow(Long categoryId, Integer templateId) {
        return isIrregular(categoryId, templateId) || NO_8.getId().equals(templateId);
    }

    /**
     * 多盘口按列和行分组
     *
     * @param categoryId 玩法ID
     * @param templateId 模板ID
     * @return
     */
    public static boolean isMultiGroupByColAndRow(Long categoryId, Integer templateId) {
        return !MarketCategoryEnum.isWinningMargin(categoryId) &&
                NO_5.getId().equals(templateId);
    }

    /*public static boolean isOddsNameCleanTotal(Long categoryId, Integer templateId) {
        return NO_5.getId().equals(templateId) &&
                (MarketCategoryEnum.isTotal(categoryId) || MarketCategoryEnum.isTeamTotal(categoryId));
    }

    public static boolean isOddsNameReplaceTotal(Long categoryId, Integer templateId) {
        return !NO_5.getId().equals(templateId) &&
                (MarketCategoryEnum.isTotal(categoryId) ||
                        MarketCategoryEnum.isTeamTotal(categoryId) ||
                        MarketCategoryEnum.isOddsNameReplaceTotal(categoryId));
    }*/
}
