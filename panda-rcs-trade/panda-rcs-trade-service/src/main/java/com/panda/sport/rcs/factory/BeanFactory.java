package com.panda.sport.rcs.factory;

import com.panda.sport.rcs.enums.MarketStatusEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TraderLevelEnum;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.MatchStatusAndDataSuorceVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.factory
 * @Description : Bean 工厂
 * @Author : Paca
 * @Date : 2020-08-02 10:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public class BeanFactory {

    private static final I18nBean DRAW_I18N = new I18nBean().setZs("平局").setEn("Draw").setJc("平局").setZh("平局");

    private static final I18nBean OTHER_I18N = new I18nBean().setZs("其他").setEn("Other").setJc("其他").setZh("其他");

    private static final I18nBean DRAW_OTHER_I18N = new I18nBean().setZs("平局其他").setEn("DrawOther").setJc("平局其他").setZh("平局其他");

    private static final I18nBean HOME_OTHER_I18N = new I18nBean().setZs("主胜其他").setEn("HomeOther").setJc("主胜其他").setZh("主胜其他");

    private static final I18nBean Away_OTHER_I18N = new I18nBean().setZs("客胜其他").setEn("AwayOther").setJc("客胜其他").setZh("客胜其他");

    private static final I18nBean OVER_I18N = new I18nBean().setZs("大").setEn("Over").setJc("大").setZh("大");

    private static final I18nBean UNDER_I18N = new I18nBean().setZs("小").setEn("Under").setJc("小").setZh("小");

    private static final I18nBean HOME_TEAM = new I18nBean().setZs("主队").setEn("Home team").setJc("主队").setZh("主隊");

    private static final I18nBean AWAY_TEAM = new I18nBean().setZs("客队").setEn("Away team").setJc("客队").setZh("客隊");

    private static final I18nBean NONE_I18N = new I18nBean().setZs("无进球").setEn("None").setJc("无进球").setZh("無進球");
    
    private static final I18nBean DRAW1_I18N = new I18nBean().setZs("进球平局").setEn("Draw").setJc("进球平局").setZh("進球平局");

    private static final I18nBean OWN_GOAL_I18N = new I18nBean().setZs("乌龙球").setEn("own goal").setJc("乌龙球").setZh("烏龍球");

    public static I18nBean getDrawI18n() {
        return DRAW_I18N;
    }

    public static I18nBean getOtherI18n() {
        return OTHER_I18N;
    }
    public static I18nBean getDarwOtherI18n() {
        return DRAW_OTHER_I18N;
    }
    public static I18nBean getHomeOtherI18n() {
        return HOME_OTHER_I18N;
    }
    public static I18nBean getAwayOtherI18n() {
        return Away_OTHER_I18N;
    }

    public static I18nBean getOverI18n() {
        return OVER_I18N;
    }

    public static I18nBean getUnderI18n() {
        return UNDER_I18N;
    }

    public static I18nBean getHomeTeam() {
        return HOME_TEAM;
    }

    public static I18nBean getAwayTeam() {
        return AWAY_TEAM;
    }

    public static I18nBean getNoneI18n() {
        return NONE_I18N;
    }

    public static I18nBean getDraw1I18n() {
        return DRAW1_I18N;
    }

    public static I18nBean getOwnGoalI18n() {
        return OWN_GOAL_I18N;
    }

    @Deprecated
    public static RcsTradeConfig createStatusConfig(Long matchId, Integer tradeLevel, String targetData, Long categoryId, Integer status) {
        RcsTradeConfig config = new RcsTradeConfig()
                .setMatchId(matchId.toString())
                .setTraderLevel(tradeLevel)
                .setTargerData(targetData)
                .setStatus(status)
                .setUpdateUser(String.valueOf(TradeUserUtils.getUserIdNoException()));
        if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            config.setAddition1(categoryId.toString());
        }
        return config;
    }

    /**
     * 玩法操盘方式配置
     *
     * @param matchId    赛事ID
     * @param categoryId 玩法ID
     * @param tradeType  操盘方式
     * @return
     */
    @Deprecated
    public static RcsTradeConfig createPlayTradeTypeConfig(Long matchId, Long categoryId, Integer tradeType) {
        return new RcsTradeConfig()
                .setMatchId(matchId.toString())
                .setTraderLevel(TraderLevelEnum.PLAY.getLevel())
                .setTargerData(categoryId.toString())
                .setDataSource(tradeType)
                .setUpdateUser(String.valueOf(TradeUserUtils.getUserIdNoException()));
    }

    /**
     * 玩法集操盘方式配置
     *
     * @param matchId        赛事ID
     * @param categorySetId  玩法集ID
     * @param categoryIdList 玩法集下所有玩法ID集合
     * @param tradeType      操盘方式
     * @return
     */
    @Deprecated
    public static List<RcsTradeConfig> createPlaySetTradeTypeConfig(Long matchId, Long categorySetId, List<Long> categoryIdList, Integer tradeType) {
        List<RcsTradeConfig> list = new ArrayList<>(categoryIdList.size());
        Integer userId = TradeUserUtils.getUserIdNoException();
        categoryIdList.forEach(categoryId -> {
            RcsTradeConfig config = new RcsTradeConfig()
                    .setMatchId(matchId.toString())
                    .setTraderLevel(TraderLevelEnum.PLAY.getLevel())
                    .setTargerData(categoryId.toString())
                    .setAddition1(categorySetId.toString())
                    .setDataSource(tradeType)
                    .setUpdateUser(String.valueOf(userId));
            list.add(config);
        });
        return list;
    }

    /**
     * 默认赛事状态
     *
     * @return
     */
    public static RcsTradeConfig defaultMatchStatus() {
        return new RcsTradeConfig()
                // 时间顺序
                .setId(0)
                .setStatus(MarketStatusEnum.OPEN.getState());
    }

    /**
     * 默认盘口位置状态
     *
     * @return
     */
    public static RcsTradeConfig defaultMarketPlaceStatus() {
        return new RcsTradeConfig()
                // 时间顺序
                .setId(-1)
                .setStatus(MarketStatusEnum.OPEN.getState());
    }

    /**
     * 默认玩法状态
     *
     * @return
     */
    public static RcsTradeConfig defaultCategoryStatus() {
        return new RcsTradeConfig()
                // 时间顺序
                .setId(-2)
                .setStatus(MarketStatusEnum.OPEN.getState());
    }

    /**
     * 默认玩法集状态
     *
     * @return
     */
    public static RcsTradeConfig defaultCategorySetStatus() {
        return new RcsTradeConfig()
                // 时间顺序
                .setId(-3)
                .setStatus(MarketStatusEnum.OPEN.getState());
    }

    /**
     * 赛事操盘状态WS推送信息
     *
     * @param sportId
     * @param matchId
     * @param status
     * @param linkId
     * @return
     */
    public static MatchStatusAndDataSuorceVo matchTradeStatusWsInfo(Long sportId, Long matchId, Integer status, String linkId) {
        // 1-赛事操盘状态
        return new MatchStatusAndDataSuorceVo(1, sportId, matchId, linkId)
                .setLevel(TradeLevelEnum.MATCH.getLevel())
                .setId(matchId.toString())
                .setStatus(status);
    }

    /**
     * 主玩法操盘状态WS推送信息
     *
     * @param sportId
     * @param matchId
     * @param mainPlayStatusMap
     * @param linkId
     * @return
     */
    public static MatchStatusAndDataSuorceVo mainPlayTradeStatusWsInfo(Long sportId, Long matchId, Map<String, Integer> mainPlayStatusMap, String linkId) {
        // 2-主玩法操盘状态
        return new MatchStatusAndDataSuorceVo(2, sportId, matchId, linkId)
                .setMainPlayStatusMap(mainPlayStatusMap);
    }

    /**
     * 玩法集编码操盘状态WS推送信息
     *
     * @param sportId
     * @param matchId
     * @param playSetCode
     * @param status
     * @param playIds
     * @param linkId
     * @return
     */
    public static MatchStatusAndDataSuorceVo playSetCodeTradeStatusWsInfo(Long sportId, Long matchId, String playSetCode, Integer status, List<Long> playIds, String linkId) {
        // 9-玩法集编码操盘状态
        return new MatchStatusAndDataSuorceVo(9, sportId, matchId, linkId)
                .setLevel(TradeLevelEnum.PLAY_SET_CODE.getLevel())
                .setPlaySetCode(playSetCode)
                .setStatus(status)
                .setCategoryIdList(playIds);
    }

    /**
     * 水差是否关联WS推送信息
     *
     * @param sportId
     * @param matchId
     * @param playId
     * @param subPlayId
     * @param relevanceType
     * @param linkId
     * @return
     */
    public static MatchStatusAndDataSuorceVo relevanceTypeWsInfo(Long sportId, Long matchId, Long playId, Long subPlayId, Integer relevanceType, String linkId) {
        // 101-水差是否关联
        return new MatchStatusAndDataSuorceVo(101, sportId, matchId, linkId)
                .setPlayId(playId)
                .setSubPlayId(subPlayId)
                .setRelevanceType(relevanceType);
    }

    /**
     * 出涨预警标志WS推送信息
     *
     * @param sportId
     * @param matchId
     * @param chuZhangWarnSignMap
     * @param linkId
     * @return
     */
    public static MatchStatusAndDataSuorceVo chuZhangWarnSignWsInfo(Long sportId, Long matchId, Map<String, String> chuZhangWarnSignMap, String linkId) {
        // 121-出涨预警标志
        return new MatchStatusAndDataSuorceVo(121, sportId, matchId, linkId)
                .setChuZhangWarnSignMap(chuZhangWarnSignMap);
    }

}
