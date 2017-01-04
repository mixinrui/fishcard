package com.boxfishedu.workorder.web.controller.assignteacher;

import com.boxfishedu.workorder.servicex.assignTeacher.AssignTeacherServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by olly on 2017/1/4.
 */
@RestController
@RequestMapping("/rest/job")
public class AssignTeacherPoint {
    @Autowired
    AssignTeacherServiceX assignTeacherServiceX;

    /**
     * 指定老师定时任务后门
     * @return
     */
    @RequestMapping("/assign/timer")
    public JsonResultModel assign_timer(){
        assignTeacherServiceX.autoAssign();
        return JsonResultModel.newJsonResultModel(null);
    }
}
