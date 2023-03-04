package com.panda.sport.rcs.trade.util;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.enums.DataSourceTypeEnum;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author :  Sean
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.util
 * @Description :  TODO
 * @Date: 2020-08-08 15:13
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public class MarginUtils {

    public static boolean convert(List<Map<String, Object>> oddsList,
                                  Long dataSource, String marketKindEnumString, BigDecimal margin,Long play) {
        BigDecimal calcedMargin = new BigDecimal(0);
        BigDecimal sumValue = new BigDecimal(0);
        MarketKindEnum marketKindEnum = MarketKindEnum.getMarketKindByValue(marketKindEnumString);
        switch (marketKindEnum) {
            //欧赔 独赢盘
            case Europe:
                //自动操盘

//                if(dataSource == DataSourceTypeEnum.AUTOMATIC.getValue().longValue()){
////                    return Boolean.TRUE;
//                    for (Map<String, Object> map : oddsList) {
//                        BigDecimal fieldOddsValue = new BigDecimal(map.get("margin").toString());
//                        sumValue = sumValue.add(fieldOddsValue);
//                    }
//                    return sumValue.compareTo(margin.multiply(new BigDecimal(oddsList.size()))) == 0 ? Boolean.TRUE:Boolean.FALSE;
//                }else{
                    if (!TradeConstant.BASKETBALL_MAIN_EU_PLAYS.contains(play.intValue())){
                        return true;
                    }
                    calcedMargin = convert(oddsList, marketKindEnumString,margin);
                    // margin 玩法优化
                    if (TradeConstant.BASKETBALL_MAIN_EU_PLAYS.contains(play.intValue()) || TradeConstant.FOOTBALL_MAIN_EU_PLAYS.contains(play.intValue())){
                        BigDecimal upMargin = calcedMargin.setScale(0,BigDecimal.ROUND_HALF_UP);
                        BigDecimal downMargin = calcedMargin.setScale(0,BigDecimal.ROUND_HALF_UP);
                        return (upMargin.compareTo(margin.add(new BigDecimal(2))) == 1 || downMargin.compareTo(margin.subtract(new BigDecimal(2))) == -1) ? Boolean.FALSE:Boolean.TRUE;
                    }else {
                        BigDecimal upMargin = calcedMargin.setScale(0,BigDecimal.ROUND_UP);
                        BigDecimal downMargin = calcedMargin.setScale(0,BigDecimal.ROUND_DOWN);
                        return (upMargin.compareTo(margin) == 0 || downMargin.compareTo(margin) == 0) ? Boolean.TRUE:Boolean.FALSE;
                    }
//                }
                //马培 两项盘 如果odds1+odds2>=1 margin=2-(odds1+odds2)  如果odds1+odds2<1 margin= -(odds1+odds2)
            case Malaysia:
                //自动操盘 不需要校验
                if(dataSource == DataSourceTypeEnum.AUTOMATIC.getValue().longValue()){
                    return Boolean.TRUE;
                }
                for (Map<String, Object> map : oddsList) {
                    BigDecimal fieldOddsValue = new BigDecimal(map.get("fieldOddsValue").toString());
//                    fieldOddsValue = fieldOddsValue.
//                            divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), 2, BigDecimal.ROUND_HALF_UP);
                    sumValue = sumValue.add(fieldOddsValue);
                }
                if(sumValue.compareTo(new BigDecimal(0))>0){
                    sumValue = (new BigDecimal(2)).subtract(sumValue);
                }else if (sumValue.compareTo(new BigDecimal(0)) < 0){
                    sumValue = sumValue.multiply(new BigDecimal(-1));
                }
                return margin.compareTo(sumValue) == 0;
            default:
                return false;
        }
    }

    public static void main(String[] args) {
        BigDecimal calcedMargin = new BigDecimal(113.96);
        BigDecimal upMargin = calcedMargin.setScale(0,BigDecimal.ROUND_HALF_UP);
        BigDecimal downMargin = calcedMargin.setScale(0,BigDecimal.ROUND_HALF_UP);
        System.out.println(upMargin+"--->"+downMargin);
    }

    public static BigDecimal convert(List<Map<String, Object>> oddsList, String marketKindEnumString,BigDecimal margin) {
        BigDecimal margin1 = new BigDecimal(0);
        BigDecimal bigDecimal = new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE);
        MarketKindEnum marketKindEnum = MarketKindEnum.getMarketKindByValue(marketKindEnumString);
        switch (marketKindEnum) {
            case Europe:
                for (Map<String, Object> map : oddsList) {
                    BigDecimal fieldOddsValue = new BigDecimal(map.get("fieldOddsValue").toString());
                    if (fieldOddsValue.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal divide = bigDecimal.divide(fieldOddsValue, 2, BigDecimal.ROUND_HALF_UP);
                        margin1 = margin1.add(divide);
                    }else if (ObjectUtils.isNotEmpty(margin)){
                        return margin;
                    }else {
                        margin1 = margin1.add(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE));
                    }
                }
                return margin1;
            case Malaysia:
                BigDecimal fieldOddsValueNum = new BigDecimal(0);
                for (Map<String, Object> map : oddsList) {
                    BigDecimal fieldOddsValue = new BigDecimal(map.get("fieldOddsValue").toString());
                    fieldOddsValueNum = fieldOddsValueNum.add(fieldOddsValue);
                }
                //小于0
                if (fieldOddsValueNum.compareTo(new BigDecimal(0)) > 0) {
                    fieldOddsValueNum = new BigDecimal("2").subtract(fieldOddsValueNum).setScale(2, BigDecimal.ROUND_DOWN);
                } else if (fieldOddsValueNum.compareTo(new BigDecimal(0)) < 0){
                    fieldOddsValueNum = fieldOddsValueNum.multiply(new BigDecimal(-1)).setScale(2, BigDecimal.ROUND_DOWN);
                }
                return fieldOddsValueNum;
            default:
                return margin1;
        }
    }

    public static List<Map<String, Object>> calculationOddsByMargin(List<Map<String, Object>> oddsList, String marketKindEnumString, RcsMatchMarketConfig config) {
        if (CollectionUtils.isEmpty(oddsList)){
            return oddsList;
        }
        MarketKindEnum marketKindEnum = MarketKindEnum.getMarketKindByValue(marketKindEnumString);
        BigDecimal originalOdds = new BigDecimal("0");
        BigDecimal otherOdds = new BigDecimal("0");
        BigDecimal uppderOdds = new BigDecimal("0");
        switch (marketKindEnum) {
            case Malaysia:
                for (Map<String, Object> map : oddsList){
                    if (config.getOddsType().equalsIgnoreCase(map.get("oddsType").toString())){
                        originalOdds = new BigDecimal(map.get("fieldOddsValue").toString()).add(config.getOddsChange());
                        break;
                    }
                }
                originalOdds = checkMyOdds(originalOdds);
                uppderOdds = originalOdds.add(config.getMargin());
                if (uppderOdds.doubleValue() >= 1){
                    otherOdds = BigDecimal.valueOf(NumberUtils.INTEGER_TWO).subtract(uppderOdds);
                }else {
                    otherOdds = uppderOdds.multiply(BigDecimal.valueOf(NumberUtils.LONG_MINUS_ONE));
                }
                otherOdds = checkMyOdds(otherOdds);
                for (Map<String, Object> map : oddsList){
                    if (config.getOddsType().equalsIgnoreCase(map.get("oddsType").toString())){
                        map.put("fieldOddsValue",originalOdds.toString());
                    }else {
                        map.put("fieldOddsValue",otherOdds.toString());
                    }
                }
                break;
            case Europe:
                BigDecimal margin = new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE);
                for (Map<String, Object> map : oddsList){
                    if (config.getOddsType().equalsIgnoreCase(map.get("oddsType").toString())){
                        if (new BigDecimal(map.get("fieldOddsValue").toString()).doubleValue() == 0){
                            continue;
                        }
                        margin = margin.divide(new BigDecimal(map.get("fieldOddsValue").toString()),NumberUtils.INTEGER_TWO,BigDecimal.ROUND_HALF_UP);
                        margin = margin.subtract(config.getOddsChange());
                        originalOdds = new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE).divide(margin,NumberUtils.INTEGER_TWO,BigDecimal.ROUND_DOWN);
                        break;
                    }
                }
                // 根据概率算另一边的赔率
                for (Map<String, Object> map : oddsList){
                    if (config.getOddsType().equalsIgnoreCase(map.get("oddsType").toString())){
                        map.put("fieldOddsValue",originalOdds.toString());
                        map.put("originalOddsValue",originalOdds.toString());
                    }else {
                        BigDecimal otherMargin = config.getMargin().subtract(margin);
                        otherOdds = new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE).divide(otherMargin,NumberUtils.INTEGER_TWO,BigDecimal.ROUND_DOWN);
                        map.put("fieldOddsValue",otherOdds.toString());
                        map.put("originalOddsValue",otherOdds.toString());
                    }
                }
                break;
            default: break;
        }
        return oddsList;
    }
    /**
     * @Description 如果负数超过1了，就+2
     *             如果整数超过1 了，就-2
     * @Param [originalOdds]
     * @Author  Sean
     * @Date  14:34 2020/8/15
     * @return java.math.BigDecimal
     **/
    public static BigDecimal checkMyOdds(BigDecimal originalOdds){
        BigDecimal odds = BigDecimal.ZERO;
        if (ObjectUtils.isNotEmpty(originalOdds)){
            if (originalOdds.compareTo(new BigDecimal("-1")) <= 0){
                odds = originalOdds.add(new BigDecimal("2"));
            }else if (originalOdds.compareTo(new BigDecimal("1")) > 0){
                odds = originalOdds.subtract(new BigDecimal("2"));
            }else {
                odds = originalOdds;
            }
        }
        return odds;
    }
    public static BigDecimal caluOddsBySpread(BigDecimal odds, BigDecimal spread) {
        BigDecimal odd = odds.add(spread);
        if (odd.intValue() < 1 ){
            odd = odd.multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE));
        }else if (odd.intValue() >= 1){
            odd = new BigDecimal(NumberUtils.INTEGER_TWO).subtract(odd);
        }
        return odd;
    }
}
