package com.panda.sport.rcs.trade.service;

import com.panda.merge.dto.MarketPlaceDtlDTO;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.sport.rcs.pojo.MatchPeriod;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;

import java.util.List;
import java.util.Map;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 操盘状态服务
 * @Author : Paca
 * @Date : 2021-07-03 1:08
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface TradeStatusService {

    /**
     * 修改操盘状态
     *
     * @param updateVO
     * @return
     */
    String updateTradeStatus(MarketStatusUpdateVO updateVO);

    /**
     * 修改玩法下多个子玩法操盘状态
     *
     * @param updateVO
     * @return
     */
    String updateBatchSubPlayTradeStatus(MarketStatusUpdateVO updateVO);

    /**
     * 修改操盘模式封盘，开的盘口变成封，关、封、锁的盘口保留原样
     *
     * @param updateVO
     * @param linkId
     * @return
     */
    List<MarketPlaceDtlDTO> updateTradeModeSeal(MarketStatusUpdateVO updateVO, String linkId);

    /**
     * 早盘切滚球
     *
     * @param matchId
     * @param matchStatusSeal
     */
    void switchLive(Long matchId, boolean matchStatusSeal);

    /**
     * 比分事件触发操盘状态改变
     *
     * @param updateVO
     * @param matchPeriod
     * @return
     */
    String updateTradeStatusEvent(MarketStatusUpdateVO updateVO, MatchPeriod matchPeriod);

    /**
     * 调价窗口修改位置状态
     *
     * @param sportId
     * @param matchId
     * @param playId
     * @param subPlayId
     * @param placeNum
     * @param placeStatus
     */
    void updatePlaceStatus(Long sportId, Long matchId, Long playId, Long subPlayId, Integer placeNum, Integer placeStatus);

    /**
     * 调价窗口推送赔率时获取状态
     *
     * @param sportId
     * @param matchId
     * @param playId
     * @param placeStatus
     * @return
     */
    Integer getStatus(Long sportId, Long matchId, Long playId, Integer placeStatus);

    /**
     * 获取Redis位置状态
     *
     * @param sportId
     * @param matchId
     * @param playId
     * @param subPlayId
     * @return
     */
    Map<Integer, Integer> getPlaceStatusFromRedis(Long sportId, Long matchId, Long playId, Long subPlayId);

    /**
     * 获取Redis位置状态
     *
     * @param sportId
     * @param matchId
     * @param playId
     * @param subPlayId
     * @param placeNum
     * @return
     */
    Integer getPlaceStatusFromRedis(Long sportId, Long matchId, Long playId, Long subPlayId, Integer placeNum);

    /**
     * 获取主玩法状态
     *
     * @param matchId
     * @param playId
     * @return
     */
    Integer getPlaceholderMainPlayStatusFromRedis(Long matchId, Long playId);

    /**
     * 是否联动模式
     *
     * @param matchId
     * @param playId
     * @return
     */
    boolean isLinkage(Long matchId, Long playId);

    /**
     * 获取玩法集编码状态
     *
     * @param sportId
     * @param matchId
     * @param playId
     * @return
     */
    Integer getPlaySetCodeStatus(Long sportId, Long matchId, Long playId);

    /**
     * 获取玩法集编码状态
     *
     * @param matchId
     * @return
     */
    Map<String, Integer> getPlaySetCodeStatus(Long matchId);

    /**
     * 获取玩法集编码下所有玩法状态
     *
     * @param sportId
     * @param matchId
     * @param playIds
     * @return
     */
    Map<Long, Integer> getPlaySetCodeStatus(Long sportId, Long matchId, List<Long> playIds);

    /**
     * 处理推送状态
     *
     * @param sportId         赛种
     * @param matchId         赛事ID
     * @param playId          玩法ID
     * @param marketList      盘口信息
     * @param matchStatus     赛事状态，可为null
     * @param tradeMode       操盘模式，可为null
     * @param sourceCloseFlag 数据源关盘标志，0-否，1-是
     * @param operateSource   操作来源，1-操盘手
     * @param endFlag         收盘标志，0-否，1-是
     */
    void handlePushStatus(Long sportId, Long matchId, Long playId, List<StandardMarketDTO> marketList, Integer matchStatus, Integer tradeMode, Integer sourceCloseFlag, Integer operateSource, Integer endFlag);
}
