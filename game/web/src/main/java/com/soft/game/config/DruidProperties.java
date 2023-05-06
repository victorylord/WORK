/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 只提供了常用的属性，如果有需要，自己添加
 *
 * @author
 * @since
 */
@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class DruidProperties {
    private String url;
    private String username;
    private String password;
    private String publicKey;
    private int initialSize;
    private int minIdle;
    private int maxActive;
    private int timeBetweenEvictionRunsMillis;
    private int minEvictableIdleTimeMillis;
    private String validationQuery;
    private boolean testWhileIdle;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private String filters;
    private String connectionProperties;

}
