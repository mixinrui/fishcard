package com.boxfishedu.workorder.common.util;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by hucl on 16/6/3.
 */
public class ClassTypeJudge {
    /**
     * 判断一个类是否为基本数据类型。
     *
     * @param clazz 要判断的类。
     * @return true 表示为基本数据类型。
     */
    public static boolean isBaseDataType(Class clazz) {
        boolean result = true;
        try {
            result = (clazz.equals(String.class) ||
                    clazz.equals(Byte.class) ||
                    clazz.equals(Long.class) ||
                    clazz.equals(Double.class) ||
                    clazz.equals(Float.class) ||
                    clazz.equals(Character.class) ||
                    clazz.equals(Short.class) ||
                    clazz.equals(BigDecimal.class) ||
                    clazz.equals(BigInteger.class) ||
                    clazz.equals(Boolean.class) ||
                    clazz.equals(Date.class) ||
                    clazz.equals(DateTime.class) ||
                    clazz.isPrimitive());
        } catch (Exception ex) {
            System.out.println("不做处理");
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(isBaseDataType(new WorkOrder().getClass()));
    }
}
