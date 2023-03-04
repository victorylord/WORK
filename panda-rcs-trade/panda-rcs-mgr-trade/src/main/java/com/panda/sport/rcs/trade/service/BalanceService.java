package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.BalanceOptionEnum;
import com.panda.sport.rcs.enums.BalanceTypeEnum;
import com.panda.sport.rcs.enums.OddsTypeEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketMarginConfigMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.odds.BalanceReqVo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.MarketBalanceVo;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.vo.statistics.BalanceVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 平衡值
 * @Author : Paca
 * @Date : 2021-02-11 10:51
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class BalanceService {

    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    @Autowired
    private StandardSportMarketService standardSportMarketService;

    @Autowired
    private RcsMatchMarketMarginConfigMapper rcsMatchMarketMarginConfigMapper;

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;

    public MarketBalanceVo queryBalance(BalanceReqVo reqVo) {
        String dateExpect = standardMatchInfoService.getMatchDateExpect(reqVo.getMatchId());
        MarketBalanceVo balanceVo = new MarketBalanceVo(reqVo);
        if (!SportIdEnum.isFootball(reqVo.getSportId())) {
            // 篮球
            getBalance(BalanceTypeEnum.JUMP_ODDS.getType(), dateExpect, reqVo.keySuffix(), reqVo.getBalanceOption(), balanceVo);
            getBalance(BalanceTypeEnum.JUMP_MARKET.getType(), dateExpect, reqVo.keySuffix(), reqVo.getBalanceOption(), balanceVo);
        } else {
            // 足球
            getBalance(BalanceTypeEnum.JUMP_ODDS.getType(), dateExpect, reqVo.keySuffix(), reqVo.getBalanceOption(), balanceVo);
        }
        LambdaQueryWrapper<RcsMatchMarketMarginConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RcsMatchMarketMarginConfig::getMarketId, reqVo.getMarketId());
        RcsMatchMarketMarginConfig marketMarginConfig = rcsMatchMarketMarginConfigMapper.selectOne(wrapper);
        if (marketMarginConfig != null) {
            balanceVo.setHomeMargin(marketMarginConfig.getHomeMargin());
            balanceVo.setAwayMargin(marketMarginConfig.getAwayMargin());
            balanceVo.setTieMargin(marketMarginConfig.getTieMargin());
            balanceVo.setAwayAutoChangeRate(marketMarginConfig.getAwayAutoChangeRate());
        }
        return balanceVo;
    }

    /**
     * 获取平衡值
     *
     * @param balanceType   平衡值类型，1-跳赔平衡值，2-跳盘平衡值
     * @param dateExpect    赛事账务日
     * @param keySuffix     足球-盘口ID，篮球-位置ID
     * @param balanceOption 累计差额计算方式，0-投注额差值，1-投注/赔付混合差值
     * @param balanceVo     平衡值
     */
    private void getBalance(Integer balanceType, String dateExpect, String keySuffix, Integer balanceOption, MarketBalanceVo balanceVo) {
        String jumpKey;
        if (BalanceTypeEnum.isJumpMarket(balanceType)) {
            // 跳盘平衡值
            balanceVo.setJumpMarketBalance(0L);
            // 累计差额计算方式，0-投注额差值，1-投注/赔付混合差值
            if (BalanceOptionEnum.isBet(balanceOption)) {
                jumpKey = RedisKey.getJumpMarketBetKey(dateExpect, keySuffix);
            } else {
                jumpKey = RedisKey.getJumpMarketMixKey(dateExpect, keySuffix);
            }
        } else {
            // 跳赔平衡值
            balanceVo.setBalanceValue(0L);
            // 累计差额计算方式，0-投注额差值，1-投注/赔付混合差值
            if (BalanceOptionEnum.isBet(balanceOption)) {
                jumpKey = RedisKey.getJumpOddsBetKey(dateExpect, keySuffix);
            } else {
                jumpKey = RedisKey.getJumpOddsMixKey(dateExpect, keySuffix);
            }
        }
        Map<String, String> jumpMap = redisUtils.hgetAll(jumpKey);
        log.info("::{}::jumpMap={}", CommonUtil.getRequestId(balanceVo.getMatchId()), JSONObject.toJSONString(jumpMap));
        if (CollectionUtils.isEmpty(jumpMap)) {
            String oddsType = OddsTypeEnum.getHomeOddsTypeByPlayId(balanceVo.getPlayId());
            if (BalanceTypeEnum.isJumpMarket(balanceType)) {
                balanceVo.setJumpMarketOddsType(oddsType);
            } else {
                balanceVo.setCurrentSide(oddsType);
            }
            return;
        }
        BigDecimal jumpTotal = BigDecimal.ZERO;
        BigDecimal jumpMax = BigDecimal.ZERO;
        String oddsType = null;
        for (Map.Entry<String, String> entry : jumpMap.entrySet()) {
            // 投注项累计值
            BigDecimal value = CommonUtils.toBigDecimal(entry.getValue());
            jumpTotal = jumpTotal.add(value);
            if (value.compareTo(jumpMax) > 0) {
                jumpMax = value;
                oddsType = entry.getKey();
            }
        }
        if (StringUtils.isBlank(oddsType)) {
            oddsType = OddsTypeEnum.getHomeOddsTypeByPlayId(balanceVo.getPlayId());
        }
        long balance = jumpMax.subtract(jumpTotal.subtract(jumpMax)).longValue();
        if (BalanceTypeEnum.isJumpMarket(balanceType)) {
            // 跳盘平衡值
            balanceVo.setJumpMarketBalance(balance);
            balanceVo.setJumpMarketOddsType(oddsType);
        } else {
            // 跳赔平衡值
            balanceVo.setBalanceValue(balance);
            balanceVo.setCurrentSide(oddsType);
        }
    }

    public String clearBalance(BalanceReqVo reqVo) {
        log.info("::{}::清除平衡值：BalanceReqVo{}", CommonUtil.getRequestId(reqVo.getMatchId()),JsonFormatUtils.toJson(reqVo));
        String dateExpect = standardMatchInfoService.getMatchDateExpect(reqVo.getMatchId());
        BalanceVo balanceVo = new BalanceVo(reqVo);
        if (BalanceTypeEnum.isJumpMarket(reqVo.getBalanceType())) {
            // 清除跳盘平衡值
            String jumpMarketBetKey = RedisKey.getJumpMarketBetKey(dateExpect, reqVo.keySuffix());
            String jumpMarketMixKey = RedisKey.getJumpMarketMixKey(dateExpect, reqVo.keySuffix());
            redisUtils.del(jumpMarketBetKey);
            redisUtils.del(jumpMarketMixKey);
            balanceVo.setType(BalanceTypeEnum.JUMP_MARKET.getType());
            balanceVo.setJumpMarketBalance(0L);
            balanceVo.setJumpMarketOddsType(OddsTypeEnum.getHomeOddsTypeByPlayId(reqVo.getPlayId()));
        } else {
            // 清除跳赔平衡值
            String jumpOddsBetKey = RedisKey.getJumpOddsBetKey(dateExpect, reqVo.keySuffix());
            String jumpOddsMixKey = RedisKey.getJumpOddsMixKey(dateExpect, reqVo.keySuffix());
            redisUtils.del(jumpOddsBetKey);
            redisUtils.del(jumpOddsMixKey);
            balanceVo.setType(BalanceTypeEnum.JUMP_ODDS.getType());
            balanceVo.setBalanceValue(0L);
            balanceVo.setCurrentSide(OddsTypeEnum.getHomeOddsTypeByPlayId(reqVo.getPlayId()));
        }
        balanceVo.setGlobalId(balanceVo.generateKey());
        // 平衡值推送WS到前端
        producerSendMessageUtils.sendMessage(MqConstants.REALTIME_SYNC_BALANCE_TOPIC, MqConstants.REALTIME_SYNC_BALANCE_TAG, balanceVo.getGlobalId(), balanceVo);
        return balanceVo.generateKey();
    }

    /**
     * 清除玩法下所有平衡值
     *
     * @param balanceType 平衡值类型，1-跳赔平衡值，2-跳盘平衡值
     * @param sportId     赛种ID
     * @param matchId     赛事ID
     * @param playId      玩法ID
     * @param dateExpect  赛事账务日
     */
    public void clearAllBalance(Integer balanceType, Long sportId, Long matchId, Long playId, String dateExpect,String subPlayId) {
        log.info("::{}::清除玩法下所有平衡值：balanceType={},sportId={},matchId={},playId={},dateExpect={}", CommonUtil.getRequestId(matchId),balanceType, sportId, matchId, playId, dateExpect);
        if (StringUtils.isBlank(dateExpect)) {
            dateExpect = standardMatchInfoService.getMatchDateExpect(matchId);
        }
        BalanceVo balanceVo = new BalanceVo();
        balanceVo.setType(balanceType);
        balanceVo.setSportId(sportId);
        balanceVo.setMatchId(matchId);
        balanceVo.setMarketCategoryId(playId);
        if (!SportIdEnum.isFootball(sportId)) {
            // 篮球
            for (int placeNum = 1; placeNum <= RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT; placeNum++) {
                String keySuffix = String.format("%s_%s_%s", matchId, playId, placeNum);
                if (TradeConstant.BASKETBALL_X_PLAYS.contains(playId.intValue()) || (!SportIdEnum.isBasketball(sportId))){
                    keySuffix = String.format("%s_%s_%s_%s", matchId, playId,subPlayId, placeNum);
                }
                String jumpBetKey;
                String jumpMixKey;
                if (BalanceTypeEnum.isJumpMarket(balanceType)) {
                    // 跳盘平衡值
                    jumpBetKey = RedisKey.getJumpMarketBetKey(dateExpect, keySuffix);
                    jumpMixKey = RedisKey.getJumpMarketMixKey(dateExpect, keySuffix);
                } else {
                    // 跳赔平衡值
                    jumpBetKey = RedisKey.getJumpOddsBetKey(dateExpect, keySuffix);
                    jumpMixKey = RedisKey.getJumpOddsMixKey(dateExpect, keySuffix);
                }
                redisUtils.del(jumpBetKey);
                redisUtils.del(jumpMixKey);
            }
            // -1清除所有位置平衡值
            balanceVo.setPlaceNum(-1);
            if (BalanceTypeEnum.isJumpMarket(balanceType)) {
                // 跳盘平衡值
                balanceVo.setJumpMarketBalance(0L);
                balanceVo.setJumpMarketOddsType(OddsTypeEnum.getHomeOddsTypeByPlayId(playId));
            } else {
                // 跳赔平衡值
                balanceVo.setBalanceValue(0L);
                balanceVo.setCurrentSide(OddsTypeEnum.getHomeOddsTypeByPlayId(playId));
            }
            balanceVo.setGlobalId(balanceVo.generateKey());
            // 平衡值推送WS到前端
            producerSendMessageUtils.sendMessage(MqConstants.REALTIME_SYNC_BALANCE_TOPIC, MqConstants.REALTIME_SYNC_BALANCE_TAG, balanceVo.getGlobalId(), balanceVo);
        } else {
            // 足球
            List<Long> marketIdList = standardSportMarketService.getMarketIdList(matchId, playId);
            if (CollectionUtils.isEmpty(marketIdList)) {
                return;
            }
            for (Long marketId : marketIdList) {
                // 跳赔平衡值
                balanceVo.setType(BalanceTypeEnum.JUMP_ODDS.getType());
                balanceVo.setMarketId(marketId);
                balanceVo.setBalanceValue(0L);
                String keySuffix = String.valueOf(marketId);
                String jumpBetKey = RedisKey.getJumpOddsBetKey(dateExpect, keySuffix);
                String jumpMixKey = RedisKey.getJumpOddsMixKey(dateExpect, keySuffix);
                redisUtils.del(jumpBetKey);
                redisUtils.del(jumpMixKey);
                balanceVo.setGlobalId(balanceVo.generateKey());
                // 平衡值推送WS到前端
                producerSendMessageUtils.sendMessage(MqConstants.REALTIME_SYNC_BALANCE_TOPIC, MqConstants.REALTIME_SYNC_BALANCE_TAG, balanceVo.getGlobalId(), balanceVo);
            }
        }
    }

    public void clearAllBalance(Long sportId, Long matchId, Long playId, String dateExpect, String subPlayId) {
        clearAllBalance(BalanceTypeEnum.JUMP_ODDS.getType(), sportId, matchId, playId, dateExpect,subPlayId);
        clearAllBalance(BalanceTypeEnum.JUMP_MARKET.getType(), sportId, matchId, playId, dateExpect,subPlayId);
    }
}
