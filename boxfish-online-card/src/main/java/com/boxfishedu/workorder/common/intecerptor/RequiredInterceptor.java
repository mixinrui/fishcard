package com.boxfishedu.workorder.common.intecerptor;

import java.lang.annotation.*;

/**
 * Created by hucl on 16/6/3.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public abstract @interface RequiredInterceptor{
    boolean required() default true;
}
