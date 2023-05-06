/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.generate.mapper;

import org.springframework.stereotype.Repository;

/**
 * @author tianbaozheng
 */
@Repository
public interface HealthCheckMapper {

    /**
     * 数据库健康检查
     *
     * @param
     * @return java.lang.Integer
     * @description
     * @author tianbaozheng
     * @date 2021/9/9 2:44 下午
     */

    Integer selectForHealth();
}
