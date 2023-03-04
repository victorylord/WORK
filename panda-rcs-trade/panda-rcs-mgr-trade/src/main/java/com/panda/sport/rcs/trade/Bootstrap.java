package com.panda.sport.rcs.trade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
@SpringBootApplication
@MapperScan(basePackages = "com.panda.sport.rcs.**.mapper")
@ImportResource(locations= {"classpath:app-provider.xml"})
@ComponentScan(basePackages = {"com.panda.sport.rcs","com.panda.sports.auth"})
@ServletComponentScan
@EnableZuulProxy
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableDiscoveryClient
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }
}
