package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hucl on 16/3/19.
 */
public class TeacherType {
    public static Map<String,String> typeMap=new HashMap<>();
    static{
        typeMap.put("1","chinese");
        typeMap.put("4","foreigner");
    }
}
