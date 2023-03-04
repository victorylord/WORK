package com.panda.sport.rcs.trade.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.panda.sports.auth.exception.SessionValidException;
import com.panda.sports.auth.rpc.IAuthRequiredPermission;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 权限重写类
 * 如果权限 sso com.panda.sports.auth.cache.CacheUtils 有变动请更新这个类
 */

@Slf4j
public class CacheUtils {

    private static final Cache<String, Object> cache = Caffeine.newBuilder()
            .maximumSize(Integer.MAX_VALUE)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build();

    private static final String EXCLUDEURLS = "excludeUrls";
    private static final String PERMISSIONCODE = "permissionCode";
    private static final String SEPARATE = ":";
    private static final String USERSESSION = "userSession";
    private static final String PA = "pa";


    public static List<String> getExcludeUrls(IAuthRequiredPermission iAuthRequiredPermission) {
        //log.info("overrideAuth-getExcludeUrls");
        Object entityCache = cache.getIfPresent(PA.concat(SEPARATE).concat(EXCLUDEURLS));
        if (entityCache == null) {
            return getExcludeUrlsFromRPC(iAuthRequiredPermission);
        }
        return (List<String>) entityCache;
    }

    private static List<String> getExcludeUrlsFromRPC(IAuthRequiredPermission iAuthRequiredPermission) {
        //log.info("overrideAuth-getExcludeUrlsFromRPC");
        List<String> excludeUrls = iAuthRequiredPermission.getExcludePathPatterns();
        if (excludeUrls == null) {
            return Lists.newArrayList();
        }
        cache.put(PA.concat(SEPARATE).concat(EXCLUDEURLS), excludeUrls);
        return excludeUrls;
    }

    public static Map<String, Object> getUserSession(Integer userId, Integer appId, IAuthRequiredPermission iAuthRequiredPermission) throws SessionValidException {
        //log.info("overrideAuth-getUserSession");
        Object entityCache = cache.getIfPresent(PA.concat(SEPARATE).concat(String.valueOf(userId)).concat(SEPARATE).concat(String.valueOf(appId)).concat(SEPARATE).concat(USERSESSION));
        if (entityCache == null) {
            return getUserSessionFromRPC(userId, appId, iAuthRequiredPermission);
        }
        return (Map<String, Object>) entityCache;
    }


    private static Map<String, Object> getUserSessionFromRPC(Integer userId, Integer appId, IAuthRequiredPermission iAuthRequiredPermission) throws SessionValidException {
        //log.info("overrideAuth-getUserSessionFromRPC");
        Map<String, Object> userSession = iAuthRequiredPermission.getLoginUser(userId, appId);
        if (userSession == null) {
            return Maps.newHashMap();
        }
        cache.put(PA.concat(SEPARATE).concat(String.valueOf(userId)).concat(SEPARATE).concat(String.valueOf(appId)).concat(SEPARATE).concat(USERSESSION), userSession);
        return userSession;
    }


    public static Set<String> getPermissionCode(Integer userId, Integer appId, IAuthRequiredPermission iAuthRequiredPermission) throws SessionValidException {
        //log.info("overrideAuth-getPermissionCode");
        Object entityCache = cache.getIfPresent(PA.concat(SEPARATE).concat(String.valueOf(userId)).concat(SEPARATE).concat(String.valueOf(appId)).concat(SEPARATE).concat(PERMISSIONCODE));
        if (entityCache == null) {
            return getPermissionCodeFromRPC(userId, appId, iAuthRequiredPermission);
        }
        return (Set<String>) entityCache;
    }


    private static Set<String> getPermissionCodeFromRPC(Integer userId, Integer appId, IAuthRequiredPermission iAuthRequiredPermission) throws SessionValidException {
        //log.info("overrideAuth-getPermissionCodeFromRPC");
        Set<String> permissionCode = iAuthRequiredPermission.getPermissionCode(userId, appId);
        if (permissionCode == null) {
            return Sets.newHashSet();
        }
        cache.put(PA.concat(SEPARATE).concat(String.valueOf(userId)).concat(SEPARATE).concat(String.valueOf(appId)).concat(SEPARATE).concat(PERMISSIONCODE), permissionCode);
        return permissionCode;
    }
}