package com.panda.sport.rcs.trade.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author Kwon
 *2022年10月25日14:39:43s
 * 手动实现注入
 */
@Component
public class BeanUtils implements ApplicationContextAware {
    protected  static ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(appContext == null){
            appContext = applicationContext;
        }
    }
    public static Object getBean(String beanName){
        return appContext.getBean(beanName);
    }
    public static Object getBean(Class<?> beanClass){
        return appContext.getBean(beanClass);
    }
}
