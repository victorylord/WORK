package com.panda.sport.rcs.enums;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 羽毛球
 * @Author : Paca
 * @Date : 2022-05-04 14:59
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface Badminton {

    /**
     * 获取占位符玩法
     *
     * @return
     */
    static List<Long> getPlaceholderPlayIds() {
        return Lists.newArrayList(175L, 176L, 177L, 178L, 179L, 203L);
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
