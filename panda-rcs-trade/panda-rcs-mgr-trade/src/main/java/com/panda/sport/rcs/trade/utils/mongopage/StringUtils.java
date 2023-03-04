package com.panda.sport.rcs.trade.utils.mongopage;

import org.springframework.util.CollectionUtils;

import com.panda.sport.rcs.constants.I18iConstants;
import com.panda.sport.rcs.utils.I18nUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.utils.stings
 * @Description :  TODO
 * @Date: 2020-07-14 11:06
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public class StringUtils {
    private static final String COMPETITOR_1 = "{$competitor1}";

    private static final String COMPETITOR_2 = "{$competitor2}";
    private static final String HCP1 = "({-hcp})";
    private static final String HCP2 = "({+hcp})";
    private static final String HCP = "({hcp})";

    private static final String TOTAL = "{total}";

    public static String parseName(String name) {
        if (name == null) {
            return "";
        }
        name = name.replace(COMPETITOR_1, I18nUtils.getMessage(I18iConstants.COMPETITOR_1_HOME));
        name = name.replace(COMPETITOR_2, I18nUtils.getMessage(I18iConstants.COMPETITOR_2_AWAY));
        return name;
    }

    public static Map<String, String> parseName(Map<String, String> name, String marketValue) {
        if (CollectionUtils.isEmpty(name)) {
            return null;
        }
        Map<String, String> newName = new HashMap<>();
        for (Map.Entry<String, String> entry : name.entrySet()) {
            String value = entry.getValue();
            double v = Double.parseDouble(marketValue);
            value = value.replace(TOTAL, marketValue);
            value = value.replace(HCP, marketValue);
            value = value.replace(HCP1, String.valueOf(-v));
            value = value.replace(HCP2, String.valueOf(v));
            newName.put(entry.getKey(), value);
        }
        return newName;
    }
}
