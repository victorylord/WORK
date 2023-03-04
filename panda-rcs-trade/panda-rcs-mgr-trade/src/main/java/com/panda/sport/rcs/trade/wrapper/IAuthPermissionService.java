package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sports.auth.exception.SessionValidException;

/**
 * @description:权限
 * @author: davidqiang
 * @create: 2022-09-06 16:50
 **/
public interface IAuthPermissionService {

    /**
     * @Description   //校验是否有操作权限
     * @Param [userId, appId, playId, permissonStr]
     * @Author  davidqiang
     * @Date   2022/09/06
     * @return HttpResponse
     **/
    boolean checkAuthOpearate(String url) throws Exception;

}
