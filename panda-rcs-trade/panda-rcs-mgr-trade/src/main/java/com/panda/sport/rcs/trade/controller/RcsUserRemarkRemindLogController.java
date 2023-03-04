package com.panda.sport.rcs.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.pojo.RcsUserRemarkRemindLog;
import com.panda.sport.rcs.trade.service.UserRemarkRemindLogService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsUserRemarkRemindLogService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsUserRemarkRemindLogQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 用戶人工备注提醒日志
 *
 * @description:
 * @author: magic
 * @create: 2022-05-29 10:15
 **/
@Component
@RestController
@RequestMapping(value = "/rcsUserRemarkRemindLog")
@Slf4j
public class RcsUserRemarkRemindLogController {
    @Autowired
    IRcsUserRemarkRemindLogService rcsUserRemarkRemindLogService;

    @Autowired
    UserRemarkRemindLogService userRemarkRemindLogService;

    /**
     * 修改人工备注提醒  需求：1874
     *
     * @return
     */
    @RequestMapping(value = "/updateRemark", method = RequestMethod.POST)
    public HttpResponse updateRemark(@RequestBody RcsUserRemarkRemindLog rcsUserRemarkRemindLog) {
        try {
            if (StringUtils.isBlank(rcsUserRemarkRemindLog.getUserId())) {
                log.error("::{}::用戶id不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("用戶id不能为空");
            }
            if (StringUtils.isBlank(rcsUserRemarkRemindLog.getUsername())) {
                log.error("::{}::用戶名不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("用戶名不能为空");
            }
            if (StringUtils.isBlank(rcsUserRemarkRemindLog.getMerchantCode())) {
                log.error("::{}::商户编码不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("商户编码不能为空");
            }
            if (StringUtils.isBlank(rcsUserRemarkRemindLog.getRemark())) {
                log.error("::{}::备注不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("备注不能为空");
            }
//            if (rcsUserRemarkRemindLog.getRemindDate() == null) {
//                log.error("提醒日期不能为空");
//                return HttpResponse.failToMsg("提醒日期不能为空");
//            }
            Integer traderId = TradeUserUtils.getUserId();
            userRemarkRemindLogService.updateRemark(rcsUserRemarkRemindLog, traderId);
            return HttpResponse.success();
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    /**
     * 列表分页查询 需求：1874
     *
     * @return
     */
    @RequestMapping(value = "/findPage", method = RequestMethod.POST)
    public HttpResponse<Page<RcsUserRemarkRemindLog>> findPage(@RequestBody RcsUserRemarkRemindLogQueryVo param) {
        try {
            if (Objects.isNull(param.getCurrentPage()) || param.getCurrentPage().equals(0)) {
                param.setCurrentPage(1);
                param.setPageSize(10);
            }
            Page<RcsUserRemarkRemindLog>  page = userRemarkRemindLogService.getUserRemarkRemindLog(param);
            return HttpResponse.success(page);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    /**
     * 获取用户日志列表 需求：1874
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public HttpResponse list(String userId) {
        try {
            if (userId == null) {
                log.error("::{}::用戶id不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("用戶id不能为空");
            }
            List<RcsUserRemarkRemindLog> list = userRemarkRemindLogService.getList(userId);
            return HttpResponse.success(list);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    /**
     * 获取用户日志总数 需求：1874
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public HttpResponse count(String userId) {
        try {
            if (userId == null) {
                log.error("::{}::用戶id不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("用戶id不能为空");
            }
            long count = userRemarkRemindLogService.getCount(userId);
            return HttpResponse.success(count);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    /**
     * 获取用户当前最新日志 需求：1874
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public HttpResponse get(String userId) {
        try {
            if (userId == null) {
                log.error("::{}::用戶id不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("用戶id不能为空");
            }
            RcsUserRemarkRemindLog rcsUserRemarkRemindLog = userRemarkRemindLogService.getOne(userId);
            return HttpResponse.success(rcsUserRemarkRemindLog);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }
}
