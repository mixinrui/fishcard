package com.boxfishedu.workorder.common.util;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.bean.WorkOrderView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

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
            Field[] field = B.getDeclaredFields();

            for(Field f : field){
                String fname = f.getType().getName();
               if(     fname.equals("java.lang.String")
                   ||  fname.equals("java.lang.Long")
                   ||  fname.equals("java.util.Date")
                   ||  fname.equals("java.lang.Integer")
                   ||  fname.equals("java.lang.Short")

                       ){
                   String name =  f.getName();
                   Class types = f.getType();
                   name = name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
                   Method setMethodB = B.getDeclaredMethod("set" + name, types);
                   Method getMethodA = classA.getDeclaredMethod("get" + name, types);

                   setMethodB.invoke(oB, getMethodA.invoke(A) );
               }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return oB;
    }


    public static  void main(String args[]){
        WorkOrder wo1 = new WorkOrder();
        wo1.setTeacherId(3232L);
        wo1.setStartTime(new Date());

        WorkOrderView  wov   =  (WorkOrderView)VoUtil.reversValueA2B(wo1,WorkOrderView.class);
        System.out.println(wov.getTeacherId());
    }
}
