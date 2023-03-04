package com.panda.sport.rcs.vo.secondary;

import com.panda.sport.rcs.mongo.MarketCategory;
import com.panda.sport.rcs.mongo.MatchTeamVo;
import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 足球两项盘玩法集信息
 * @Author : Paca
 * @Date : 2021-02-19 11:57
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@NoArgsConstructor
@Accessors
public class FootballTwoPlaySetInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 玩法集ID
     */
    private Long playSetId;
    /**
     * 玩法集编码状态
     */
    private Map<Long, Integer> playSetCodeStatusMap;
    /**
     * 比分
     */
    private String score;
    /**
     * 红牌
     */
    private String score1;

    /**
     * 黄牌
     */
    private String score2;

    /**
     * 客户端玩法集展示开关 0关 1开
     */
    private Integer clientShow;

    /**
     * 球队
     */
    private List<MatchTeamVo> teamList;

    /**
     * 玩法ID集合
     */
    private List<Long> playIds;

    /**
     * 玩法信息集合
     */
    private List<MarketCategory> playInfoList;

    /**
     * 盘口货量
     */
    private Map<String, List<RcsPredictBetOdds>> betMap;

    private Map<Long, String> dataSourceMap;
}
