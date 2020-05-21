package com.wp.casino.messageserver.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationContextProvider implements ApplicationContextAware {


    private static ApplicationContext applicationContext;

    @Override

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        if(ApplicationContextProvider.applicationContext == null) {

            ApplicationContextProvider.applicationContext = applicationContext;

        }

        log.info("ApplicationContext："+ApplicationContextProvider.applicationContext);

    }

    public static ApplicationContext getApplicationContext() {

        return applicationContext;

    }

    public static Object getBean(String name) {

        return getApplicationContext().getBean(name);

    }

    public static <T> T getBean(Class<T> clazz) {

        return getApplicationContext().getBean(clazz);

    }

    public static <T> T getBean(String name,Class<T> clazz) {

        return getApplicationContext().getBean(name,clazz);

    }


}
