package com.panda.sport.rcs.constants;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.constants
 * @Description : mqtopic
 * @Author : Enzo
 * @Date : 2020-07-30 14:20
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface MqConstant {

    /**
     * 前十五分钟赛事盘口更新
     **/
    String MATCH_SNAPSHOT_MARKET_UPDATE_TOPIC = "MATCH_SNAPSHOT_MARKET_UPDATE";

    String MARKET_CONGIG_UPDTAE_SNAPSHOT_TOPIC = "MARKET_CONGIG_UPDTAE_SNAPSHOT";

    String MARKET_ODDS_UPDTAE_SNAPSHOT_TOPIC = "MARKET_ODDS_UPDTAE_SNAPSHOT";

    String MARKET_CONGIG_UPDTAE_TOPIC = "MARKET_CONGIG_UPDTAE_TOPIC";

    /**
     * 推送赔率
     */
    String RCS_TRADE_MATCH_ODDS_CONFIG_TOPIC = "RCS_TRADE_MATCH_ODDS_CONFIG";

    /**
     * 跳盘
     */
    String RCS_TRADE_JUMP_MARKET_TOPIC = "RCS_TRADE_JUMP_MARKET";

    /**
     * 清除篮球水差
     */
    String RCS_TRADE_CLEAR_BASKETBALL_WATER_DIFF_TOPIC = "RCS_TRADE_CLEAR_BASKETBALL_WATER_DIFF";

    String RCS_MATCH_CATEGORYSET_SHOW = "RCS_MATCH_CATEGORYSET_SHOW";

    /**
     * 根据玩家组id修改所有所属玩家的货量百分比配置
     */
    String USER_GROUP_BET_RATE_TOPIC = "USER_GROUP_BET_RATE_TOPIC";

    String TRADE_CATEGORYSET_SHOW = "TRADE_CATEGORYSET_SHOW";

    String RCS_TRADE_PREFIX = "RCS_TRADE_";

    interface Topic {
        /**
         * L联动模式
         */
        String STANDARD_MARKET_ODDS = "STANDARD_MARKET_ODDS";

        /**
         * 玩法自动关盘
         */
        String STANDARD_CATEGORY_AUTOCLOSE = "STANDARD_CATEGORY_AUTOCLOSE";

        /**
         * 切换操盘模式
         */
        String RCS_MARKET_TRADE_TYPE = "RCS_MARKET_TRADE_TYPE";

        /**
         * 修改操盘状态
         */
        String RCS_TRADE_UPDATE_MARKET_STATUS = "RCS_TRADE_UPDATE_MARKET_STATUS";

        /**
         * 滚球状态
         */
        String STANDARD_MATCH_SWITCH_STATUS = "STANDARD_MATCH_SWITCH_STATUS";
    }
}
