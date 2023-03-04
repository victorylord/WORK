package com.panda.sport.rcs.enums;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 排球
 * @Author : Paca
 * @Date : 2021-12-20 16:52
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface Volleyball {

    /**
     * 获取占位符玩法
     *
     * @return
     */
    static List<Long> getPlaceholderPlayIds() {
        return Lists.newArrayList(162L, 253L, 254L, 255L, 256L);
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
