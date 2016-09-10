package com.boxfishedu.card.comment.manage.util;

import org.springframework.context.ApplicationContext;

/**
 * Created by LuoLiBing on 16/5/11.
 */
public class ApplicationContextProvider {

    private static ApplicationContext context;

    private ApplicationContextProvider() {
    }

    public static <T> T getBean(Class<? extends T> clazz) {
        return context != null ? context.getBean(clazz) : null;
    }

    public synchronized static void createInstance(ApplicationContext ctx) {
        if(context == null) {
            context = ctx;
        }
    }

}
