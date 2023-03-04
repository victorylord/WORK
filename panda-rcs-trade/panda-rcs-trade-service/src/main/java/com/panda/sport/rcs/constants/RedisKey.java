package com.panda.sport.rcs.constants;

import java.util.concurrent.TimeUnit;

/**
 * Redis常量
 *
 * @author enzo
 */
public interface RedisKey {

    interface BaseData {
        String PREFIX = "rcs:baseData:";

        /**
         * 玩法多语言，数据结构String，rcs:baseData:playLanguage:赛种:玩法ID
         */
        String PLAY_LANGUAGE = PREFIX + "playLanguage:%s:%s";
        String PLAYAR_LANGUAGE = PREFIX + "playearLanguage:%s";
        String CATEGORY_LANGUAGE = PREFIX + "categoryLanguage:%s:%s";
    }

    interface Config {
        String PREFIX = "rcs:trade:config:";

        /**
         * 出涨封盘开关缓存key<br/>
         * 数据结构：Hash
         */
        static String getChuZhangSwitchKey(Long matchId, Integer matchType) {
            return String.format(PREFIX + "chuZhangSwitch:%s:%s", matchType, matchId);
        }
    }

    interface Second {

        int DAYS_3 = getByDays(3);

        int DAYS_7 = getByDays(7);

        int DAYS_30 = getByDays(30);

        /**
         * 天数转换成秒数
         *
         * @param days
         * @return
         */
        static int getByDays(int days) {
            return (int) TimeUnit.DAYS.toSeconds(days);
        }

        /**
         * 分钟转换成秒数
         *
         * @param minutes
         * @return
         */
        static int getByMinutes(int minutes) {
            return (int) TimeUnit.MINUTES.toSeconds(minutes);
        }
    }

    interface MainMarket {

        /**
         * 获取A+模式主盘口信息缓存 key
         *
         * @param matchId
         * @return
         */
        static String getAutoPlusMainMarketInfoKey(Long matchId) {
            return String.format("rcs:build:mainMarketInfo:autoPlus:%s", matchId);
        }

        /**
         * 获取A+模式主盘口信息缓存 hashKey
         *
         * @param playId
         * @param subPlayId
         * @return
         */
        static String getAutoPlusMainMarketInfoHashKey(Long playId, Long subPlayId) {
            return String.format("%s:%s", playId, subPlayId);
        }

        /**
         * 获取L模式主盘口信息缓存 key
         *
         * @param matchId
         * @return
         */
        static String getLinkageMainMarketInfoKey(Long matchId) {
            return String.format("rcs:build:mainMarketInfo:linkage:%s", matchId);
        }

        /**
         * 获取L模式主盘口信息缓存 hashKey
         *
         * @param playId
         * @param subPlayId
         * @return
         */
        static String getLinkageMainMarketInfoHashKey(Long playId, Long subPlayId) {
            return String.format("%s:%s", playId, subPlayId);
        }

        static String getLinkageDataSourceTimeKey(Long matchId, Long playId, Long subPlayId) {
            return String.format("rcs:build:dataSourceTime:linkage:%s:%s:%s", matchId, playId, subPlayId);
        }

    }

    /**
     * 切换滚球标志
     *
     * @param matchId
     * @return
     */
    static String getSwitchLiveFlagKey(Long matchId) {
        return String.format("rcs:trade:switchLiveFlag{%s}", matchId);
    }

    /**
     * 水差关联状态<br/>
     * 数据结构：Hash<br/>
     * key = rcs:relevanceType:{matchId}:{playId}<br/>
     * field = subPlayId
     *
     * @param matchId
     * @param playId
     * @return
     */
    static String getRelevanceTypeKey(Long matchId, Long playId) {
        return String.format("rcs:relevanceType:%s:%s", matchId, playId);
    }

    long EXPRIY_TIME_7_DAYS = TimeUnit.DAYS.toSeconds(7);

    long EXPRIY_TIME_2_HOURS = TimeUnit.HOURS.toSeconds(2);

    long EXPRIY_TIME_5_MINS = TimeUnit.MINUTES.toSeconds(5);

    /**
     * 玩法投注项模板缓存，配置表 rcs_category_odd_templet
     */
    String RCS_CATEGORY_ODDS_TEMPLATE = "rcs:categoryOddsTemplate:";

    /**
     * 位置状态缓存key
     *
     * @param matchId
     * @param categoryId
     * @return
     */
    static String getMarketPlaceStatusConfigKey(Long matchId, Long categoryId) {
        return String.format("rcs:marketStatusConfig:%s:%s", matchId, categoryId);
    }

    /**
     * 位置状态缓存key
     *
     * @param matchId
     * @param play
     * @return
     */
    static String getPlaceStatusConfigKey(Long matchId, String play) {
        return String.format("rcs:marketStatusConfig:%s:%s", matchId, play);
    }

    /**
     * 占位符主玩法状态缓存key
     *
     * @param matchId
     * @return
     */
    static String getPlaceholderMainPlayStatusKey(Long matchId) {
        return String.format("rcs:tradeStatus:placeholderMainPlay:%s", matchId);
    }

    /**
     * 赛事状态缓存key
     *
     * @param matchId
     * @return
     */
    static String getMatchTradeStatusKey(Long matchId) {
        return String.format("rcs:tradeStatus:match:%s", matchId);
    }

    /**
     * 玩法集编码状态缓存key，暂只适用于足球
     *
     * @param matchId
     * @return
     */
    static String getPlaySetCodeStatusKey(Long matchId) {
        return String.format("rcs:tradeStatus:playSetCode:%s", matchId);
    }

    /**
     * 玩法集编码和玩法关系缓存key
     *
     * @param sportId
     * @return
     */
    static String getPlaySetCodeRelationKey(Long sportId) {
        return String.format("rcs:playSetCode:relation:%s", sportId);
    }

    /**
     * 普通玩法自动关盘状态缓存key
     *
     * @param matchId
     * @return
     */
    static String getAutoCloseStatusKey(Long matchId) {
        return String.format("rcs:tradeStatus:autoClose:%s", matchId);
    }

    /**
     * 操盘模式缓存key
     *
     * @param matchId
     * @return
     */
    static String getTradeModeKey(Long matchId) {
        return String.format("rcs:tradeMode:%s", matchId);
    }

    /**
     * 盘口位置快照状态配置
     */
    String RCS_SNAPSHOT_STATUS_CONFIG = "rcs:snapshotStatusConfig:%s:%s";

    static String getSnapshotStatusConfigKey(Long matchId, Long categoryId) {
        return String.format(RCS_SNAPSHOT_STATUS_CONFIG, matchId, categoryId);
    }

    /**
     * A+模式切换标志
     */
    String AUTO_PLUS_SWITCH_FLAG = "rcs:dataserver:odds:risk:%s:%s";

    /**
     * 联动模式切换标志
     */
    String LINKAGE_SWITCH_FLAG = "rcs:trade:linkageSwitchFlag:%s:%s";

    String RCS_BASKETBALL_TIME = "rcs:basketball:time:%s:%s";
    
    String RCS_ICE_HOCKEY_TIME = "rcs:iceHockey:time:%s:%s";

    String RCS_FOOTBALL_TIME = "rcs:football:time:%s:%s";

    String RCS_TRADER_CHOOSE = "rcs:trader:choose:%s";

    String RCS_PAUSE_ORDER = "rcs:pause:order:%s";

    String MATCH_LIVE_RISK_MANAGERCODE = "rcs:match:live:risk:manageCode:%s";

    /**
     * 跳盘值累计key
     *
     * @param matchId
     * @param playId
     * @param betStage live-滚球，pre-早盘
     * @return
     */
    static String getJumpMarketValueKey(Long matchId, Long playId, String betStage) {
        return String.format("rcs:odds:jumpMarketValue:%s:%s:%s", matchId, playId, betStage);
    }

    /**
     * 跳盘 投注额 累计key
     *
     * @param dateExpect
     * @param keySuffix  足球-盘口ID，篮球-位置ID
     * @return
     */
    static String getJumpMarketBetKey(String dateExpect, String keySuffix) {
        return String.format("rcs:odds:jumpMarket:%s:%s:bet{%s}", dateExpect, keySuffix, keySuffix);
    }

    /**
     * 跳盘 投注/赔付混合 累计key
     *
     * @param dateExpect
     * @param keySuffix  足球-盘口ID，篮球-位置ID
     * @return
     */
    static String getJumpMarketMixKey(String dateExpect, String keySuffix) {
        return String.format("rcs:odds:jumpMarket:%s:%s:mix{%s}", dateExpect, keySuffix, keySuffix);
    }

    /**
     * 跳赔 投注额 累计key
     *
     * @param dateExpect
     * @param keySuffix  足球-盘口ID，篮球-位置ID
     * @return
     */
    static String getJumpOddsBetKey(String dateExpect, String keySuffix) {
        return String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE + "{%s}", dateExpect, keySuffix, keySuffix);
    }

    /**
     * 跳赔 投注/赔付混合 累计key
     *
     * @param dateExpect
     * @param keySuffix  足球-盘口ID，篮球-位置ID
     * @return
     */
    static String getJumpOddsMixKey(String dateExpect, String keySuffix) {
        return String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE_PLUS + "{%s}", dateExpect, keySuffix, keySuffix);
    }

    String FREE_ORDER = "rcs:order:second:config:%s:%s";

    Long FREE_ORDER_TIME = 5L;

    Long FREE_ORDER_UPDATE_TIME = 5000L;

    String ORDER_SECOND_TRADER = "rcs:order:second:trader:%s:%s";

    static String getLinkageModeSignKey(Long matchId, Long playId) {
        return String.format("rcs:trade:odds:linkageMode:sign:%s:%s", matchId, playId);
    }

//    static String getLinkageModeMarketKey(Long matchId, Long playId) {
//        return String.format("rcs:trade:odds:linkageMode:market:%s:%s", matchId, playId);
//    }

    String RCS_TASK_MATCH_ALL_SCORE = "rcs:task:match:all:score:%s:%s";

    static String getFootballScore(Long matchId, Long categorySetId) {
        return String.format(RCS_TASK_MATCH_ALL_SCORE, matchId, categorySetId);
    }

    /**
     * 综合操盘，联赛模板跳分设置，最大投注最大赔付
     */
    String TEMPLATE_TOURNAMENT_AMOUNT = "rcs:template:tournament:";

    /**
     * 早盘/滚球标志 key
     */
    String ODDS_MATCH_TYPE_KEY = "rcs:trade:STANDARD_MARKET_ODDS:matchType:%s";
    /**
     * 赛事百家赔数据源权重
     */
    String REDIS_KEY_MULTI_ODDS_WEIGHT_VALUE = "rcs:tournament:template:baijia:config:matchId:%s:matchType:%s";


    String REDIS_MTS_CONTACT_CONFIG_KEY = "rcs:redis:mts:contact:config:matchId:%s:matchType:%s";
    /**用模板ID的玩法集id**/
    String REDIS_ACCEPT_CONFIG_EVENT_KEY = "redis:accept:config:event:key:template:%s:category:%s";

    String MATCH_EVENT_REDIS_KEY = "rcs:event:config:match:%s:categorySetId:%s";
    String REDIS_EVENT_CONFIG= "rcs:accept:event:matchId:%s:eventCode:%s";
    /**
     * 出涨预警标志缓存key<br/>
     * 数据结构：Hash
     */
    static String getChuZhangWarnSignKey(Long matchId, Integer matchType) {
        return String.format("rcs:trade:chuZhangWarnSign:%s:%s", matchType, matchId);
    }

    /**
     * kir-1788 redisKey
     */
    String RCS_TOURNAMENT_TEMPLATE_ACCEPT_AUTO_CHANGE_KEY = "rcs:tournament:template:accept:auto:change:matchId:%s:categorySetId:%s";
    static String getRcsTournamentTemplateAcceptAutoChangeKey(Long matchId, Long categorySetId) {
        return String.format(RCS_TOURNAMENT_TEMPLATE_ACCEPT_AUTO_CHANGE_KEY, matchId, categorySetId);
    }
    String DATA_LIVE_MATCH_CONFIG_DATASOURCE = "rcs:match:data:config:dataSource:matchId:%s:categorySetId:%s";
    static String getDataLiveMatchConfigDatasource(Long matchId, Long categorySetId) {
        return String.format(DATA_LIVE_MATCH_CONFIG_DATASOURCE, matchId, categorySetId);
    }
    String DATA_LIVE_MATCH_SETTLE_DATASOURCE = "rcs:match:data:settle:dataSource:matchId:%s:categorySetId:%s";
    static String getDataLiveMatchSettleDatasource(Long matchId, Long categorySetId) {
        return String.format(DATA_LIVE_MATCH_SETTLE_DATASOURCE, matchId, categorySetId);
    }



}
