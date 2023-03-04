package com.panda.sport.rcs.utils;

import com.github.kiprobinson.bigfraction.BigFraction;
import com.panda.sport.rcs.common.OddsValueConvertUtils;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.mongo.MatchMarketOddsVo;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.utils
 * @Description : 赔率转换工具类
 * @Author : Paca
 * @Date : 2020-07-22 20:33
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public class OddsConvertUtils extends OddsValueConvertUtils {

    /**
     * 赔率转换并已默认方式显示，保留两位小数
     *
     * @param kind
     * @param originalValue
     * @return
     */
    public static String convertAndDefaultDisplay(MarketKindEnum kind, Integer originalValue) {
        BigDecimal newValue = convertTo(kind, originalValue);
        if (newValue == null) {
            return null;
        }
        BigDecimal b2 = new BigDecimal(BaseConstants.MULTIPLE_VALUE);
        if (MarketKindEnum.UnitedKingdom == kind) {
            // 英式赔率以分数表示
            return BigFraction.valueOf(originalValue - BaseConstants.MULTIPLE_VALUE, b2).toString();
        }
        return new DecimalFormat("#0.00").format(newValue.doubleValue());
    }

    /**
     * 通过盘口下投注项集合计算margin值
     *
     * @param marketOddsList 盘口下投注项集合
     * @param marketKind     盘口类别
     * @return
     */
    public static BigDecimal calMarginByOddsList(List<MatchMarketOddsVo> marketOddsList, MarketKindEnum marketKind) {
        BigDecimal margin = BigDecimal.ZERO;
        if(marketKind== null) marketKind= MarketKindEnum.Europe;
        if(!CollectionUtils.isEmpty(marketOddsList)){
            marketOddsList = marketOddsList.stream().filter(fi->fi.getActive()!=0).collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(marketOddsList)||marketOddsList.size()==0)return margin;
        switch (marketKind) {
            case Europe:
                for (MatchMarketOddsVo marketOdds : marketOddsList) {
                    BigDecimal fieldOddsValue = new BigDecimal(marketOdds.getFieldOddsValue());
                    if (fieldOddsValue.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal divide = BigDecimal.ONE.divide(fieldOddsValue, BaseConstants.ODDS_CALCULATION_SCALE, BigDecimal.ROUND_HALF_UP);
                        margin = margin.add(divide);
                    }
                }
                return margin.multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_DOWN);
            case Malaysia:
                BigDecimal fieldOddsValueSum = BigDecimal.ZERO;
                for (MatchMarketOddsVo marketOdds : marketOddsList) {
                    BigDecimal fieldOddsValue = new BigDecimal(marketOdds.getFieldOddsValue());
                    fieldOddsValueSum = fieldOddsValueSum.add(fieldOddsValue);
                }
                // 小于0
                if (fieldOddsValueSum.compareTo(BigDecimal.ZERO) <= 0) {
                    margin = fieldOddsValueSum.negate().setScale(2, BigDecimal.ROUND_DOWN);
                } else {
                    margin = new BigDecimal("2").subtract(fieldOddsValueSum).setScale(2, BigDecimal.ROUND_DOWN);
                }
                return margin;
            default:
                return margin;
        }
    }

    /**
     * 校准马来赔
     *
     * @param malayOdds
     * @return
     */
    public static BigDecimal checkMalayOdds(BigDecimal malayOdds) {
        // 计算后的值 <= -1，则 计算后的值 + 2
        if (malayOdds.compareTo(BigDecimal.ONE.negate()) <= 0) {
            return malayOdds.add(new BigDecimal(NumberUtils.INTEGER_TWO));
        }
        // 计算后的值 > 1，则 计算后的值 - 2
        if (malayOdds.compareTo(BigDecimal.ONE) > 0) {
            return malayOdds.subtract(new BigDecimal(NumberUtils.INTEGER_TWO));
        }
        return malayOdds;
    }

    /**
     * 通过马来赔计算spread
     *
     * @param upperOddValue
     * @param downOddValue
     * @return
     */
    public static BigDecimal calSpreadByMalayOdds(String upperOddsValue, String downOddsValue) {
        BigDecimal upperOdds = CommonUtils.toBigDecimal(upperOddsValue, BigDecimal.ONE);
        BigDecimal downOdds = CommonUtils.toBigDecimal(downOddsValue, BigDecimal.ONE);
        if (upperOdds.multiply(downOdds).compareTo(BigDecimal.ZERO) > 0) {
            return new BigDecimal(NumberUtils.INTEGER_TWO).subtract(upperOdds).subtract(downOdds);
        } else {
            return upperOdds.add(downOdds).abs();
        }
    }
}
