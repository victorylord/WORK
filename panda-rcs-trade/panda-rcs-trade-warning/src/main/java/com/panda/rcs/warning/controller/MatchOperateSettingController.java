package com.panda.rcs.warning.controller;

import com.panda.rcs.warning.service.MatchOperateExceptionMonitorApi;
import com.panda.rcs.warning.vo.PageQuery;
import com.panda.rcs.warning.vo.RcsMatchMonitorSettingUpdate;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping(value = "/matchOperateSetting")
@Api(tags = "操盘异常监控配置")
@Slf4j
public class MatchOperateSettingController {
    @Autowired
    private MatchOperateExceptionMonitorApi matchOperateExceptionMonitorApi;

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Integer>
     * @Description //获取配置参数
     * @Param []
     * @Author kimi
     * @Date 2020/1/13
     **/
    @RequestMapping(value = "/getInitData", method = RequestMethod.GET)
    @ApiOperation("获取配置参数")
    public Response getInitData() {
        return Response.success(matchOperateExceptionMonitorApi.queryRcsMatchMonitorSetting());
    }

    @PostMapping("/save")
    @LogAnnotion(name = "报警阈值设定",
            keys = {"id", "dataType", "type", "riskLevel", "mathOperateSetId",
                    "riskKey", "riskValue", "unit"},
            title = {"操盘异常监控设置ID", "告警类型", "阶段", "监控等级", "操盘异常监控设置详情ID",
                    "操盘异常监控设置key", "操盘异常监控设置value", "单位"})
    @ApiOperation("保存报警阈值设定")
    public Response saveMemo(@RequestBody RcsMatchMonitorSettingUpdate update) {
        log.info("matchOperateSettings---保存报警阈值设定");
        matchOperateExceptionMonitorApi.updateRcsMatchMonitorSetting(update);
        return Response.success();
    }

    /**
     * 查询报警监控日志
     *
     * @return
     */
    @RequestMapping(value = "/queryErrorLogInfo", method = RequestMethod.POST)
    @ApiOperation("获取配置参数")
    public Response queryErrorLogInfo(@RequestBody PageQuery pageQuery, HttpServletRequest request) {
        String lang = StringUtils.isBlank(request.getHeader("lang")) ? "zs" : request.getHeader("lang");
        return Response.success(matchOperateExceptionMonitorApi.queryErrorLogInfo(pageQuery, lang));
    }

}
