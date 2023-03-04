package com.panda.sport.rcs.mongo;

import com.panda.sport.rcs.pojo.danger.RcsDangerTournament;
import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;
import com.panda.sport.rcs.vo.I18nItemVo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :  标准赛事基础信息表
 */
@Data
@Document(collection = "match_market_live")
public class MatchMarketLiveBean {

    @Id
    @Field(value = "id")
    private String id;

    /**
     * 比赛开始时间
     */
    private String matchStartTime;

    private String matchStartDate;
    /**
     * 比赛进行时间
     */
    private Integer secondsMatchStart;

    private Long eventTime;

    private String eventCode;
    /**
     * 比赛阶段
     */
    private Integer period;

    /**
     * 体育种类ID
     */
    private Long sportId;

    /**
     * 标准赛事联赛ID
     */
    private Long standardTournamentId;

    /**
     * 联赛分级。1: 一级联赛；2:二级联赛；3：三级联赛；以此类推；0：未分级
     */
    private Integer tournamentLevel;

    /**
     * 标准赛事编码. 用于管理的赛事id
     */
    private String matchManageId;
    /**
     * 标准联赛ID
     */
    private Long matchId;

    /**
     * 玩法集ID 常规玩法：10001、角球玩法：10002、加时进球：10003
     */
    private Long categorySetId;
    /**
     * 玩法集编码
     */
    private String playSetCode;
    /**
     * 玩法集编码状态
     */
    private Integer playSetCodeStatus;

    /**
     * 玩法集下的所有玩法ID
     */
    private List<Long> categoryIds;

    /**
     * 赛事状态.  比如:未开赛, 滚球, 取消, 延迟等.
     */
    private Integer matchStatus;

    /**
     * 赛前滚球状态
     */
    private Integer oddsLive;

    /**
     * 操盘类型 0:自动操盘 1:手动操盘 2:智能操盘
     */
    private List<Integer> tradeType;

    /**
     * 赛事收藏状态
     */
    private boolean matchCollectStatus;
    /**
     * 联赛收藏状态
     */
    private boolean tournamentCollectStatus;
    /**
     * 操盘状态
     */
    private boolean traderStatus = false;

    /**
     * 比赛开盘标识. 0: 未开盘; 1: 开盘; 2: 关盘; 3: 封盘; 开盘后用户可下注
     */
    private Integer operateMatchStatus;

    /**
     * 赛事是否开放赛前盘. 取值为 1  或  0.  1=开放; 0=不开放
     */
    private Integer preMatchBusiness;

    /**
     * 是否支持滚球。取值为 1 或 0 。1=支持；0=不支持
     */
    private Integer liveOddBusiness;

    /**
     * 是否为中立场。取值为 0  和1  。  1:是中立场，0:非中立场。操盘人员可手动处理
     */
    private Integer neutralGround;

    /**
     * 賽前操盘手
     */
    private String preTraderName;
    /**
     * 滚球操盘手
     */
    private String liveTraderName;

    /**
     * 滚球操盘手id
     */
    private String liveTraderId;

    /**
     * 赛前操盘手id
     */
    private String preTraderId;
    /**
     * 操盘人数
     */
    private Integer traderNum;

    /**
     * 赛前开售时间
     */
    private Long preMatchTime;

    /**
     * 操盘平台 MTS ，PA
     */
    private String riskManagerCode;

    /**
     * 当前比分信息
     */
    private String score;
    /**
     * 红牌比分
     */
    private String redCardScore;

    /**
     * 最近比分队伍
     */
    private String recentScoreTeam;
    /**
     * 最近角球比分队伍
     */
    private String recentCornerScoreTeam;

    /**
     * 显示盘口数
     */
    private Integer marketCount;

    /**
     * 玩法总数量
     */
    private Integer categoryCount;

    /**
     * 自动+ 操盘玩法数量
     */
    private Integer autoAddCount;

    /**
     * 自动操盘玩法数量
     */
    private Integer autoCount;
    /**
     * 手动操盘玩法数量
     */
    private Integer manualCount;

    /**
     * 用于排序字段，联赛名称首字母，主客队首字母
     */
    private String nameConcat;

    /**
     * 显示
     */
    private boolean categorySetShow = false;

    /**
     * fore显示
     */
    private  boolean forecastSetShow = false;

    /**
     * 是否属于前十五分钟快照赛事
     * 1是 0否
     */
    private Integer matchSnapshot;

    private Long sortId;

    /**
     * 联赛名称及编码
     */
    private Long tournamentNameCode;
    /**
     * 置顶时间
     */
    private Long topTime;
    /**
     * 置顶操盘手
     */
    private Long topTraderId;

    /**
     * \
     * 玩法集下对应信息
     */
    private List<MatchCatgorySetVo> setInfos;

    private List<I18nItemVo> tournamentNames;

    /**
     * 球队
     */
    private List<MatchTeamVo> teamList;


    /**
     * 玩法
     */
    private List<MarketCategory> marketCategoryList;

    /**
     * 对应玩法实货量
     */
    private Map<String, List<RcsPredictBetOdds>> betMap;


    private Map<Long, String> dataSourceMap;

    /**
     * 货量出涨预警标志
     */
    private Map<String, String> chuZhangWarnSignMap;

    /**
     * 未读备忘录id列表
     */
    private List<String> memoIds;

    public Integer getTraderNum() {
        if (traderNum == null) {
            return 1;
        }
        return traderNum;
    }


    /**
     * 罚牌比分
     */
    private String cardScore;

    /**
     * 角球比分
     */
    private String cornerScore;
    /**
     * 加时赛比分
     */
    private String extraTimeScore;

    /**
     * 黄牌比分
     */
    private String yellowCardScore;

    /**
     * 赛事类型（默认1）{
     *     1：普通赛事
     *     2：电竞赛事
     *     3：篮球3x3(如果运动类型为篮球）
     * }
     */
    private Integer matchType;

    /**
     * 客户端玩法集展示开关 0关 1开
     */
    private Integer clientShow;

    private Integer roundType;

    /**
     * 局数
     */
    private Integer setNum;

    /**
     * 网球比分
     */
    private List<ScoreVo> scoreVos;
    /**
     * 棒球比分
     */
    private List<BaseBallScoreVo> baseBallScoreVos;

    /**
     * 发球方
     */
    private String servesFirst;

    /**
     * 是否错误完赛事件（普通足球阶段为999才会使用该字段，0:否，1:是）
     */
    private Integer isErrorEndEvent;
    /**
     * 完赛时间
     */
    private Long  endTime;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 赛事级别提前结算开关
     */
    private Integer matchPreStatusRisk;

    /**
     * 第三方赛事信息
     */
    private String thirdMatchListStr;

    private String aoId;

    /**
     * 危险联赛对象
     */
    private RcsDangerTournament dangerTournament;

    /**
     * 是否首次apply
     */
    private boolean isApply = true;
    

    /**
     * 	赛事模板专用（模板复制来源名称）
     */
    private String templateName;


}
