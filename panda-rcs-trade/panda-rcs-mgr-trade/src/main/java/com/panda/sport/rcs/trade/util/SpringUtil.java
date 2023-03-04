package com.panda.sport.rcs.trade.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.util
 * @Description :  TODO
 * @Date: 2020-08-16 15:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    // 通过class获取Bean
    public static <T> T getBean(Class<T> clz) {
        return context.getBean(clz);
    }

    // 通过name获取Bean
    public static <T> T getBean(String name) {
        return (T) context.getBean(name);
    }

    // 通过name,class返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    public static String getProperty(String key) {
        return context.getEnvironment().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return context.getEnvironment().getProperty(key, defaultValue);
    }

    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return context.getEnvironment().getProperty(key, targetType, defaultValue);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        SpringUtil.context = context;
    }
}
