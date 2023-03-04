package com.panda.sport.rcs.utils;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.profit.enums.ProfitPlayIdEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Slf4j
public class ForecastSortUtils {

    public static Double getMarketValueCompleteNumber(String marketValueComplete){
        Double MarketValue = 0.0;
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(marketValueComplete)){
            Integer flag = NumberUtils.INTEGER_ONE;
            if (marketValueComplete.startsWith("-")){
                marketValueComplete= marketValueComplete.replace("-","");
                flag = NumberUtils.INTEGER_MINUS_ONE;
            }
            String[] values = marketValueComplete.split("/");
            Double totalValues = 0.0;
            for (String s :values){
                totalValues += Double.parseDouble(s.trim());
            }
            MarketValue = (totalValues / values.length)  * flag;
        }
        return MarketValue;
    }

    public static Integer getBetScoreNumber(String betScore){
        Integer totalValues = 0;
        if (StringUtils.isNotEmpty(betScore)){
            String[] values = betScore.split(":");
            for (String s :values){
                totalValues += Integer.parseInt(s.trim());
            }
        }
        return totalValues;
    }

    public static Integer getPlayOptionsNumber(String playOptions){
        Integer totalValues = 100;
        if (StringUtils.isNotEmpty(playOptions)){
            if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(playOptions) ||
                    BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(playOptions)){
                totalValues = 1;
            }
            if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(playOptions) ||
                    BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(playOptions)){
                totalValues = 2;
            }
        }
        return totalValues;
    }
    public static String getPlayPhaseTypeGroup(Integer playId){
        String totalValues = "0";
        if (ObjectUtils.isNotEmpty(playId)){
            if (ProfitPlayIdEnum.Handicap.getCode().intValue() == playId ||
                    ProfitPlayIdEnum.OverUnder.getCode().intValue() == playId){
                totalValues = "1";
            }else if (ProfitPlayIdEnum.Halftime_Handicap.getCode().intValue() == playId ||
                    ProfitPlayIdEnum.Halftime_OverUnder.getCode().intValue() == playId){
                totalValues = "2";
            }
        }
        return totalValues;
    }

    public static String fetchGroupKey(String str1,String...strings){
        StringBuffer buffer = new StringBuffer(str1);
        if (ObjectUtils.isNotEmpty(strings)){
            for (String str : strings){
                buffer.append("_").append(str);
            }
        }
        return buffer.toString();
    }
}
