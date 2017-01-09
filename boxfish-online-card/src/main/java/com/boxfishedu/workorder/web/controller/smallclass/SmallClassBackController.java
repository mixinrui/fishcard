package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by hucl on 17/1/9.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/backend/smallclass")
public class SmallClassBackController {
    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @RequestMapping(value = "/slot", method = RequestMethod.GET)
    public JsonResultModel publicSlots(String roleId) {
        DayTimeSlots dayTimeSlots=teacherStudentRequester.dayTimeSlotsTemplate(Long.parseLong(roleId));
        List<TimeSlots> timeSlotses=dayTimeSlots.getDailyScheduleTime();
        return JsonResultModel.newJsonResultModel(timeSlotses);
    }

}
