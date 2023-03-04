package com.panda.sport.rcs.trade.mq.impl;

import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author :  sean
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.mq.impl
 * @Description :  消息的一下常用方法
 * @Date: 2021-01-09 18:33
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class ConsumetUtil {
    @Autowired
    RedisClient redisClient;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;

    private static List<Integer> X_PLAYS = new ArrayList<>();

    @PostConstruct
    public void init(){
        X_PLAYS.addAll(TradeConstant.FOOTBALL_X_NO_INSERT_PLAYS);
        X_PLAYS.addAll(TradeConstant.FOOTBALL_X_INSERT_PLAYS);
        X_PLAYS.addAll(TradeConstant.BASKETBALL_X_PLAYS);
    }

    public static List<Integer> getxPlays() {
        return X_PLAYS;
    }

    /**
     * 清理盘口的平称值
     * @param dateExpect
     * @param market
     * @param oddsType
     * @param sportId
     */
    public void clearBalanceValue(String dateExpect, StandardSportMarket market, Integer clearType, String oddsType, Long sportId) {
        if (null == clearType) {
            clearType = 0;
        }
        //足
        String key = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE, dateExpect, market.getId());
        String keyPlus = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE_PLUS, dateExpect, market.getId());
        String keyjumpbet = String.format("rcs:odds:jumpMarket:%s:%s:bet{%s}", dateExpect, dateExpect, market.getId());
        String keyjumpmix = String.format("rcs:odds:jumpMarket:%s:%s:mix{%s}", dateExpect, dateExpect, market.getId());
        String suffixKey = "{" + market.getId() + "}";
        log.info("::{}::clearBalanceValue-0-key:{},keyPlus{},suffixKey{}","RTRCMMTG_"+market.getStandardMatchInfoId()+"_"+clearType,key,keyPlus,suffixKey);
        if (TradeConstant.FOOTBALL_MOST_ODDS_TYPE_PLAYS.contains(market.getMarketCategoryId().intValue())&&(1==clearType||2==clearType)) {
            log.info("::{}::clearBalanceValue-1-key:{},keyPlus{},suffixKey{}","RTRCMMTG_"+market.getStandardMatchInfoId()+"_"+clearType,key,keyPlus,suffixKey);
            redisClient.hashRemove(key + suffixKey, String.valueOf(oddsType));
            redisClient.hashRemove(keyPlus + suffixKey, String.valueOf(oddsType));
            redisClient.hashRemove(key + ":count" + suffixKey, String.valueOf(oddsType));
            redisClient.hashRemove(key + ":lock" + suffixKey, String.valueOf(oddsType));
        } else {
            log.info("::{}::clearBalanceValue-2-key:{},keyPlus{},suffixKey{}","RTRCMMTG_"+market.getStandardMatchInfoId()+"_"+clearType,key,keyPlus,suffixKey);
            redisClient.delete(key + suffixKey);
            redisClient.delete(keyPlus + suffixKey);
            redisClient.delete(key + ":count" + suffixKey);
            redisClient.delete(key + ":lock" + suffixKey);
        }
        //篮网
        //带x玩法判断
        if(X_PLAYS.contains(market.getMarketCategoryId().intValue())||ClearMatchMarketConsumer.TETC.contains(sportId.intValue())){
            suffixKey = "{" + String.format("%s_%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(),market.getChildMarketCategoryId(), market.getPlaceNum()) + "}";
            key = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE, dateExpect, String.format("%s_%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(),market.getChildMarketCategoryId(), market.getPlaceNum()));
            keyPlus = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE_PLUS, dateExpect, String.format("%s_%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(),market.getChildMarketCategoryId(), market.getPlaceNum()));
            String placeId = String.format("%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(), market.getPlaceNum());
            keyjumpbet=String.format("rcs:odds:jumpMarket:%s:%s:bet{%s}", dateExpect,placeId,placeId);
            keyjumpmix=String.format("rcs:odds:jumpMarket:%s:%s:mix{%s}", dateExpect,placeId,placeId);
        }else{
            suffixKey = "{" + String.format("%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(), market.getPlaceNum()) + "}";
            key = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE, dateExpect, String.format("%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(), market.getPlaceNum()));
            keyPlus = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE_PLUS, dateExpect, String.format("%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(), market.getPlaceNum()));
            String placeId = String.format("%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(), market.getPlaceNum());
            keyjumpbet = String.format("rcs:odds:jumpMarket:%s:%s:bet{%s}", dateExpect,placeId,placeId);
            keyjumpmix = String.format("rcs:odds:jumpMarket:%s:%s:mix{%s}", dateExpect,placeId,placeId);
        }
        if (!(TradeConstant.FOOTBALL_MOST_ODDS_TYPE_PLAYS.contains(market.getMarketCategoryId().intValue())&&(1==clearType||2==clearType))) {
            log.info("::{}::clearBalanceValue-3-key:{},keyPlus{},suffixKey{},keyjumpbet{},keyjumpmix{}","RTRCMMTG_"+market.getStandardMatchInfoId()+"_"+clearType,key,keyPlus,suffixKey,keyjumpbet,keyjumpmix);
            redisClient.delete(key + suffixKey);
            redisClient.delete(keyPlus + suffixKey);
            redisClient.delete(key + ":count" + suffixKey);
            redisClient.delete(key + ":lock" + suffixKey);
            redisClient.delete(keyjumpbet);
            redisClient.delete(keyjumpmix);
        }
    }

    /**
     * 冠军
     * @param dateExpect
     * @param market
     * @param clearType
     * @param oddsType
     */
    public void clearChampionBalanceValue(String dateExpect, StandardSportMarket market, Integer clearType, String oddsType) {
        if (null == clearType) {
            clearType = 0;
        }
        String key = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE, dateExpect, market.getId());
        String keyPlus = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE_PLUS, dateExpect, market.getId());
        String suffixKey = "{" + market.getId() + "}";
        log.info("::{}::clearBalanceValue-0-key:{},keyPlus{},suffixKey{}","RTRCCMG_"+market.getStandardMatchInfoId()+"_"+clearType,key,keyPlus,suffixKey);
        if (1==clearType||2==clearType) {
            log.info("::{}::clearBalanceValue-1-key:{},keyPlus{},suffixKey{}","RTRCCMG_"+market.getStandardMatchInfoId()+"_"+clearType,key,keyPlus,suffixKey);
            redisClient.hashRemove(key + suffixKey, String.valueOf(oddsType));
            redisClient.hashRemove(keyPlus + suffixKey, String.valueOf(oddsType));
            redisClient.hashRemove(key + ":count" + suffixKey, String.valueOf(oddsType));
            redisClient.hashRemove(key + ":lock" + suffixKey, String.valueOf(oddsType));
        } else {
            log.info("::{}::clearBalanceValue-2-key:{},keyPlus{},suffixKey{}","RTRCCMG_"+market.getStandardMatchInfoId()+"_"+clearType,key,keyPlus,suffixKey);
            redisClient.delete(key + suffixKey);
            redisClient.delete(keyPlus + suffixKey);
            redisClient.delete(key + ":count" + suffixKey);
            redisClient.delete(key + ":lock" + suffixKey);
        }
        key = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE, dateExpect, String.format("%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(), market.getPlaceNum()));
        keyPlus = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE_PLUS, dateExpect, String.format("%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(), market.getPlaceNum()));
        suffixKey = "{" + String.format("%s_%s_%s", market.getStandardMatchInfoId(), market.getMarketCategoryId(), market.getPlaceNum()) + "}";
        if (!(1==clearType||2==clearType)) {
            log.info("::{}::clearBalanceValue-3-key:{},keyPlus{},suffixKey{}","RTRCCMG_"+market.getStandardMatchInfoId()+"_"+clearType,key,keyPlus,suffixKey);
            redisClient.delete(key + suffixKey);
            redisClient.delete(keyPlus + suffixKey);
            redisClient.delete(key + ":count" + suffixKey);
            redisClient.delete(key + ":lock" + suffixKey);
        }
    }

    public void setPlaceNum(List<StandardSportMarket> list) {
        if(CollectionUtils.isEmpty(list)){return;}
        List<Long> collect = list.stream().map(e -> e.getId()).collect(Collectors.toList());
        List<StandardSportMarket> standardSportMarkets = standardSportMarketMapper.queryPlaceByMarketIds(collect);
        if(CollectionUtils.isEmpty(standardSportMarkets)){return;}
        for (StandardSportMarket standardSportMarket0 : list) {
            for (StandardSportMarket standardSportMarket1 : standardSportMarkets) {
                if(standardSportMarket0.getId().equals(standardSportMarket1.getId())){
                    standardSportMarket0.setPlaceNum(standardSportMarket1.getPlaceNum());
                }
            }
        }
    }
}
