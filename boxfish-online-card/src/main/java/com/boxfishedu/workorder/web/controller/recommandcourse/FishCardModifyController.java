package com.boxfishedu.workorder.web.controller.recommandcourse;

import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardModifyServiceX;
import com.boxfishedu.workorder.web.param.CourseChangeParam;
import com.boxfishedu.workorder.web.param.TeacherChangeParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/5/10.
 */
@CrossOrigin
@RestController
@RequestMapping("/")
public class FishCardModifyController {
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

    @RequestMapping(value = "/courses/phonics", method = RequestMethod.PUT)
    public JsonResultModel changePhonicsCourses(){
        fishCardModifyServiceX.changePhonicsCourses();
        return JsonResultModel.newJsonResultModel("ok");
    }

}
