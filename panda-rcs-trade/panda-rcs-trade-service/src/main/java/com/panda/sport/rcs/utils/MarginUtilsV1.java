package com.panda.sport.rcs.utils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.enums.MarketKindEnum;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description //TODO
 * @Param
 * @Author kimi
 * @Date 2019/11/15
 * @return
 **/
@Slf4j
public class MarginUtilsV1 {


    /**
     * @return boolean
     * @Description //magin值校验
     * @Param [oddsList, marketKindEnum, magin]
     * @Author kimi
     * @Date 2019/12/11
     **/
    public static boolean convert(List<Map<String, Object>> oddsList, String marketKindEnumString, BigDecimal margin) {
        BigDecimal margin1 = new BigDecimal(0);
        BigDecimal bigDecimal = new BigDecimal(BaseConstants.MULTIPLE_VALUE);
        MarketKindEnum marketKindEnum = MarketKindEnum.getMarketKindByValue(marketKindEnumString);
        switch (marketKindEnum) {
            //欧赔不验证
            case Europe:
//                for (Map<String, Object> map : oddsList) {
//                    BigDecimal fieldOddsValue = new BigDecimal(map.get("fieldOddsValue").toString());
//                    BigDecimal divide = bigDecimal.divide(fieldOddsValue, BaseConstants.ODDS_CALCULATION_SCALE, BigDecimal.ROUND_HALF_UP);
//                    margin1 = margin1.add(divide);
//                }
//                int v = margin1.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN).intValue();
//                return margin.intValue() == v;

                return true;
            case Malaysia:
                BigDecimal fieldOddsValueNum = new BigDecimal(0);
                for (Map<String, Object> map : oddsList) {
                    BigDecimal fieldOddsValue = new BigDecimal(map.get("fieldOddsValue").toString());
                    fieldOddsValueNum = fieldOddsValueNum.add(fieldOddsValue);
                }
                fieldOddsValueNum = fieldOddsValueNum.divide(bigDecimal, 2, BigDecimal.ROUND_DOWN);
                //小于0
                if (fieldOddsValueNum.compareTo(new BigDecimal(0)) == -1) {
                    margin1 = fieldOddsValueNum.divide(new BigDecimal(-1)).setScale(2, BigDecimal.ROUND_DOWN);
                } else {
                    margin1 = new BigDecimal("2").subtract(fieldOddsValueNum).setScale(2, BigDecimal.ROUND_DOWN);
                }
                return margin.doubleValue() == margin1.doubleValue();
            default:
                return false;
        }
    }


    public static BigDecimal convert(List<Map<String, Object>> oddsList, String marketKindEnumString) {
        BigDecimal margin1 = new BigDecimal(0);
        BigDecimal bigDecimal = new BigDecimal(BaseConstants.MULTIPLE_VALUE);
        MarketKindEnum marketKindEnum = MarketKindEnum.getMarketKindByValue(marketKindEnumString);
        switch (marketKindEnum) {
            case Europe:
                for (Map<String, Object> map : oddsList) {
                    BigDecimal fieldOddsValue = new BigDecimal(map.get("fieldOddsValue").toString());
                    if (fieldOddsValue.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal divide = bigDecimal.divide(fieldOddsValue, BaseConstants.ODDS_CALCULATION_SCALE, BigDecimal.ROUND_HALF_UP);
                        margin1 = margin1.add(divide);
                    }
                }
                return margin1.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN);
            case Malaysia:
                BigDecimal fieldOddsValueNum = new BigDecimal(0);
                for (Map<String, Object> map : oddsList) {
                    BigDecimal fieldOddsValue = new BigDecimal(map.get("fieldOddsValue").toString());
                    fieldOddsValueNum = fieldOddsValueNum.add(fieldOddsValue);
                }
                //小于0
                if (fieldOddsValueNum.compareTo(new BigDecimal(0)) == -1) {
                    margin1 = fieldOddsValueNum.divide(new BigDecimal(-1)).setScale(2, BigDecimal.ROUND_DOWN);
                } else {
                    margin1 = new BigDecimal("2").subtract(fieldOddsValueNum).setScale(2, BigDecimal.ROUND_DOWN);
                }
                return margin1;
            default:
                return margin1;
        }
    }

    public static void main(String[] args) {
        System.out.println(BigDecimal.ZERO.compareTo(BigDecimal.ZERO));
    }
}