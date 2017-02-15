package com.boxfishedu.workorder.common.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Created by hucl on 17/2/15.
 * 主要给配置文件用,所以没做泛型
 */
public class ReflectUtil {

    public static String getValue(Object obj, String propertyName) throws Exception {
        boolean len = true;

        // 获取实体类的所有属性，返回Field数组
        Field[] field = obj.getClass().getDeclaredFields();
        // 遍历所有属性
        for (int j = 0; j < field.length; j++) {
            // 获取属性的名字
            String name = field[j].getName();
            if (Objects.equals(propertyName, name)) {
                // 将属性的首字符大写，方便构造get，set方法
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                Method m = obj.getClass().getMethod("get" + name);
                // 调用getter方法获取属性值
                String value = (String) m.invoke(obj);
                if (value != null) {
                    return value;
                } else {
                    return null;
                }
            } else {
                continue;
            }
        }
        return null;
    }

    public static void setValue(Object object, String propertyName, String value) throws Exception {
        Class tClass = object.getClass();
        //获得该类的所有属性
        Field[] fields = tClass.getDeclaredFields();
        for (Field field : fields) {
            if (Objects.equals(propertyName, field.getName())) {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), tClass);
                //获得set方法
                Method method = pd.getWriteMethod();
                method.invoke(object, value);
            }
        }
    }


}
