package com.boxfishedu.workorder.web.controller.teacherrelated;

import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.TimeLimitPolicy;
import com.boxfishedu.workorder.service.studentrelated.RandomSlotFilterService;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.teacherrelated.TeacherAppRelatedServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;


/**
 * Created by hucl on 16/3/31.
 * 与教师操作相关的操作,主要为ios教师端相关
 */
@CrossOrigin
@RestController
@RequestMapping("/service/teacher")
public class TeacherAppRelatedController {
    @Autowired
    private TeacherAppRelatedServiceX teacherAppRelatedServiceX;
    @Autowired
    private CommonServeServiceX commonServeServiceX;
    @Autowired
    private TimeLimitPolicy timeLimitPolicy;
    @Autowired
    private TeacherStudentRequester teacherStudentRequester;
    @Autowired
    private RandomSlotFilterService randomSlotFilterService;
    private final static DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 评论学生,应该还需要的参数还有评论的内容等,写成dto,方便后续扩展,以及操作者的id
     *
     * @param workOrderId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/evaluation/student/{work_order_id}}", method = RequestMethod.POST)
    public JsonResultModel evaluateForStudent(@PathVariable("work_order_id") Long workOrderId) {
        return null;
    }

    /**
     * 教师端获取一个月的课程表
     * @return 返回带课程标记的课程规划表
     */
    @RequestMapping(value = "{teacher_id}/schedule/month", method = RequestMethod.GET)
    public Object courseScheduleMonth(
            @PathVariable("teacher_id") Long teacherId,
            Long userId,
            Integer count, // 一次返回几个月的数据
            String yearMonth,
            Locale locale) {
        commonServeServiceX.checkToken(teacherId, userId);
        YearMonth yearMonthParam = null;
        if(StringUtils.isNotBlank(yearMonth)) {
            yearMonthParam = YearMonth.from(yearMonthFormatter.parse(yearMonth));
        }
        return teacherAppRelatedServiceX.getScheduleByIdAndDateRange(
                teacherId, yearMonthParam, count, locale);
    }

    @RequestMapping(value = "{teacher_id}/schedule/day", method = RequestMethod.GET)
    public JsonResultModel courseScheduleList(@PathVariable("teacher_id") Long teacherId, Long userId,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                              Locale locale) {
        commonServeServiceX.checkToken(teacherId, userId);
        return teacherAppRelatedServiceX.getScheduleByIdAndDate(teacherId, date, locale);
    }

    //    @Cacheable(value = "teacher_schedule_assigned", key = "T(java.util.Objects).hash(#teacherId,#date)")
    @RequestMapping(value = "{teacher_id}/schedule_assigned/day", method = RequestMethod.GET)
    public JsonResultModel courseScheduleListAssign(@PathVariable("teacher_id") Long teacherId, Long userId,
                                                    @RequestParam(required = false)
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                                    Locale locale) {
        commonServeServiceX.checkToken(teacherId, userId);
        return teacherAppRelatedServiceX.getScheduleAssignedByIdAndDate(teacherId, date, locale);
    }

    @RequestMapping(value = "{teacherId}/timeSlots/template")
    public JsonResultModel getDayTimeSlotsTemplate(@PathVariable Long teacherId, Long userId,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        commonServeServiceX.checkToken(teacherId, userId);
        return JsonResultModel.newJsonResultModel(timeLimitPolicy.limit(teacherStudentRequester.dayTimeSlotsTemplate(teacherId, date)));
    }

    @RequestMapping(value = "international/{teacherId}/timeSlots/template", method = RequestMethod.GET)
    public JsonResultModel getInternationalDayTimeSlotsTemplate(
            @PathVariable Long teacherId, Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(teacherId, userId);
        return teacherAppRelatedServiceX.getInternationalDayTimeSlotsTemplate(teacherId, date);
    }


    @RequestMapping(value = "international/{teacher_id}/schedule/day", method = RequestMethod.GET)
    public JsonResultModel internationalCourseScheduleList(
            @PathVariable("teacher_id") Long teacherId, Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date,
            Locale locale) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(teacherId, userId);
        return teacherAppRelatedServiceX.getInternationalScheduleByIdAndDate(
                teacherId, date, locale);
    }
}
