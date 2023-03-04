package com.panda.sport.rcs.pojo.dto.odds;

import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class MatchPlayConfig {

    private String playId;

    /**
     * 赔率类型
     * MY 马来  ， EU：欧赔
     */
    private String marketType;

    /**
     * 盘口差
     */
    private String marketHeadGap;

    /**
     * 位置配置
     */
    private List<MatchMarketPlaceConfig> placeConfig;

    /**
     * 当前玩法对应的盘口赔率
     */
    private List<RcsStandardMarketDTO> marketList;

    /**
     * 玩法级别开关封锁
     */
    private Integer status;

    /**
     * 玩法级别操盘状态切换
     */
    private Integer tradeStatus;

    /**
     * 是否自动关盘，0-否，1-是
     */
    private Integer autoCloseFlag;

    /**
     * 数据源关盘标志，0-否，1-是
     */
    private Integer sourceCloseFlag;

    /**
     * 分时模板配置
     */
    private RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargain;

    /**
     * 主要玩法让分、大小，位置spread
     */
    private Map<Integer, BigDecimal> placeSpreadMap;
}
