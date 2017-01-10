package com.boxfishedu.workorder.web.controller.studentrelated;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.coursenotify.CourseChangeTimeNotifySerceX;
import com.boxfishedu.workorder.servicex.home.HomePageServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceXV1;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceXV1;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionChecker;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionException;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by hucl on 16/3/31.
 * 主要处理学生App相关的操作
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student")
@SuppressWarnings("ALL")
public class
StudentAppRelatedController {
    @Autowired
    private TimePickerServiceX timePickerServiceX;
    @Autowired
    private AvaliableTimeServiceX avaliableTimeServiceX;
    @Autowired
    private CommonServeServiceX commonServeServiceX;
    @Autowired
    private TimePickerServiceXV1 timePickerServiceXV1;
    @Autowired
    private AvaliableTimeServiceXV1 avaliableTimeServiceXV1;
    @Autowired
    private AccountCardInfoService accountCardInfoService;
    @Autowired
    private RepeatedSubmissionChecker checker;
    @Autowired
    private OnlineAccountService onlineAccountService;

    @Autowired
    private HomePageServiceX homePageServiceX;

    @Autowired
    private CourseChangeTimeNotifySerceX courseChangeTimeNotifySerceX;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    /**
     * 学生端批量选择课程的接口
     * 1.获取课程的接口为假数据 2.获取教师的时候需要根据coursetype把外教区分出来,并且体现到workorder和course_schedule的冗余表里
     */
    @RequestMapping(value = "/v1/workorders", method = RequestMethod.POST)
    public JsonResultModel ensureCourseTimesV1(@RequestBody TimeSlotParam timeSlotParam, Long userId) {
        timeSlotParam.setStudentId(userId);

        try {
            if(checker.checkRepeatedSubmission(timeSlotParam.getOrderId())) {
                throw new RepeatedSubmissionException("正在提交当中,请稍候...");
            }
            JsonResultModel jsonResultModel = timePickerServiceXV1.ensureCourseTimes(timeSlotParam);
            checker.evictRepeatedSubmission(timeSlotParam.getOrderId());
            return jsonResultModel;
        } catch (Exception e) {
            evictRepeatedSubmission(e, timeSlotParam.getOrderId());
            throw e;
        }
    }

    /**
     * 学生端批量选择课程的接口(指定老师)
     * 1.获取课程的接口为假数据 2.获取教师的时候需要根据coursetype把外教区分出来,并且体现到workorder和course_schedule的冗余表里
     * @param timeSlotParam
     * @param userId
     * @return
     */
    @RequestMapping(value = "/v11/workorders", method = RequestMethod.POST)
    public JsonResultModel ensureCourseTimesV11(@RequestBody TimeSlotParam timeSlotParam, Long userId) {
        timeSlotParam.setStudentId(userId);

        try {
            if(checker.checkRepeatedSubmission(timeSlotParam.getOrderId())) {
                throw new RepeatedSubmissionException("正在提交当中,请稍候...");
            }
            JsonResultModel jsonResultModel = timePickerServiceXV1.ensureCourseTimesv2(timeSlotParam);
            checker.evictRepeatedSubmission(timeSlotParam.getOrderId());
            return jsonResultModel;
        } catch (Exception e) {
            evictRepeatedSubmission(e, timeSlotParam.getOrderId());
            throw e;
        }
    }


    /**
     * 学生公开课
     * @param studentId
     * @param level
     * @return
     */
    @RequestMapping(value = "{studentId}/schedule/public", method = RequestMethod.GET)
    public JsonResultModel courseSchedulePublic(@PathVariable Long studentId, String level) {
        return JsonResultModel.newJsonResultModel(timePickerServiceXV1.getStudentPublicClassTimeEnum(level));
    }

    @RequestMapping(value = "/{studentId}/enter/publicClassRoom", method = RequestMethod.PUT)
    public JsonResultModel enterClassRoom(
            @PathVariable Long studentId, Long userId, Integer slotId,
            @RequestParam(value = "access_token") String accessToken) {
        if(!studentId.equals(userId)) {
            throw new UnauthorizedException();
        }
        return JsonResultModel.newJsonResultModel(
                timePickerServiceXV1.enterPublicClassRoom(userId, slotId, accessToken));
    }


    @RequestMapping(value = "{student_Id}/schedule/month", method = RequestMethod.GET)
    public JsonResultModel courseScheduleList(
            @PathVariable("student_Id") Long studentId, Long userId, Locale locale) {
        commonServeServiceX.checkToken(studentId, userId);
        return timePickerServiceX.getByStudentIdAndDateRange(
                studentId, DateUtil.createDateRangeForm(), locale);
    }


    @RequestMapping(value = "{student_Id}/schedule/page", method = RequestMethod.GET)
    public Object courseSchedulePage(@PathVariable("student_Id") Long studentId, Long userId,
                                     @PageableDefault(value = 15) Pageable pageable,
                                     Locale locale) {
        commonServeServiceX.checkToken(studentId, userId);
        return timePickerServiceX.getCourseSchedulePage(studentId, pageable, locale);
    }

    /**
     * 获取延迟上课的周数
     * @param userId
     * @return
     */
    @RequestMapping(value = "/v1/delay/week/class" ,method = RequestMethod.GET)
    public JsonResultModel getDelayClassWeeks(Long userId)throws  Exception{
        return avaliableTimeServiceXV1.getDelayWeekDays();
    }

    /**
     * 小班课 延迟选时间
     * @param userId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/v1/delay/week/smallclass" ,method = RequestMethod.GET)
    public JsonResultModel getDelaySmallClassWeeks(Long userId)throws  Exception{
        return avaliableTimeServiceXV1.getDelayWeekDaysForSmallClass();
    }

    @RequestMapping(value = "/v1/time/available", method = RequestMethod.GET)
    public JsonResultModel timeAvailableV1(AvaliableTimeParam avaliableTimeParam, Long userId) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(avaliableTimeParam.getStudentId(), userId);
        if(null != avaliableTimeParam.getStudentId())
            avaliableTimeParam.setStudentId(userId);
        return avaliableTimeServiceXV1.getTimeAvailable(avaliableTimeParam);
    }



    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public JsonResultModel test(AvaliableTimeParam avaliableTimeParam, Long userId) throws CloneNotSupportedException {

        Map<String,Integer> querData =Maps.newHashMap();

        for(int i=0;i<100;i++){
            JsonResultModel jsonResultModel =  avaliableTimeServiceXV1.getTimeAvailable(avaliableTimeParam);

            List<DayTimeSlots> list = (List<DayTimeSlots>)jsonResultModel.getData();
            for(DayTimeSlots dts : list){
                final String day = dts.getDay();
                dts.getDailyScheduleTime().stream().forEach(timeSlots -> {
                    this.putValue(querData,day + " " + timeSlots.getSlotId() );
                });

            }
        }

        return JsonResultModel.newJsonResultModel(querData);
    }


    private  void putValue(Map<String,Integer> map,String key){
        if(null !=map.get(key)){
            map.put(key,(Integer)map.get(key)+1);
        }else {
            map.put(key,1);
        }
    }
    @RequestMapping(value = "schedule/finish/page")
    public JsonResultModel getFinishCourseSchedulePage(
            Long userId, @PageableDefault(value = 10, sort = {"classDate", "timeSlotId"},
            direction = Sort.Direction.DESC) Pageable pageable,
            Locale locale) {
        return timePickerServiceX.getFinishCourseSchedulePage(userId, pageable, locale);
    }

    @RequestMapping(value = "schedule/unfinish/page")
    public JsonResultModel getUnFinishCourseSchedulePage(
            Long userId,
            @PageableDefault(value = 10, sort = {"classDate", "timeSlotId"},
                    direction = Sort.Direction.DESC) Pageable pageable,
            Locale locale) {
        return timePickerServiceX.getUnFinishCourseSchedulePage(userId, pageable, locale);
    }


    /***********************
     * 兼容历史版本
     ***************************/
    @RequestMapping(value = "/workorders", method = RequestMethod.POST)
    public JsonResultModel ensureCourseTimes(@RequestBody TimeSlotParam timeSlotParam, Long userId) {
        timeSlotParam.setStudentId(userId);
        try {
            if (checker.checkRepeatedSubmission(timeSlotParam.getOrderId())) {
                throw new RepeatedSubmissionException("正在提交当中,请稍候...");
            }
            JsonResultModel jsonResultModel = timePickerServiceX.ensureCourseTimes(timeSlotParam);
            checker.evictRepeatedSubmission(timeSlotParam.getOrderId());
            return jsonResultModel;
        } catch (Exception e) {
            evictRepeatedSubmission(e, timeSlotParam.getOrderId());
            throw e;
        }
    }

    @RequestMapping(value = "/time/available", method = RequestMethod.GET)
    public JsonResultModel timeAvailable(AvaliableTimeParam avaliableTimeParam, Long userId) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(avaliableTimeParam.getStudentId(), userId);
        avaliableTimeParam.setStudentId(userId);
        return avaliableTimeServiceX.getTimeAvailable(avaliableTimeParam);
    }

    @RequestMapping(value = "/{student_id}/card_infos", method = RequestMethod.PUT)
    public JsonResultModel userCardInfoModify(String order_type,@PathVariable("student_id") Long studentId) {
        AccountCardInfo accountCardInfo=accountCardInfoService.queryByStudentId(studentId);
        AccountCourseBean.CardCourseInfo cardCourseInfo = accountCardInfo.getChinese().getCourseInfo();
        cardCourseInfo.setCourseName("@@@@@@@中文课程名称修改2222");
        accountCardInfoService.save(accountCardInfo);
        return JsonResultModel.newJsonResultModel(accountCardInfo);
    }

    @RequestMapping(value = "/{student_id}/card_infos", method = RequestMethod.GET)
    public JsonResultModel userCardInfo(String order_type,@PathVariable("student_id") Long studentId) {
        return homePageServiceX.getHomePage(order_type, studentId);
    }

    private void evictRepeatedSubmission(Exception e, Long orderId) {
        if(!(e instanceof RepeatedSubmissionException)) {
            checker.evictRepeatedSubmission(orderId);
        }
    }


    /**
     * app课程列表 获取提醒内容(1 提示学生修改时间)
     * @param studentId
     * @param userId
     * @return
     */
    @RequestMapping(value = "{student_Id}/schedule/page/notice", method = RequestMethod.GET)
    public Object courseSchedulePage(@PathVariable("student_Id") Long studentId, Long userId) {
        commonServeServiceX.checkToken(studentId, userId);
        return courseChangeTimeNotifySerceX.getNotifyByStudentId(studentId);
    }

}
