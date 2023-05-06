/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.soft.game.generate.mapper.HealthCheckMapper;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
@Slf4j
@RequestMapping("/")
@Api(tags = {"health"}, value = "测试接口和健康检查")
public class MonitorController {

    @Resource
    private HealthCheckMapper healthCheckMapper;

    @GetMapping("/health")
    @ResponseBody
    public String health(HttpServletRequest request, HttpServletResponse response) {
        try {
            healthCheckMapper.selectForHealth();
            // redis 非强依赖 挂掉也可以正常运行
            // redisUtils.hasKey("key");
            return "ok";
        } catch (Exception e) {
            log.error("健康检查发生异常:" + e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "error";
        }
    }

}