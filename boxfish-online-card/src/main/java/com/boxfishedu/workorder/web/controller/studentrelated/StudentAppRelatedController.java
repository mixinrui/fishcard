package com.boxfishedu.workorder.web.controller.studentrelated;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceX;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/3/31.
 * 主要处理学生App相关的操作
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student")
public class
StudentAppRelatedController {
    @Autowired
    private TimePickerServiceX timePickerServiceX;

    @Autowired
    private AvaliableTimeServiceX avaliableTimeServiceX;
    @Autowired
    private CommonServeServiceX commonServeServiceX;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    /**
     * 学生端批量选择课程的接口
     * TODO:1.获取课程的接口为假数据 2.获取教师的时候需要根据coursetype把外教区分出来,并且体现到workorder和course_schedule的冗余表里
     */
    @RequestMapping(value = "/workorders", method = RequestMethod.POST)
    public JsonResultModel ensureCourseTimes(@RequestBody TimeSlotParam timeSlotParam) {
        JsonResultModel jsonResultModel= timePickerServiceX.ensureCourseTimes(timeSlotParam);
        return jsonResultModel;
    }

    @RequestMapping(value = "{student_Id}/schedule/month", method = RequestMethod.GET)
    public JsonResultModel courseScheduleList(@PathVariable("student_Id") Long studentId,Long userId) {
        commonServeServiceX.checkToken(studentId,userId);
        return timePickerServiceX.getByStudentIdAndDateRange(studentId, DateUtil.createDateRangeForm());
    }


    @RequestMapping(value = "{student_Id}/schedule/page", method = RequestMethod.GET)
    public Object courseSchedulePage(@PathVariable("student_Id") Long studentId, Long userId,
                                              @PageableDefault(
                                                      value = 15,
                                                      sort = {"classDate", "timeSlotId"},
                                                      direction = Sort.Direction.DESC) Pageable pageable) {
        commonServeServiceX.checkToken(studentId,userId);
        return timePickerServiceX.getCourseSchedulePage(studentId, pageable);
    }


    @RequestMapping(value = "time/available", method = RequestMethod.GET)
    public JsonResultModel timeAvailable(AvaliableTimeParam avaliableTimeParam, Long userId) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(userId,userId);
        avaliableTimeParam.setStudentId(userId);
        return avaliableTimeServiceX.getTimeAvailable(avaliableTimeParam);
    }

    @RequestMapping(value = "schedule/finish/page")
    public JsonResultModel getFinishCourseSchedulePage(Long userId, @PageableDefault(
                                                        value = 10,
                                                        sort = {"classDate", "timeSlotId"},
                                                        direction = Sort.Direction.DESC) Pageable pageable) {
        return timePickerServiceX.getFinishCourseSchedulePage(userId, pageable);
    }

    @RequestMapping(value = "schedule/unfinish/page")
    public JsonResultModel getUnFinishCourseSchedulePage(Long userId, @PageableDefault(
                                                        value = 10,
                                                        sort = {"classDate", "timeSlotId"},
                                                        direction = Sort.Direction.DESC) Pageable pageable) {
        return timePickerServiceX.getUnFinishCourseSchedulePage(userId, pageable);
    }
}
