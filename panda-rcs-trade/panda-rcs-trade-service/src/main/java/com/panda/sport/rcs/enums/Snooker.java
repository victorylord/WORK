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
 * @Description : 斯诺克
 * @Author : Paca
 * @Date : 2022-01-24 14:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface Snooker {

    /**
     * 获取占位符玩法
     *
     * @return
     */
    static List<Long> getPlaceholderPlayIds() {
        return Lists.newArrayList(184L, 185L, 186L, 187L, 188L, 189L, 190L, 191L, 192L, 193L, 194L, 195L, 196L, 197L);
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

    @Getter
    @AllArgsConstructor
    enum CategorySet {
        FULL_TIME(70101L, Arrays.asList(1L, 204L), "全场"),
        SET_ONE(70102L, Arrays.asList(188L, 190L, 191L, 192L, 193L, 194L, 195L, 196L, 197L), "第一局");


        private Long categorySetId;
        private List<Long> categoryIds;
        private String name;

        /**
         * 通过玩法Id获取去玩法集Id
         * @param categoryId
         * @return
         */
        public static Snooker.CategorySet getCategorySetId(Long categoryId) {
            for (Snooker.CategorySet categorySet : Snooker.CategorySet.values()) {
                if (categorySet.getCategoryIds().contains(categoryId)) {
                    return categorySet;
                }
            }
            return null;
        }

        /**
         * 对应玩法集ID
         * @param categorySetId
         * @return
         */
        public static List<Long> getCategoryIdsBySetId(Long categorySetId) {
            List<Long> categoryIds = new ArrayList();
            if(categorySetId.equals(70101L)){
                categoryIds = CategorySet.FULL_TIME.getCategoryIds();
            }else {
                categoryIds = CategorySet.SET_ONE.getCategoryIds();
            }
            return categoryIds;
        }

        /**
         * 列数
         * @param categoryId
         * @return
         */
        public static Integer getColNo(Long categoryId) {

            for (Snooker.CategorySet categorySet : Snooker.CategorySet.values()) {
                if (categorySet.getCategoryIds().contains(categoryId)) {
                    return categorySet.getCategoryIds().indexOf(categoryId)+1;
                }
            }
            return 0;
        }

        public static List<Long> getcategorySets(){
            List<Long> result = new ArrayList<>();
            for (Snooker.CategorySet categorySet : Snooker.CategorySet.values()) {
                result.addAll(categorySet.getCategoryIds());
            }
            return result;
        }

    }

}
