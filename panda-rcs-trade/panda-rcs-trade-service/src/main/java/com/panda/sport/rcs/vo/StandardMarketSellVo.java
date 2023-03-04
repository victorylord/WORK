package com.panda.sport.rcs.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.panda.sport.rcs.mongo.MarketCategory;
import lombok.Data;

import java.util.List;

/**
 * 操盘手确认开售页面,数据返回对象
 *
 * @author carver
 */
@Data
public class StandardMarketSellVo {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 体育种类ID
     */
    private Long sportId;

    /**
     * 标准赛事ID
     */
    private Long matchInfoId;

    /**
     * 联赛中文名
     */
    private String tournamentNameCn;

    /**
     * 联赛英文名
     */
    private String tournamentNameEn;

    /**
     * 开赛时间
     */
    private Long beginTime;

    /**
     * 赛事状态
     */
    private Integer matchStatus;

    /**
     * 主队中文名
     */
    private String teamHomeNameCn;

    /**
     * 主队英文名
     */
    private String teamHomeNameEn;

    /**
     * 比分
     */
    private String score;

    /**
     * 客队中文名
     */
    private String teamAwayNameCn;

    /**
     * 客队英文名
     */
    private String teamAwayNameEn;

    /**
     * 中立场
     */
    private String neutralGround;

    /**
     * 赛事管理ID
     */
    private String matchManageId;

    /**
     * 是否支持滚球。取值为 1 或 0 。1=支持；0=不支持
     */
    private Integer liveOddBusiness;


    /**
     * 赛前操盘手
     */
    private String preTrader;

    /**
     * 赛前开售状态
     */
    private String preMatchSellStatus;

    /**
     * 滚球操盘手
     */
    private String liveTrader;

    /**
     * 滚球开售状态
     */
    private String liveMatchSellStatus;

    /**
     * 赛前开售时间
     */
    private Long preMatchTime;

    /**
     * 滚球开售时间
     */
    private Long liveOddTime;

    /**
     * 赛前到时间未确认开售，是否警告  0:否  1:是
     */
    private Integer preIsWarn = 0;

    /**
     * 滚球到时间未确认开售，是否警告  0:否  1:是
     */
    private Integer liveIsWarn = 0;

    /**
     * 盘口数
     */
    private Integer marketCount;

    /**
     * 角球是否展示  0:不展示 1:展示
     */
    private Integer cornerShow;

    /**
     * 罚牌是否展示  0:不展示 1:展示
     */
    private Integer cardShow;

    /**
     * 历史赛程  0:不是  1:是
     */
    private Integer historyFlag = 0;

    /**
     * 赛事阶段id
     */
    private Integer matchPeriodId = 0;

    /**
     * 数据源
     */
    private String dataSourceCode;

    /**
     * 赛前赔率数据服务商
     */
    private String preMatchDataProviderCode;

    /**
     * 滚球赔率数据服务商
     */
    private String liveMatchDataProviderCode;

    /**
     * 玩法
     */
    private List<CategorySellVo> categorySellVos;

}
