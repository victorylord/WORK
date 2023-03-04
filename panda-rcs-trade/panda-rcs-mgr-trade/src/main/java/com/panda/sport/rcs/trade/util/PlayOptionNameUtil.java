package com.panda.sport.rcs.trade.util;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.panda.sport.data.rcs.dto.OrderItem;
import com.panda.sport.rcs.common.MatchConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.util
 * @Description :  TODO
 * @Date: 2020-08-04 16:08
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public class PlayOptionNameUtil {
    public static String assemblyMarketValue(Map<String,Object> map, OrderItem item, String optionValue, Pattern pattern) {
        String home = "";
        String away = "";
        if (ObjectUtils.isNotEmpty(map.get("homeName"))) {
            Map<String,String> teamMap = (Map<String,String>)map.get("homeName");
            if (!org.springframework.util.ObjectUtils.isEmpty(teamMap.get("zs"))){
                home = teamMap.get("zs");
            }else if (!org.springframework.util.ObjectUtils.isEmpty(teamMap.get("en"))){
                home = teamMap.get("en");
            }
        }
        if (ObjectUtils.isNotEmpty(map.get("awayName"))) {
            Map<String,String> teamMap = (Map<String,String>)map.get("awayName");
            if (!org.springframework.util.ObjectUtils.isEmpty(teamMap.get("zs"))){
                away = teamMap.get("zs");
            }else if (!org.springframework.util.ObjectUtils.isEmpty(teamMap.get("en"))){
                away = teamMap.get("en");
            }
        }
        String marketValue = item.getMarketValue();
        String playName = item.getPlayName();
        String playOptions = item.getPlayOptions();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(optionValue)){
            if (optionValue.contains(MatchConstant.TITLE_SHOW_NAME_COMPETITOR1_AHCP)){
                optionValue = optionValue.replace(MatchConstant.TITLE_SHOW_NAME_COMPETITOR1_AHCP, home);
            }
            if (optionValue.contains(MatchConstant.TITLE_SHOW_NAME_COMPETITOR2_AHCP)){
                optionValue = optionValue.replace(MatchConstant.TITLE_SHOW_NAME_COMPETITOR2_AHCP, away);
            }
            if (optionValue.contains(MatchConstant.TITLE_SHOW_NAME_TOTAL)){
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(marketValue)) {
                    if (marketValue.contains("大") || marketValue.contains("小")) {
                        marketValue = marketValue.replace("大 ", "");
                        marketValue = marketValue.replace("小 ", "");
                    }
                }
                optionValue = optionValue.replace(MatchConstant.TITLE_SHOW_NAME_TOTAL, "") + marketValue;
            }
            if (optionValue.contains(MatchConstant.TITLE_SHOW_NAME_HCP)){
                optionValue = optionValue.replace(MatchConstant.TITLE_SHOW_NAME_HCP, marketValue);
            }else if (optionValue.contains(MatchConstant.TITLE_SHOW_NAME_HCP2)){
                if (marketValue.contains("-")){
                    optionValue = optionValue.replace(MatchConstant.TITLE_SHOW_NAME_HCP2, marketValue);
                }else {
                    optionValue = optionValue.replace(MatchConstant.TITLE_SHOW_NAME_HCP2, "+"+marketValue);
                }
            }else if (optionValue.contains(MatchConstant.TITLE_SHOW_NAME_HCP1)){
                if (marketValue.contains("-")){
                    String replace = marketValue.replace("-", "");
                    optionValue = optionValue.replace(MatchConstant.TITLE_SHOW_NAME_HCP1, "+"+replace);
                }else {
                    optionValue = optionValue.replace(MatchConstant.TITLE_SHOW_NAME_HCP1, "-"+marketValue);

                }
            }else if (optionValue.contains(MatchConstant.HCP)){
                optionValue = optionValue.replace(MatchConstant.HCP, marketValue);
            }
            if (optionValue.contains("null") || optionValue.contains(":") || optionValue.contains("Other")) {
                optionValue = optionValue.replace("null", "");
                optionValue = optionValue.replace(":", "-");
                optionValue = optionValue.replace("Other", "其他");
            }
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(playName)) {
            if (playName.contains("净胜")) {
                if (!playOptions.contains("Other")) {
                    String[] ands = playOptions.split("And");
                    if (ands != null && ands.length > 1) {
                        if (ands[0].equals("1")) {
                            optionValue = home + "-净胜  ";
                        } else if (ands[0].equals("2")) {
                            optionValue = away + "-净胜  ";
                        }
                        optionValue = optionValue +  "  " + ands[1];
                    }else {
                        optionValue =  ands[0];
                    }
                }else {
                    optionValue="其他";
                }
            }
        }
        if (StringUtils.isEmpty(optionValue) || "null".equals(optionValue)) {
            optionValue = item.getPlayOptionsName();
        }
        return optionValue;
    }
}
