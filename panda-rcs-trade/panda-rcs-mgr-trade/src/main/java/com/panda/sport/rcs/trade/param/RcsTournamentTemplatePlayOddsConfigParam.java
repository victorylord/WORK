package com.panda.sport.rcs.trade.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.panda.sport.rcs.pojo.MatchTeamInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RcsTournamentTemplatePlayOddsConfigParam {

    /**
     * 体育种类ID
     */
    private Long sportId;
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
     * 是否主页面1=是，0=否
     */
    @TableField(exist = false)
    private Integer isMainPage;
    /**
     * 玩法赔率源
     */
    private List<PlaysOddsConfig> playOddsConfigs;
    /**
     * 球队
     */
    private List<MatchTeamInfo> teamList;

    @Data
    public static class PlaysOddsConfig {
        /**
         * 玩法赔率源
         */
        private String dataSource;
        /**
         * 配置的玩法
         */
        private List<Long> playIds;
    }

    /**
     * 操作頁面代碼
     */
    private Integer operatePageCode;

    private Map<String,Object> beforeParams;

    /**
     * 赛事管理id
     */
    private String matchManageId;

    /**
     * 操盘手
     */
    private String userName;
}
