package com.panda.sport.rcs.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 冰球玩法集定义
 *
 * @author gray
 * @version 1.0.0
 * @date 2023/2/10 13:33
 */

public interface IceHockey {
    /**
     * 获取占位符玩法
     *
     * @return
     */
    static List<Long> getPlaceholderPlayIds() {
        return Lists.newArrayList(8L, 9L, 257L, 258L, 261L, 262L, 263L, 264L, 265L, 266L, 267L, 268L, 297L, 298L);
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
        if(Arrays.asList(1L,4L,2L,259L,295L).contains(playId)){
            return true;
        }
        return false;
    }
    
    static boolean isManualBuildMarket(Long playId) {
        if(Arrays.asList(4L, 2L, 295L).contains(playId)){
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
        if(Arrays.asList(4L).contains(playId)){
            return true;
        }
        return false;
    }
    
    /**
     * 三项盘独赢玩法
     * @param playId
     * @return
     */
    static boolean threeItemsWinAlonePlays(Object playId){
        if(playId != null){
            Long id =  Long.parseLong(playId.toString());
            if(Arrays.asList(1L).contains(id)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
    
    /**
     * 是否是独赢玩法
     * @param playId
     * @return
     */
    static boolean capotPlays(Object playId){
        if(playId != null){
            Long id =  Long.parseLong(playId.toString());
            if(Arrays.asList(1L,259L).contains(id)){
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
        if(Arrays.asList(2L,295L).contains(playId)){
            return true;
        }
        return false;
    }
    
    @Getter
    @AllArgsConstructor
    enum CategorySet {
        FULL_TIME(40005L, Arrays.asList(3L, 15L, 14L, 204L, 260L, 8L, 9L, 257L, 258L, 28L, 149L, 6L, 5L, 12L, 41L, 294L, 296L), "全场"),
        SET_ONE(40006L, Arrays.asList(265L, 266L, 267L, 297L, 298L), "第一节"),
        SET_TWO(40007L, Arrays.asList(265L, 266L, 267L, 297L, 298L), "第二节"),
        SET_THREE(40008L, Arrays.asList(265L, 266L, 267L, 297L, 298L), "第三节");
        
        
        private Long categorySetId;
        private List<Long> categoryIds;
        private String name;
        
        /**
         * 通过玩法Id获取去玩法集Id
         * @param categoryId
         * @return
         */
        public static IceHockey.CategorySet getCategorySetId(Long categoryId) {
            for (IceHockey.CategorySet categorySet : IceHockey.CategorySet.values()) {
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
            for (IceHockey.CategorySet categorySet : IceHockey.CategorySet.values()) {
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
            
            for (IceHockey.CategorySet categorySet : IceHockey.CategorySet.values()) {
                if (categorySet.getCategoryIds().contains(categoryId)) {
                    return categorySet.getCategoryIds().indexOf(categoryId)+1;
                }
            }
            return 0;
        }
        
        public static List<Long> getcategorySets(){
            List<Long> result = new ArrayList<>();
            for (IceHockey.CategorySet categorySet : IceHockey.CategorySet.values()) {
                result.addAll(categorySet.getCategoryIds());
            }
            return result;
        }
    }
}
