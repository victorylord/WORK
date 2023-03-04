package com.panda.sport.rcs.trade.config;

import com.panda.sport.rcs.log.interceptors.LogInterceptor;
import com.panda.sport.rcs.monitor.mq.SendMqApi;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@ConditionalOnBean(name = "sendMqApi")
@ConditionalOnProperty("log.operate.switch")
@Configuration
@Slf4j
public class WebAppConfig extends WebMvcConfigurationSupport {

    @Autowired
    private SendMqApi sendMqApi;

    @Autowired
    private Environment env;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将所有/static/** 访问都映射到classpath:/static/ 目录下
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
        // swagger2
        registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LogInterceptor interceptor = new LogInterceptor((topic, tag, key, msg) ->  {
            sendMqApi.execute(topic, tag, key, msg);
        });
        registry.addInterceptor(interceptor).addPathPatterns("/**");
        if("1".equals(env.getProperty("rcs.sys.auth"))) {//开启权限
            try {
                HandlerInterceptor authInterceptor = SpringContextUtils.getBean("authInterceptorBean");
                if(authInterceptor == null) {
                    log.warn("没有找到对应权限的拦截器类！");
                    return;
                }
                registry.addInterceptor(authInterceptor).addPathPatterns("/**");
            }catch (Exception e) {
                log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            }
        }else {
            log.info("当前系统不开启权限！");
        }
    }
}
