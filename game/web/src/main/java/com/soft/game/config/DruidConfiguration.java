/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.config;

import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.pool.DruidDataSource;

import lombok.extern.slf4j.Slf4j;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * druid配置
 *
 * @author
 * @since
 */
@Configuration
@EnableConfigurationProperties(DruidProperties.class)
@ConditionalOnClass(DruidDataSource.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@MapperScan(basePackages = {"com.soft.game.generate.mapper", "com.soft.game.dao",
        "com.soft.game.generate.model"}, sqlSessionFactoryRef =
        "sqlSessionFactory")
@Slf4j
public class DruidConfiguration {

    @Autowired
    private DruidProperties properties;

    @Autowired
    private Environment env;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (StringUtils.isEmpty(properties.getUrl())) {
            log.error("Your database connection pool configuration is incorrect!"
                    + " Please check your Spring profile, current profiles are:"
                    + Arrays.toString(env.getActiveProfiles()));
            throw new ApplicationContextException("Database connection pool is not configured correctly");
        }
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(properties.getUrl());

        log.info("properties.getUrl(): " + properties.getUrl() + "=============");
        log.info("properties.getUsername(): " + properties.getUsername() + "=============");

        dataSource.setUsername(properties.getUsername());

        String password = properties.getPassword();
        if (!StringUtils.isEmpty(properties.getPublicKey())) {
            try {
                password = ConfigTools.decrypt(properties.getPublicKey(), password);
            } catch (Exception e) {
                log.error("密码解密失败", e);
            }
        }
        dataSource.setPassword(password);

        if (properties.getInitialSize() > 0) {
            dataSource.setInitialSize(properties.getInitialSize());
        }
        if (properties.getMinIdle() > 0) {
            dataSource.setMinIdle(properties.getMinIdle());
        }
        if (properties.getMaxActive() > 0) {
            dataSource.setMaxActive(properties.getMaxActive());
        }
        if (properties.getTimeBetweenEvictionRunsMillis() > 0) {
            dataSource.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRunsMillis());
        }
        if (properties.getMinEvictableIdleTimeMillis() > 0) {
            dataSource.setMinEvictableIdleTimeMillis(properties.getMinEvictableIdleTimeMillis());
        }
        dataSource.setValidationQuery(properties.getValidationQuery());
        dataSource.setTestWhileIdle(properties.isTestWhileIdle());
        dataSource.setTestOnBorrow(properties.isTestOnBorrow());
        dataSource.setTestOnReturn(properties.isTestOnReturn());
        try {
            dataSource.setFilters(properties.getFilters());
            dataSource.setConnectionProperties(properties.getConnectionProperties());
            //            dataSource.init();
        } catch (SQLException e) {
            //            log.error("datasource connect failed. Errors: {}", e);
            throw new RuntimeException(e);
        }
        dataSource.setConnectionProperties(properties.getConnectionProperties());

        return dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        //        设置数据源
        sqlSessionFactoryBean.setDataSource(dataSource());
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);
        sqlSessionFactoryBean.setTypeAliasesPackage(env.getProperty("mybatis.type-aliases-package")); //  指定基包
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setCacheEnabled(true);
        // 采用驼峰式命名方法
        configuration.setMapUnderscoreToCamelCase(true);
        sqlSessionFactoryBean.setConfiguration(configuration);
        return sqlSessionFactoryBean.getObject();
    }

}
