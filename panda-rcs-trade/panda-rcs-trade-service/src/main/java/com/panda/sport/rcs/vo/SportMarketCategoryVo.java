package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 查询玩法下配置的盘口类型
 */
@Data
public class SportMarketCategoryVo implements Serializable {

    /**
     * 玩法Id
     */
    private Long id;

    /**
     * 赔率切换
     */
    private List<String> oddsSwitch;
}
