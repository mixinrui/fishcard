package com.boxfishedu.workorder.common.util;

import java.lang.reflect.Field;

/**
 * 用于转换vo的常类
 * Created by jiaozijun on 16/7/12.
 */
public class VoUtil {
    /**
     * 复制A的值到对象B中   (前提A的字段>=B的字段)
     *
     * @param A
     * @param B
     * @return
     */
    public static Object reversValueA2B(Object A, Class B) {
        Object oB = null;
        try {
            oB = B.newInstance();
            Class classA = A.getClass();
            Field[] field = classA.getDeclaredFields();

            for(Field f : field){
               if(     f.getClass() .equals("java.lang.String")
                   ||  f.getClass() .equals("java.lang.Long")
                   ||  f.getClass() .equals("java.util.Date")
                   ||  f.getClass() .equals("java.lang.Integer")
                   ||  f.getClass() .equals("java.lang.Short")

                       ){

               }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
