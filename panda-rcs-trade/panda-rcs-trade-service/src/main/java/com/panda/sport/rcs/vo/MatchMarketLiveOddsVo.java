package com.panda.sport.rcs.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Data;

@Data
@Document(collection = "match_market_live")
public class MatchMarketLiveOddsVo implements java.io.Serializable {

    /*//赛前操盘手
    private String preTrader;
    //滚球操盘手
    private String liveTrader;*/

    private Long matchId;
    /**
     * 标准赛事编码. 用于管理的赛事id
     */
    private String matchManageId;
    /**
     * 比赛开始时间
     */
    private String matchStartTime;

    private String matchStartDate;
    /**
     * 联赛分级。1: 一级联赛；2:二级联赛；3：三级联赛；以此类推；0：未分级
     */
    private Integer tournamentLevel;
    /**
     * 联赛-开售滚球数
     */
    private Long rollNum;
    /**
     * 封盘-0/开盘-1 作用于整个玩法阶段下的所有盘口
     */
    private Integer playPhaseStatus;
    /**
     * 玩法阶段 字典ballPhase
     */
    private Integer playPhaseType;
    /**
     * 赛事状态
     */
    private Integer matchStatus;

    /**
     * 是否为中立场。取值为 0  和1  。  1:是中立场，0:非中立场。操盘人员可手动处理
     */
    private Integer neutralGround;

    /**
     * 联赛名称及编码
     */
    private Long tournamentNameCode;

    /**
     * 比赛开盘标识. 0: 未开盘; 1: 开盘; 2: 关盘; 3: 封盘; 开盘后用户可下注
     */
    private Integer operateMatchStatus;

    /**
     * 赛事是否开放赛前盘. 取值为 1  或  0.  1=开放; 0=不开放
     */
    private Integer preMatchBusiness;

    /**
     * 操盘类型 0:自动操盘 1:手动操盘
     */
    private Integer tradeType;
    /**
     * 赛事收藏状态
     */
    private boolean matchCollectStatus;

    /**
     * 体育种类ID
     */
    private Long sportId;

    /**
     * 当前比分信息
     */
    private String score;

    /**
     * 最近比分队伍
     */
    private String recentScoreTeam;
    

    /**
     * 最近角球比分队伍
     */
    private String recentCornerScoreTeam;


    private List<I18nItemVo> tournamentNames;
    /**
     * 是否支持滚球。取值为 1 或 0 。1=支持；0=不支持
     */
    private Integer liveOddBusiness;

    /**
     * 玩法数量
     */
    private Integer playNum;

    /**
     * 玩法总数量
     */
    private Integer categoryCount;
    /**
     * 球队
     */
    private List<MatchMarketTeamVo> teamList;

    /**
     * 比赛进行时间
     */
    private Integer secondsMatchStart;

    private Long eventTime;

    /**
     * 比赛阶段
     */
    private Integer period;

    /**
     * 标准赛事联赛ID
     */
    private Long standardTournamentId;
    
    /**
     * 操盘平台
     */
    private String riskManagerCode;
    /**
     * 操盘手
     */
    private String traderName;

    /**
     * 操盘手
     */
    private String liveTraderName;

    /**
     * 联赛收藏状态
     */
    private boolean tournamentCollectStatus;
    /**
     * 大单数
     */
    private Long overLimitNum;

    /**
     * 当前联赛延迟用户数量
     */
    private Long delayUserNum;

    /**
     * 盘口数
     */
    private Integer marketCount;

    /**
     * 角球是否展示  0:不展示 1:展示
     */
    private Integer cornerShow;
    /**
     * 角球比分
     */
    private String cornerScore;

    /**
     * 罚牌是否展示  0:不展示 1:展示
     */
    private Integer cardShow;
    /**
     * 罚牌比分
     */
    private String cardScore;
    /**
     * 进入滚球时是否初始化期望值
     */
    private boolean initBetAmount;

    private Integer isDelete;
    //todo 之前开售页面的字段，是否删除
    /**
     * 用于排序字段，联赛名称首字母，主客队首字母
     */
    private String nameConcat;

    /**
     * 玩法
     */
    private List<MatchMarketCategoryVo> marketCategoryList;
    
    /**
     * 接口使用，mongodb不使用
     */
    private List<Integer> allPlayIds;


    @Data
    public static class MatchMarketBaseVo implements Serializable {
        private Map<String, String> names;
        private Long nameCode;

        @Field(value = "id")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
    }

    @Data
    public static class MatchMarketTeamVo extends MatchMarketBaseVo {
        /**
         * 比赛中的作用：主客队或者其他
         */
        private String matchPosition;
        /**
         * 红牌数
         */
        private Integer redCardNum;
    }

    @Data
    public static class MatchMarketVo extends MatchMarketBaseVo implements Comparable {
        /**
         * 盘口赔率
         */
        private List<MatchMarketOddsFieldVo> oddsFieldsList;

        /**
         * 盘口id
         */
        private Long marketCategoryId;
        private Long marketId;
        /**
         * 子玩法id
         */
        private String childMarketCategoryId;
        /**
         * 平衡值
         */
        private Long balance;

        /**
         * 盘口类型. 属于赛前盘或者滚球盘. 1: 赛前盘; 0: 滚球盘.
         */
        private Integer marketType;

        /**
         * 盘口状态0-5. 0:active, 1:suspended, 2:deactivated, 3:settled, 4:cancelled, 5:handedOver
         */
        private Integer status;

        private Integer marketIndex;
        /**
         * 自动 1 手动0
         */
        private Integer marketTradeType;
        /**
         * 自动水差状态 1调整过 0 未调整
         */
        private Integer changeRateStatus;

        /**
         * 三方盘口源状态,风控操盘使用
         */
        private Integer thirdMarketSourceStatus;

        private BigDecimal waterValue;

        private boolean marketActive;

        private Integer diffOdds;
        
        private Integer minOdds;

        private BigDecimal marginValue;
        /**
         * 盘口是否关联 1关联
         */
        private Integer relevanceType;

        private String addition1;

        private String addition2;
        
        private String extraInfo;
        
        private Long updateTime;
        @Override
        public int compareTo(Object targetObj) {

            if (targetObj == null) {
                return -99;
            }
            MatchMarketVo target = (MatchMarketVo) targetObj;
            if (CollectionUtils.isEmpty(target.getOddsFieldsList())) {
                return -99;
            }
            if (CollectionUtils.isEmpty(this.getOddsFieldsList())) {
                return 99;
            }
            // 按各投注项赔率差值的绝对值排序
            return this.diffOddsValue() - target.diffOddsValue();
        }

        /**
         * 所有赔率求差值
         *
         * @return
         */
        public Integer diffOddsValue() {
            if (CollectionUtils.isEmpty(this.getOddsFieldsList())) {
                return 0;
            }
            Integer differentValue = 0;
            for (int i = 0; i < this.getOddsFieldsList().size(); i++) {
                Integer oddsValue = this.getOddsFieldsList().get(i).getFieldOddsOriginValue();
                if (oddsValue == null) {
                    continue;
                }
                if (i > 0) {
                    oddsValue = (-1) * oddsValue;
                }
                differentValue += oddsValue;
            }
            differentValue = Math.abs(differentValue);
            return differentValue;
        }
    }

    @Data
    public static class MatchMarketCategoryVo extends MatchMarketBaseVo {

        /**
         * 玩法类型
         */
        private String type;

        /**
         * 玩法级别状态 数据源
         */
        private Integer playTradeType;

        /**
         * 盘口
         */
        private List<MatchMarketVo> matchMarketVoList;

        /**
         * 玩法阶段 字典ballPhase
         */
        private Integer playPhaseType;

        /**
         * 玩法阶段对应玩法种类
         */
        private Integer rollType;

        /**
         * 玩法類別：1常规进球、2常规角球、3加时进球
         */
        private Integer categoryCon;
    }

    @Data
    public static class MatchMarketOddsFieldVo extends MatchMarketBaseVo {

        /**
         * 投注项名称中包含的表达式的值
         */
        private String nameExpressionValue;

        /**
         * 投注给哪一方：T1主队，T2客队
         */
        private String targetSide;

        /**
         * 赔率显示值
         */
        private String fieldOddsValue;

        /**
         * 赔率原始值
         */
        private transient Integer fieldOddsOriginValue;

        /**
         * 投注项类型
         */
        private String oddsType;

        /**
         * 实货量
         */
        private BigDecimal betAmount;

        /**
         * 盈利期望值
         */
        private BigDecimal profitValue;


        /**
         * 注单数
         */
        private BigDecimal betNum;

        /**
         * 降级后的欧赔数据
         */
        private String nextLevelOddsValue;

        /**
         * 当前投注项是否被激活.1激活; 0未激活(锁盘)
         */
        private Integer active;

        private Integer sortNo = 0;

        private Integer groupId = 0;
    }


}


