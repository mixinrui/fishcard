package com.boxfishedu.workorder.web.view.course;

/**
 * Created by LuoLiBing on 16/8/17.
 */
@FunctionalInterface
public interface Bi3Function<T, S, U, R> {

    R apply(T t, S s, U u);
}
