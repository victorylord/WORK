package com.panda.rcs.warning.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.rcs.warning.service.MatchOperateExceptionMonitorApi;
import com.panda.rcs.warning.utils.TradeUserUtils;
import com.panda.rcs.warning.vo.MatchOperateListQuery;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.rcs.utils.i18n.CommonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  操盘异常监控设置接口
 * @Date: 2022-06-08 14:03
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@RequestMapping(value = "/matchOperateList")
@Api(tags = "操盘监控列表")
@Slf4j
public class MatchOperateListController {
    @Autowired
    private MatchOperateExceptionMonitorApi matchOperateExceptionMonitorApi;
    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Integer>
     * @Description //获取监控列表
     * @Param []
     * @Author kimi
     * @Date 2020/1/13
     **/
    @RequestMapping(value = "/getMatchOperateList", method = RequestMethod.POST)
    @ApiOperation("分頁监控列表")
    @ResponseBody
    public Response getMatchOperateList(@RequestBody MatchOperateListQuery matchOperateListQuery, HttpServletRequest request) {
        try {
            String lang=request.getHeader("lang");
            matchOperateListQuery.setLang(StringUtils.isBlank(lang)?"zs":lang);
            return Response.success(matchOperateExceptionMonitorApi.selectMatchOperateList(matchOperateListQuery));
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtils.getLinkId(),e.getMessage(),e);
            return Response.fail("风控服务器出问题");
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Integer>
     * @Description //获取监控日志列表
     * @Param []
     * @Author kimi
     * @Date 2020/1/13
     **/
    @RequestMapping(value = "/getMatchOperateLogList", method = RequestMethod.POST)
    @ApiOperation("监控日志列表")
    @ResponseBody
    public Response getMatchOperateLogList() {
        return Response.success();
    }

}
