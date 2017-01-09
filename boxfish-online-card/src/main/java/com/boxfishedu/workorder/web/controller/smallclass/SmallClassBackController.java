package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/slot", method = RequestMethod.GET)
    public JsonResultModel publicSlots(String roleId) {
        DayTimeSlots dayTimeSlots = teacherStudentRequester.dayTimeSlotsTemplate(Long.parseLong(roleId));
        List<TimeSlots> timeSlotses = dayTimeSlots.getDailyScheduleTime();
        return JsonResultModel.newJsonResultModel(timeSlotses);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public JsonResultModel buildPublicClass(PublicClassBuilderParam publicClassBuilderParam) {
        logger.debug("@buildPublicClass创建公开课,参数[{}]", publicClassBuilderParam);
        return JsonResultModel.newJsonResultModel("OK");
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public JsonResultModel updatePublicClass(PublicClassBuilderParam publicClassBuilderParam) {
        logger.debug("@updatePublicClass更新公开课,参数[{}]", publicClassBuilderParam);
        return JsonResultModel.newJsonResultModel("OK");
    }

    @RequestMapping(value = "/status", method = RequestMethod.POST)
    static class PublicClassBuilderParam {
        private Long smallClassId;
        private String date;
        private Long slotId;
        private String difficulty;
        private Long teacherId;
    }


}
