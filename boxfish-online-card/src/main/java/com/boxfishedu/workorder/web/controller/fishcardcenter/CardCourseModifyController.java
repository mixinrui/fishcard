package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.param.CourseChangeParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardModifyServiceX;
import com.boxfishedu.workorder.web.param.TeacherChangeParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by hucl on 16/5/10.
 * 供内部接口更换课程
 */
@CrossOrigin
@RestController
@RequestMapping("/card/modify")
public class CardCourseModifyController {
    @Autowired
    private FishCardModifyServiceX fishCardModifyServiceX;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/teacher", method = RequestMethod.PUT)
    public JsonResultModel changeTeacher(@RequestBody TeacherChangeParam teacherChangeParam) {
        return fishCardModifyServiceX.changeTeacher(teacherChangeParam);
    }

    @RequestMapping(value = "/courses/order", method = RequestMethod.PUT)
    public JsonResultModel changeSpecialOrderCourses(@RequestBody CourseChangeParam courseChangeParam) {
        fishCardModifyServiceX.changeSpecialOrderCourses(courseChangeParam.getStudentId(),courseChangeParam.getOrderId());
        return JsonResultModel.newJsonResultModel("ok");
    }

    @RequestMapping(value = "/courses/all", method = RequestMethod.PUT)
    public JsonResultModel changeOrderCourses(@RequestBody CourseChangeParam courseChangeParam) {
        fishCardModifyServiceX.changerderCourses(courseChangeParam.getStudentId());
        return JsonResultModel.newJsonResultModel("ok");
    }

    //修改鱼卡的课程
    @RequestMapping(value = "/course", method = RequestMethod.PUT)
    public JsonResultModel changeCourse(@RequestBody CourseChangeParam courseChangeParam){
        fishCardModifyServiceX.changCourse(courseChangeParam.getWorkOrderId());
        return JsonResultModel.newJsonResultModel("ok");
    }

    //修改鱼卡的课程,参数为一批列表
    @RequestMapping(value = "/courses", method = RequestMethod.PUT)
    public JsonResultModel changeCourses(@RequestBody CourseChangeParam courseChangeParam){
        courseChangeParam.getWorkOrderIds().forEach(workOrderId->{
            fishCardModifyServiceX.changCourse(workOrderId);
        });
        return JsonResultModel.newJsonResultModel("ok");
    }

}
