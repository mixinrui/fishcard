package com.boxfishedu.workorder.common.config;

import org.springframework.context.ApplicationContext;

/**
 * Created by LuoLiBing on 16/10/19.
 */
public class ApplicationContextAccessor {

    private static ApplicationContext context;

    public synchronized static void setContext(ApplicationContext _context) {
        if(context == null && _context != null) {
            context = _context;
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }
}
