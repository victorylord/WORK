package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.util.StringUtil;
import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.pojo.RcsMatchEventTypeInfo;
import com.panda.sport.rcs.pojo.tourTemplate.*;
import com.panda.sport.rcs.trade.service.RcsMatchEventTypeInfoServiceImpl;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IAuthPermissionService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.RcsTournamentTemplateAcceptConfigAutoChangeService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sports.auth.permission.AuthRequiredPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description //赛事接拒单事件配置
 * @Param
 * @Author Sean
 * @Date 19:46 2020/9/4
 * @return
 **/
@RestController
@RequestMapping(value = "/event")
@Slf4j
@Component
public class MatchEventConfigController {

    @Autowired
    RcsMatchEventTypeInfoServiceImpl rcsMatchEventTypeInfoService;
    @Autowired
    IAuthPermissionService iAuthPermissionService;
    @Autowired
    private RcsTournamentTemplateAcceptConfigAutoChangeService rcsTournamentTemplateAcceptConfigAutoChangeService;

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse
     * @Description //分页查询事件
     * @Param [info]
     * @Author Sean
     * @Date 16:08 2020/9/6
     **/
    @GetMapping("/list")
    public HttpResponse list(RcsMatchEventTypeInfo info, @RequestHeader(value = "lang",required = false) String lang) {
        try {
            Assert.notNull(info.getSportId(), "sportId不能为空");
            if(StringUtil.isNotEmpty(lang)){
                info.setLang(lang);
            }

            Map<String, Object> data = rcsMatchEventTypeInfoService.list(info,lang);
            return HttpResponse.success(data);
        } catch (IllegalArgumentException e) {
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse
     * @Description //更新事件备注
     * @Param [info]
     * @Author Sean
     * @Date 16:09 2020/9/6
     **/
    @PostMapping("/updateById")
    public HttpResponse updateById(@RequestBody RcsMatchEventTypeInfo info) {
        try {
            log.info("::{}::更新事件备注:{},操盘手:{}",CommonUtil.getRequestId(info.getId()), JSONObject.toJSONString(info), TradeUserUtils.getUserId());
            Assert.notNull(info.getId(), "事件id不能为空");
            rcsMatchEventTypeInfoService.updateById(info);
            return HttpResponse.success();
        } catch (IllegalArgumentException e) {
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (RcsServiceException ex) {
            return HttpResponse.fail(ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse
     * @Description //查询事件源配置
     * @Param [config]
     * @Author Sean
     * @Date 16:09 2020/9/6
     **/
    @GetMapping("/dataSourceConfig")
    public HttpResponse dataSourceConfig(RcsTournamentTemplateAcceptConfig config) {
        try {
            Assert.notNull(config.getTemplateId(), "模板id不能为空");
            Assert.notNull(config.getCategorySetId(), "玩法集id不能为空");
            RcsTournamentTemplateAcceptConfig dataSourceConfig = rcsMatchEventTypeInfoService.queryDataSourceConfig(config);
            return HttpResponse.success(dataSourceConfig);
        } catch (IllegalArgumentException e) {
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }

    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse
     * @Description //查询事件类型配置
     * @Param [config]
     * @Author Sean
     * @Date 16:10 2020/9/6
     **/
    @GetMapping("/eventConfig")
    public HttpResponse queryEventConfig(RcsTournamentTemplateAcceptEvent config) {
        try {
//            Assert.notNull(config.getSportId(),"运动类型不能为空");
            Assert.notNull(config.getTemplateId(), "模板id不能为空");
            Assert.notNull(config.getCategorySetId(), "玩法集id不能为空");
            Map<String, Object> eventConfigs = rcsMatchEventTypeInfoService.queryEventConfig(config);
            return HttpResponse.success(eventConfigs);
        } catch (IllegalArgumentException e) {
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse
     * @Description //根据事件类型查询事件
     * @Param [config]
     * @Author Sean
     * @Date 16:11 2020/9/6
     **/
    @GetMapping("/eventConfigByType")
    public HttpResponse queryEventConfigByType(RcsTournamentTemplateAcceptEvent config) {
        try {
//            Assert.notNull(config.getSportId(),"运动类型不能为空");
            Assert.notNull(config.getTemplateId(), "模板id不能为空");
            Assert.notNull(config.getCategorySetId(), "玩法集id不能为空");
            Assert.notNull(config.getEventType(), "事件类型不能为空");
            Map<String, Object> eventConfigs = rcsMatchEventTypeInfoService.queryEventConfigByType(config);
            return HttpResponse.success(eventConfigs);
        } catch (IllegalArgumentException e) {
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse
     * @Description //更新事件等待时间和事件类型配置
     * @Param [config]
     * @Author Sean
     * @Date 16:11 2020/9/6
     **/
    @PostMapping("/updateEventAndTimeConfig")
    @LogAnnotion(name = "接拒单配置", keys = {"matchId", "templateId", "dataSource", "categorySetId", "normal", "minWait", "maxWait", "events"}, title = {"赛事id", "模板id", "数据源", "玩法集id", "T常规", "T延时", "Tmax", "事件"})
    @OperateLog
    public HttpResponse updateEventAndTimeConfig(@RequestBody RcsTournamentTemplateAcceptConfig config) {
        try {
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:Event:Save");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            Assert.notNull(config.getCategorySetId(), "玩法集id不能为空");
            Assert.notNull(config.getNormal(), "T常不能为空");
            Assert.notNull(config.getMinWait(), "T延不能为空");
            Assert.notNull(config.getMaxWait(), "Tmax不能为空");
            rcsMatchEventTypeInfoService.updateEventAndTimeConfig(config);
            return HttpResponse.success();
        } catch (IllegalArgumentException e) {
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse
     * @Description //更新赛事模板自动接拒开关
     * @Param [config]
     * @Author Sean
     * @Date 16:11 2020/9/6
     **/
    @PostMapping("/updateTemplateAcceptConfigAutoChange")
    @LogAnnotion(name = "更新赛事模板自动接拒开关", keys = {"templateId", "categorySetId", "isOpen"}, title = {"模板id", "玩法集id", "开关(0.关 1.开)"})
    @OperateLog
    public HttpResponse updateTemplateAcceptConfigAutoChange(@RequestBody RcsTournamentTemplateAcceptConfigAutoChange config) {
        try {
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:auto:update");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            Assert.notNull(config.getTemplateId(), "templateId不能为空");
            Assert.notNull(config.getCategorySetId(), "categorySetId不能为空");
            Assert.notNull(config.getIsOpen(), "isOpen不能为空");
            rcsTournamentTemplateAcceptConfigAutoChangeService.updateTemplateAcceptConfigAutoChange(config);
            return HttpResponse.success();
        } catch (IllegalArgumentException e) {
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse
     * @Description //查询赛事模板自动接拒开关
     * @Param [config]
     * @Author waldkir
     * @Date 16:11 2022/5/16
     **/
    @PostMapping("/queryTemplateAcceptConfigAutoChange")
    public HttpResponse queryTemplateAcceptConfigAutoChange(@RequestBody RcsTournamentTemplateAcceptConfigAutoChange config) {
        try {
            Assert.notNull(config.getTemplateId(), "templateId不能为空");
            Assert.notNull(config.getCategorySetId(), "categorySetId不能为空");
            return HttpResponse.success(rcsTournamentTemplateAcceptConfigAutoChangeService.queryTemplateAcceptConfigAutoChange(config));
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * @Description 接拒单玩法集事件复制功能
     * @Author carver
     * @Date 15:11 2021/1/8
     **/
    @PostMapping("/copyEventAndTimeConfig")
    @OperateLog(operateType = OperateLogEnum.COPY_EVENT_AND_TIME_CONFIG)
    public HttpResponse copyEventAndTimeConfig(@RequestBody RcsTournamentTemplateAcceptConfig config) {
        try {
            Assert.notNull(config.getTemplateId(), "模板id不能为空");
            Assert.notNull(config.getCategorySetId(), "玩法集id不能为空");
            Assert.notNull(config.getCopyCategorySetId(), "复制的玩法集id不能为空");
            Assert.notNull(config.getCopyCategorySetName(), "复制的玩法集名称不能为空");
            rcsMatchEventTypeInfoService.copyEventAndTimeConfig(config);
            return HttpResponse.success();
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 查询结算事件源配置
     *
     * @Author carver
     * @Date 15:11 2021/10/16
     */
    @GetMapping("/dataSourceConfigSettle")
    public HttpResponse dataSourceConfigSettle(RcsTournamentTemplateAcceptConfigSettle settle) {
        try {
            Assert.notNull(settle.getTemplateId(), "模板id不能为空");
            Assert.notNull(settle.getCategorySetId(), "玩法集id不能为空");
            RcsTournamentTemplateAcceptConfigSettle dataSourceConfig = rcsMatchEventTypeInfoService.queryDataSourceConfigSettle(settle);
            return HttpResponse.success(dataSourceConfig);
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 查询结算事件类型配置
     *
     * @Author carver
     * @Date 15:11 2021/10/16
     */
    @GetMapping("/eventConfigSettle")
    public HttpResponse eventConfigSettle(RcsTournamentTemplateAcceptEventSettle config) {
        try {
            Assert.notNull(config.getTemplateId(), "模板id不能为空");
            Assert.notNull(config.getCategorySetId(), "玩法集id不能为空");
            Map<String, Object> eventConfigs = rcsMatchEventTypeInfoService.queryEventConfigSettle(config);
            return HttpResponse.success(eventConfigs);
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 更新结算事件等待时间和事件类型配置
     *
     * @Author carver
     * @Date 15:11 2021/10/17
     */
    @PostMapping("/updateEventAndTimeConfigSettle")
    @LogAnnotion(name = "接拒单结算配置", keys = {"matchId", "templateId", "dataSource", "categorySetId", "normal", "minWait", "maxWait", "events"}, title = {"赛事id", "模板id", "数据源", "玩法集id", "T常规", "T延时", "Tmax", "事件"})
    public HttpResponse updateEventAndTimeConfigSettle(@RequestBody RcsTournamentTemplateAcceptConfigSettle config) {
        try {
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:Event:Save");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            Assert.notNull(config.getCategorySetId(), "玩法集id不能为空");
            Assert.notNull(config.getNormal(), "T常不能为空");
            Assert.notNull(config.getMinWait(), "T延不能为空");
            Assert.notNull(config.getMaxWait(), "Tmax不能为空");
            rcsMatchEventTypeInfoService.updateEventAndTimeConfigSettle(config);
            return HttpResponse.success();
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }
}
