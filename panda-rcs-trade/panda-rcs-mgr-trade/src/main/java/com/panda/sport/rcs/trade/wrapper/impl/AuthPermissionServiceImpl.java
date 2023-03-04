package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CacheUtils;
import com.panda.sport.rcs.trade.wrapper.IAuthPermissionService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sports.auth.exception.SessionValidException;
import com.panda.sports.auth.rpc.IAuthRequiredPermission;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description:权限
 * @author: davidqiang
 * @create: 2022-09-06 16:50
 **
 */
@Service
@Slf4j
public class AuthPermissionServiceImpl implements IAuthPermissionService {
    @Reference
    private IAuthRequiredPermission iAuthRequiredPermission;
    @Override
    public boolean checkAuthOpearate(String url) throws Exception {
        Integer userId = TradeUserUtils.getUserId();
        Integer appId = TradeUserUtils.getAppId();
        log.info("::{}::,用户权限缓存:{}",userId, appId);
        Set<String> permissionCode = CacheUtils.getPermissionCode(userId, appId, iAuthRequiredPermission);
        List<String> collect = permissionCode.stream().filter(e -> e.equals(url)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(collect))
        {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
