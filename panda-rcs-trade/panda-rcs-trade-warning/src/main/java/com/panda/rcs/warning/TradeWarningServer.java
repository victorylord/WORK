package com.panda.rcs.warning;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.panda.rcs.warning.*","com.panda.sport.rcs.core.cache", "com.panda.sport.rcs"})
@MapperScan(basePackages = "com.panda.rcs.warning.**.mapper")
@EnableScheduling
@ServletComponentScan
@EnableDiscoveryClient
public class TradeWarningServer implements ApplicationListener {

    public static void main(String[] args) {
        SpringApplication.run(TradeWarningServer.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            log.info("初始化环境变量 ");
        }
        if (event instanceof ApplicationPreparedEvent) {
            log.info("初始化完成");
        }
        if (event instanceof ContextRefreshedEvent) {
            log.info("应用刷新");
        }
        if (event instanceof ApplicationReadyEvent) {
            log.info("应用已启动完成");
        }
        if (event instanceof ContextStartedEvent) {
            log.info("应用启动，需要在代码动态添加监听器才可捕获");
        }
        if (event instanceof ContextStoppedEvent) {
            log.info("应用停止");
        }
        if (event instanceof ContextClosedEvent) {
            log.info("应用关闭");
        }
    }
}
