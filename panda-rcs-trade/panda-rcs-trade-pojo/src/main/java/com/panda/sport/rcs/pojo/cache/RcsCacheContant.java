package com.panda.sport.rcs.pojo.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.panda.sport.rcs.cache.RcsCacheUtils;
import com.panda.sport.rcs.pojo.StandardMatchInfo;

import java.util.Map;

public interface RcsCacheContant {

    /**
     * 多语言缓存
     */
    String LANGUAGE = "RCS_LANGUAGE";

    Cache<Long, String> LANGUAGE_CACHE = RcsCacheUtils.newCache(LANGUAGE,
            500, 20000, 30, 30, 30);

    /**
     * 赛事缓存
     */
    String MATCHINFO = "RCS_MATCHINFO";

    Cache<Long, StandardMatchInfo> MATCHINFO_CACHE = RcsCacheUtils.newCache(MATCHINFO,
            200, 1000, 60, 60, 60);

    /**
     * 球队缓存
     */
    String TEAMINFO = "RCS_TEAMINFO";

    Cache<Long, String> TEAMINFO_CACHE = RcsCacheUtils.newCache(TEAMINFO,
            400, 2000, 60, 60, 60);

    /**
     * 投注项模板缓存
     */
    String ODDSTEMPLATE = "RCS_ODDSTEMPLATE";

    Cache<Long, String> ODDSTEMPLATE_CACHE = RcsCacheUtils.newCache(ODDSTEMPLATE,
            800, 4000, 60, 60, 60);

    /**
     * 赛事账务日期
     */
    String MATCH_DATE_EXPECT = "RCS_MATCH_DATE_EXPECT";

    Cache<Long, String> MATCH_DATE_EXPECT_CACHE = RcsCacheUtils.newCache(MATCH_DATE_EXPECT,
            200, 1000, 60, 60, 60);

    /**
     * 标准投注项模板ID
     */
    String ODDS_FIELDS_TEMPLATE_ID = "RCS_ODDS_FIELDS_TEMPLATE_ID";

    Cache<Long, Map<Integer, Long>> ODDS_FIELDS_TEMPLATE_ID_CACHE = RcsCacheUtils.newCache(ODDS_FIELDS_TEMPLATE_ID,
            50, 200, 60, 60, 60);
}
