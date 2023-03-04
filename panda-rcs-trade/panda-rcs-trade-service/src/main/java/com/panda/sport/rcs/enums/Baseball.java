package com.panda.sport.rcs.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 棒球
 * @Author : Paca
 * @Date : 2022-03-08 13:35
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface Baseball {

    @Getter
    @AllArgsConstructor
    enum CategorySet {
        FULL_TIME(30011L, Arrays.asList(247L, 248L, 284L, 285L, 286L), "全场"),
        FIVE_TIME(30012L, Arrays.asList(273L,291L, 292L), "前五局"),
        SECTION_TIME(30013L, Arrays.asList(274L, 277L, 278L, 279L), "区间"),
        SET_ANYTIME(30014L, Arrays.asList(275L, 276L, 280L, 281L, 282L, 283L, 287L, 288L, 289L), "第x局");
        private Long categorySetId;
        private List<Long> categoryIds;
        private String name;

        /**
         * 对应玩法集ID
         *
         * @param categorySetId
         * @return
         */
        public static List<Long> getCategoryIdsBySetId(Long categorySetId) {
            List<Long> categoryIds = new ArrayList();
            for (CategorySet c : CategorySet.values()) {
                if (c.getCategorySetId().equals(categorySetId)) {
                    categoryIds = c.getCategoryIds();
                    return categoryIds;
                }
            }
            categoryIds = CategorySet.SET_ANYTIME.getCategoryIds();
            return categoryIds;
        }

        /**
         * 列数
         *
         * @param categoryId
         * @return
         */
        public static Integer getColNo(Long categoryId) {

            for (Baseball.CategorySet categorySet : Baseball.CategorySet.values()) {
                if (categorySet.getCategoryIds().contains(categoryId)) {
                    return categorySet.getCategoryIds().indexOf(categoryId) + 1;
                }
            }
            return 0;
        }
    }

    /**
     * 获取占位符玩法
     *
     * @return
     */
    static List<Long> getPlaceholderPlayIds() {
        return Lists.newArrayList(275L, 276L, 280L, 281L, 282L, 283L, 287L, 288L, 289L);
    }

    /**
     * 是否是占位符玩法
     *
     * @param playId
     * @return
     */
    static boolean isPlaceholderPlay(Long playId) {
        return getPlaceholderPlayIds().contains(playId);
    }

}
