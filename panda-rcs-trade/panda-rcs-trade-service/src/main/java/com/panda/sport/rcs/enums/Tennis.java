package com.panda.sport.rcs.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 网球
 * @Author : Paca
 * @Date : 2021-08-31 21:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface Tennis {

    /**
     * 获取占位符玩法
     *
     * @return
     */
    static List<Long> getPlaceholderPlayIds() {
        return Lists.newArrayList(162L, 163L, 164L, 166L, 165L, 208L, 168L, 167L, 170L);
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

    /**
     * 是否包含当前玩法
     * @param playId 玩法ID
     * @return
     */
    static boolean isExistPlay(Long playId) {
        //网球：153-全场独赢（Margin）、154-全场让盘（spread）、155-全场让局（spread）、202、总局数（spread）
        if(Arrays.asList(153L,154L,155L,202L).contains(playId)){
            return true;
        }
        return false;
    }

    static boolean isManualBuildMarket(Long playId) {
        //154-全场让盘（spread）、155-全场让局（spread）、202、总局数（spread）
        if(Arrays.asList(154L,155L,202L).contains(playId)){
            return true;
        }
        return false;
    }

    /**
     * 让分类玩法
     * @param playId
     * @return
     */
    static boolean pointsPlays(Long playId){
        if(Arrays.asList(155L,154L).contains(playId)){
            return true;
        }
        return false;
    }

    /**
     * 是否是独赢玩法
     * @param playId
     * @return
     */
    static boolean capotPlays(Object playId){
        if(playId != null){
            Long id =  Long.parseLong(playId.toString());
            if(Arrays.asList(153L).contains(id)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
    /**
     * 是否是大小玩法
     * @param playId 玩法ID
     * @return
     */
    static boolean BsPlays(Long playId) {
        //202、总局数（spread） 归属于大小玩法
        if(Arrays.asList(202L).contains(playId)){
            return true;
        }
        return false;
    }

    @Getter
    @AllArgsConstructor
    enum CategorySet {
        FULL_TIME(50011L, Arrays.asList(160L, 204L), "全场"),
        SET_ONE(50012L, Arrays.asList(208L, 166L, 167L), "第一盘"),
        SET_TWO(50013L, Arrays.asList(208L, 166L, 167L), "第二盘"),
        SET_THREE(50014L, Arrays.asList(208L, 166L, 167L), "第三盘"),
        SET_FOUR(50015L, Arrays.asList(208L, 166L, 167L), "第四盘"),
        SET_FIVE(50016L, Arrays.asList(208L, 166L, 167L), "第五盘"),
        SPECIAL_TYEP(50017L, Arrays.asList(161L, 159L, 156L, 157L, 158L, 170L, 171L, 169L, 205L, 206L, 207L), "特殊玩法");


        private Long categorySetId;
        private List<Long> categoryIds;
        private String name;

        /**
         * 通过玩法Id获取去玩法集Id
         * @param categoryId
         * @return
         */
        public static Tennis.CategorySet getCategorySetId(Long categoryId) {
            for (Tennis.CategorySet categorySet : Tennis.CategorySet.values()) {
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
            for (Tennis.CategorySet categorySet : Tennis.CategorySet.values()) {
                if(categorySetId>0L){
                    if (categorySet.getCategorySetId().equals(categorySetId)) {
                        categoryIds = categorySet.getCategoryIds();
                    }
                }else {
                    categoryIds.addAll(categorySet.getCategoryIds());
                }
            }
            return categoryIds;
        }

        /**
         * 列数
         * @param categoryId
         * @return
         */
        public static Integer getColNo(Long categoryId) {

            for (Tennis.CategorySet categorySet : Tennis.CategorySet.values()) {
                if (categorySet.getCategoryIds().contains(categoryId)) {
                    return categorySet.getCategoryIds().indexOf(categoryId)+1;
                }
            }
            return 0;
        }

        public static List<Long> getcategorySets(){
            List<Long> result = new ArrayList<>();
            for (Tennis.CategorySet categorySet : Tennis.CategorySet.values()) {
                result.addAll(categorySet.getCategoryIds());
            }
            return result;
        }
    }

}
