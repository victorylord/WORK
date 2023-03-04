package com.panda.rcs.warning.utils;

import com.panda.sports.auth.util.SsoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class TradeUserUtils {

    /*** 老操盘编码 ***/
    public static final Integer OLD_PLATFORM = 10010;

    /*** 新操盘编码 ***/
    public static final Integer NEW_PLATFORM = 10020;

    /**
     * 得到用主户id 如果为空抛错
     */
    public static Integer getUserId() throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Integer userId = SsoUtil.getUserId(request);
        if (userId == null) {
            throw new Exception("用户id为空");
        }
        return userId;
    }

    public static String getLang() throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String lang = getLang(request);
        if (lang!=null && lang.equals("en")){
            return "en";
        }else {
            return "zs";
        }
    }

    private static String getLang(HttpServletRequest request) {
        String lang = request.getHeader("lang");
        if (StringUtils.isEmpty(lang)) {
            lang = request.getParameter("lang");
        }

        return !StringUtils.isEmpty(lang) && !"undefined".equals(lang) ? lang : null;
    }

    public static Integer getAppId() throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Integer appId = SsoUtil.getAppId(request);
        if (appId == null) {
            throw new Exception("appId为空");
        }
        return appId;
    }

    /**
     * 得到用主户id 如果为空返回-1
     */
    public static Integer getUserIdNoException() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Integer userId = SsoUtil.getUserId(request);
            if (userId == null) userId = -1;
            return userId;
        } catch (Exception e) {
//            log.error("用户ID为空：" + e.getMessage());
        }
        return -1;
    }

}
