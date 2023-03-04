package com.panda.sport.rcs.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 赛事盘口赔率查询件VO
 */

@Data
public class MarketLiveOddsQueryVo extends PageQuery {
    /**
     * 赔率类型:欧洲盘：OU，香港盘：HK
     */
    private String marketOddsKind;
    /**
     * 查询日期(前端传入的字符串，格式:yyyy-MM-dd)
     */
    private String matchDate;
    /**
     * 比赛开始时间
     */
    private Date beginTime;

    /**
     * 赛事收藏状态
     */
    private boolean matchCollectStatus;

    /**
     * liveOddBu
     * 是否支持滚球:1=支持；0=不支持(变更为是否开滚球，对应siness字段)
     */
    private Integer liveOddBusiness;

    /**
     * 标准联赛 id. 对应联赛 id  对应  standard_sport_tournament.id
     */
    private Long standardTournamentId;

    private List<Long> tournamentIds;
    /**
     * 联赛名称
     */
    private String tournamentName;
    /**
     * 比赛ID
     */
    private Long matchId;
    /**
     * 联赛级别
     */
    private Integer tournamentLevel;
    /**
     * 比赛开盘标识: 0: 未开盘; 1: 开盘; 2: 关盘; 3: 封盘; 开盘后用户可下注
     * <p>
     * 滚球-- 盘口状态 1 开放中 0 关闭中
     */
    private Integer operateMatchStatus;

    /**
     * 盘口状态 1 开放中 0 关闭中
     */
    private Integer marketStatus;

    /**
     * 操盘类型 0:自动操盘 1:手动操盘 2：A+操盘 null 表示不修改当前操盘类型
     */
    private Integer tradeType;

    /**
     * 标准赛事编码. 用于管理的赛事id
     */
    private String matchManageId;

    /**
     * 体育类型ID
     */
    private Long sportId;

    /**
     * 比赛开始时间utc
     */
    private Long beginTimeMillis;

    /**
     * 比赛结束时间utc
     */
    private Long endTimeMillis;

    /**
     * 现在时间
     */
    private Long currentTimeMillis;

    /**
     * 更新时间
     */
    private Long updateTimeMillis;

    /**
     * 是否其它早盘 1：其它早盘
     */
    private Integer isOtherEarly;

    /**
     * 操盘手ID
     */
    private Long tradeId;

    private Long marketCategoryId;
    /**
     * 玩法集ID
     */
    private Long categorySetId;
    /**
     * 1 所有自选 2 仅自己操盘 3 仅自己收藏 4 所有赛事
     */
    private Integer chooseType;
    /**
     * 入参玩法条件
     */
    private List<Long> marketCategoryIds;

    private boolean categorySetShow;
    /**
     * 赛种IDS
     */
    private List<Long> sportIds;
    /**
     * 联赛查询1 操盘列表  2 及时注单
     */
    private Integer tournamentType;
    /**
     * 1联赛排序 2时间排序
     */
    private Integer sortType;

    /**
     * 1 get 2 update
     */
    private Integer urlType;
    /**
     * 1纯投注额 2纯赔付额 3混合型
     */
    private Integer betAmountType;
    /**
     * 单关/串关 1单关货量 2串关货量
     */
    private Integer seriesType;
    /**
     * 子玩法ID
     */
    private String subPlayId;
}
