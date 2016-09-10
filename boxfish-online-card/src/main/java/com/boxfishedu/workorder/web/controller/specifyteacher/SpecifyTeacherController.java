package com.boxfishedu.workorder.web.controller.specifyteacher;

import com.boxfishedu.workorder.servicex.specifyteacher.SpecifyTeacherServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by hucl on 16/9/9.
 */
public class SpecifyTeacherController {
    @Autowired
    private SpecifyTeacherServiceX specifyTeacherServiceX;

    public JsonResultModel isSpecifyBtnShow(){
        java.util.Map<String,String> map= Maps.newHashMap();
        map.put("isShow",Boolean.TRUE.toString());
        return null;
    }

    public static void main(String[] args) {
        java.util.Map<String,String> map= Maps.newHashMap();
        map.put("isShow",Boolean.TRUE.toString());
        System.out.println(map);
    }
}
