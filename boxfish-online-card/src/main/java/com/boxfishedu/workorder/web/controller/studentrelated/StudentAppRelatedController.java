package com.boxfishedu.workorder.web.controller.studentrelated;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AvaliableTimeServiceXV1;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceXV1;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
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
    @Autowired
    private TimePickerServiceXV1 timePickerServiceXV1;
    @Autowired
    private AvaliableTimeServiceXV1 avaliableTimeServiceXV1;
    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    /**
     * 学生端批量选择课程的接口
     * TODO:1.获取课程的接口为假数据 2.获取教师的时候需要根据coursetype把外教区分出来,并且体现到workorder和course_schedule的冗余表里
     */
    @RequestMapping(value = "/v1/workorders", method = RequestMethod.POST)
    public JsonResultModel ensureCourseTimesV1(@RequestBody TimeSlotParam timeSlotParam, Long userId) {
        timeSlotParam.setStudentId(userId);
        JsonResultModel jsonResultModel= timePickerServiceXV1.ensureCourseTimes(timeSlotParam);
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


    @RequestMapping(value = "/v1/time/available", method = RequestMethod.GET)
    public JsonResultModel timeAvailableV1(AvaliableTimeParam avaliableTimeParam, Long userId) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(avaliableTimeParam.getStudentId(),userId);
        avaliableTimeParam.setStudentId(userId);
        return avaliableTimeServiceXV1.getTimeAvailable(avaliableTimeParam);
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


    /***********************兼容历史版本***************************/
    @RequestMapping(value = "/workorders", method = RequestMethod.POST)
    public JsonResultModel ensureCourseTimes(@RequestBody TimeSlotParam timeSlotParam, Long userId) {
        timeSlotParam.setStudentId(userId);
        JsonResultModel jsonResultModel= timePickerServiceX.ensureCourseTimes(timeSlotParam);
        return jsonResultModel;
    }

    @RequestMapping(value = "/time/available", method = RequestMethod.GET)
    public JsonResultModel timeAvailable(AvaliableTimeParam avaliableTimeParam, Long userId) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(avaliableTimeParam.getStudentId(),userId);
        avaliableTimeParam.setStudentId(userId);
        return avaliableTimeServiceX.getTimeAvailable(avaliableTimeParam);
    }

    @RequestMapping(value = "/{student_id}/card_infos", method = RequestMethod.GET)
    public JsonResultModel userCardInfo(){
        AccountCardInfo accountCardInfo=new AccountCardInfo();

        AccountCourseBean.CardCourseInfo cardCourseInfo=new AccountCourseBean.CardCourseInfo();
        cardCourseInfo.setCourseName("中文课程");
        cardCourseInfo.setCourseId("ARRRRRRYYYYYYYAYAYYAYYAYAYAA--A4444444444444YYYYYYY" );
        cardCourseInfo.setCourseType("PHONICS");
        cardCourseInfo.setDifficulty(1);
        cardCourseInfo.setThumbnail("http://wwww.baidu.com/1.jpg");
        AccountCourseBean accountCourseBean=new AccountCourseBean();
        accountCourseBean.setLeftAmount(7);
        accountCourseBean.setCourseInfo(cardCourseInfo);

        accountCardInfo.setChinese(accountCourseBean);

        AccountCourseBean.CardCourseInfo cardCourseInfo2=new AccountCourseBean.CardCourseInfo();
        cardCourseInfo2.setCourseName("外教课程");
        cardCourseInfo2.setCourseId("ARRRRRRYYYYYYYAYAYYAYYAYAYAA--A4444444444444YYYYYYY" );
        cardCourseInfo2.setCourseType("PHONICS");
        cardCourseInfo2.setDifficulty(1);
        cardCourseInfo2.setThumbnail("http://wwww.baidu.com/1.jpg");
        AccountCourseBean accountCourseBean2=new AccountCourseBean();
        accountCourseBean2.setLeftAmount(7);
        accountCourseBean2.setCourseInfo(cardCourseInfo2);

        accountCardInfo.setForeign(accountCourseBean2);


        AccountCourseBean.CardCourseInfo cardCourseInfo3=new AccountCourseBean.CardCourseInfo();
        cardCourseInfo3.setCourseName("外教点评");
        cardCourseInfo3.setCourseId("ARRRRRRYYYYYYYAYAYYAYYAYAYAA--A4444444444444YYYYYYY" );
        cardCourseInfo3.setCourseType("PHONICS");
        cardCourseInfo3.setDifficulty(1);
        cardCourseInfo3.setThumbnail("http://wwww.baidu.com/1.jpg");
        AccountCourseBean accountCourseBean3=new AccountCourseBean();
        accountCourseBean3.setLeftAmount(7);
        accountCourseBean3.setCourseInfo(cardCourseInfo3);

        accountCardInfo.setForeign(accountCourseBean3);

        return JsonResultModel.newJsonResultModel(accountCardInfo);
    }


}
