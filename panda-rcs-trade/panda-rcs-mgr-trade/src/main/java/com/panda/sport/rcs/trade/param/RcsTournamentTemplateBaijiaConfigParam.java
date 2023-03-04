package com.panda.sport.rcs.trade.param;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class RcsTournamentTemplateBaijiaConfigParam {
    /**
     * 赛事id
     */
    private Long matchId;
    /**
     * 盘口类型 1：早盘；0：滚球
     */
    private Long matchType;
    /**
     * 模板id
     */
    private Long templateId;
    /**
     * 模板名稱
     */
    private String templateName;
    /**
     * sportId
     */
    private Integer sportId;
    /**
     * 警示值
     */
    private BigDecimal cautionValue;
    /**
     * 玩法赔率源
     */
    private List<BaijiaConfig> baijiaConfigs;
    /**
     * 操作頁面代碼
     */
    private Integer operatePageCode;

    private RcsTournamentTemplateBaijiaConfigParam beforeParams;
    /**
     * 赛事管理id
     */
    private String matchManageId;

    @Data
    public static class BaijiaConfig {
        /**
         * 参考网名称
         */
        private String name;
        /**
         * 权重值
         */
        private Long value;
        /**
         * 是否勾选（0.否 1.是）
         */
        private Integer status;
    }
}
