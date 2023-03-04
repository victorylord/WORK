package com.panda.sport.rcs.trade.param;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.param
 * @Description :  TODO
 * @Date: 2020-05-23 16:21
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class UpdateTournamentLevelParam {
    /**
     * 联赛id
     */
    private Long id;
    /**
     * 所属层级
     */
    private Integer level;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 时间戳
     */
    private Long timestamp;
    /**
     * 是否热门，1:是 0:否
     */
    private Integer isPopular;
    /**
     * 综合球类接单延迟时间
     */
    private Integer orderDelayTime;
    /**
     * 目标咬度（即目标盈利率）
     */
    private BigDecimal targetProfitRate;

    /**
     * MTS赔接拒率变动范围
     */
    private BigDecimal MtsOddsChangeValue;
    /**
     * 赔率接拒变动开关(1.开 0.关) 默认关
     */
    private Integer oddsChangeStatus;

    /**
     * 操作類別編碼
     */
    private Integer operatePageCode;
    /**
     * 異動前參數
     */
    private UpdateTournamentLevelParam beforeParams;
    /**
     * 体育种类ID
     */
    private Long sportId;
}
