package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  滚球操盘接口
 * @Date: 2020-01-13 15:23
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@RequestMapping(value = "/grounderController")
@Slf4j
public class GrounderController {
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Integer>
     * @Description //获取滚球数量
     * @Param []
     * @Author kimi
     * @Date 2020/1/13
     **/
    @RequestMapping(value = "/getGrounderNumber", method = RequestMethod.GET)
    public HttpResponse<Integer> getGrounderNumber() {
        try {
            Integer grounderNumber = standardMatchInfoService.getGrounderNumber();
            return HttpResponse.success(grounderNumber);
        } catch (Exception e) {
            log.error("::{}::获取滚球数量:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }
}
