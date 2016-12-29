package com.boxfishedu.workorder.web.controller.assignteacher;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseInfo;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.boxfishedu.workorder.servicex.studentrelated.AssignTeacherService;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionAssignTeacherChecker;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionChecker;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionException;
import com.boxfishedu.workorder.web.param.MakeUpCourseParam;
import com.boxfishedu.workorder.web.param.StTeacherInviteParam;
import com.boxfishedu.workorder.web.param.StudentTeacherParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 学生指定老师上课
 * Created by jiaozijun on 16/12/14.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student")
@SuppressWarnings("ALL")
public class StudentAssignTeacherController {

    @Autowired
    private TimePickerServiceX timePickerServiceX;

    @Autowired
    private AssignTeacherService assignTeacherService;

    @Autowired
    private RepeatedSubmissionAssignTeacherChecker checker;

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "{teacherId}/test", method = RequestMethod.GET)
    public JsonResultModel test(@PathVariable("teacherId") Long teacherId) {
         assignTeacherService.pushTeacherList(teacherId);
        return JsonResultModel.newJsonResultModel(null);
    }


    //1 判断指定这位老师上课 按钮是否出现
    // 新增老鱼卡ID oldWorkOrderId
    @RequestMapping(value = "{workorder_Id}/show/assign", method = RequestMethod.GET)
    public JsonResultModel showAssign(@PathVariable("workorder_Id") Long oldWorkOrderId) {
        logger.info("show_assign :oldWorkOrderId [{}]",oldWorkOrderId);
        return assignTeacherService.checkAssignTeacherFlag(oldWorkOrderId);
    }


    //2.1 指定老师上课按钮(上完课) *****sku_id  *****
    @RequestMapping(value = "/assign/teacher/act", method = RequestMethod.GET)
    public JsonResultModel assignTeacherAct (Long oldWorkOrderId,Integer sku_id,
                                                Long studentId, Long teacherId, Long userId) {
        studentId = userId;
        logger.info("assign_teacher_act :oldWorkOrderId [{}] skuId [{}] teacherId [{}] userId [{}]",oldWorkOrderId,sku_id,teacherId,userId);
        if(checker.checkRepeatedSubmission(oldWorkOrderId)) {
            throw new RepeatedSubmissionException("正在提交当中,请稍候...");
        }

        // 验证重复提交问题
        JsonResultModel  jsonResultModel = assignTeacherService.matchCourseInfoAssignTeacher(oldWorkOrderId,sku_id, studentId,teacherId);

        checker.evictRepeatedSubmission(oldWorkOrderId);
        return JsonResultModel.newJsonResultModel("OK");
    }


    //2 获取指定老师带的课程列表  studentId 学生id   assignteacherId 分配老师ID
    @RequestMapping(value = "/assign/teacher/page")
    public JsonResultModel getAssignTeacherCourseSchedulePage(Long oldWorkOrderId,
            Long studentId, Long teacherId, @PageableDefault(value = 10, sort = {"classDate", "timeSlotId"},
            direction = Sort.Direction.ASC) Pageable pageable,Long userId) {
        studentId = userId;
        return assignTeacherService.getAssginTeacherCourseList(oldWorkOrderId,studentId,teacherId,pageable);
    }

    //2.2 获取指定老师带的课程列表  studentId 学生id   assignteacherId 分配老师ID
    @RequestMapping(value = "/assign/teacher/newpage")
    public JsonResultModel getAssignTeacherCourseSchedulePageNew(Long oldWorkOrderId,
                                                              Long studentId, Long teacherId,Long orderId, @PageableDefault(value = 10, sort = {"classDate", "timeSlotId"},
            direction = Sort.Direction.ASC) Pageable pageable,Long userId) {
        logger.info("assign_teacher_newpage :oldWorkOrderId [{}] studentId [{}] teacherId [{}] userId [{}]",oldWorkOrderId,studentId,teacherId,userId);
        studentId = userId;
        return assignTeacherService.getAssginTeacherCourseListnew(oldWorkOrderId,studentId,teacherId,orderId,pageable);
    }

    //3 开始上课界面接口
    @RequestMapping(value = "/{workorder_Id}/beginclass/assign", method = RequestMethod.GET)
    public JsonResultModel showBeginClass(@PathVariable("workorder_Id") Long workOrderId){
        return JsonResultModel.newJsonResultModel(  assignTeacherService.getCourseInfo(workOrderId));

    }


    //4 老师端 查看我的上课邀请（提醒）
    @RequestMapping(value = "/{teacher_Id}/invitenum/assign", method = RequestMethod.GET)
    public JsonResultModel getInvitedNum(@PathVariable("teacher_Id")Long teacherId,Long userId){
        teacherId = userId;
        JSONObject jo = new JSONObject();
        jo.put("unreadnum",assignTeacherService.getMyInvited(teacherId));
        return JsonResultModel.newJsonResultModel(jo);
    }


    // 5 老师端上课邀请列表
    @RequestMapping(value = "/{teacher_Id}/invitelist/assign", method = RequestMethod.GET)
    public JsonResultModel getInvitedList(@PathVariable("teacher_Id") Long teacherId,@PageableDefault(value = 10, sort = {"applyTime"},
            direction = Sort.Direction.DESC) Pageable pageable,Long userId){
        teacherId = userId;
        JSONObject jo = new JSONObject();
        jo.put("baseHours",48);
        jo.put("results",assignTeacherService.getmyInviteList(teacherId,pageable));
        return JsonResultModel.newJsonResultModel(jo);
    }

    // 6老师端获取学生的上课邀请列表详情
    @RequestMapping(value = "/{student_id}/invitelist/assign/{teacher_id}", method = RequestMethod.GET)
    public JsonResultModel getInvitedDetailList(@PathVariable("student_id") Long studentId,@PathVariable("teacher_id") Long teacherId ,@PageableDefault(value = 10) Pageable pageable,Long userId){
        teacherId = userId;
        Page<StStudentApplyRecords> stStudentApplyRecordsList = assignTeacherService.getMyClassesByStudentId(teacherId,studentId,pageable);
        return JsonResultModel.newJsonResultModel(stStudentApplyRecordsList);
    }



    // 7 接受上课邀请接口
    @RequestMapping(value = "/acceptInvite", method = RequestMethod.POST)
    public JsonResultModel acceptInvite(@RequestBody StTeacherInviteParam stTeacherInviteParam,Long userId){
        if(null==userId)
            throw new BusinessException("非法反问被拒绝");
        return assignTeacherService.acceptInvitedCourseByStudentId(stTeacherInviteParam,userId);
    }

    //8 下单 是否弹出 是否继续指定该老师上课
    @RequestMapping(value = "/{student_Id}/show/assign/first/{sku_id}", method = RequestMethod.GET)
    public JsonResultModel showAssignFirst(@PathVariable("student_Id") Long studentId,@PathVariable("sku_id") Integer sku_id,Long userId) {
        studentId = userId;
        return JsonResultModel.newJsonResultModel(assignTeacherService.getMyLastAssignTeacher(studentId,sku_id));
    }

    //9 换个老师接口
    @RequestMapping(value = "/neworder/changeTeacher", method = RequestMethod.POST)
    public JsonResultModel showAssignFirst(@RequestBody StudentTeacherParam studentTeacherParam,Long userId) {
        studentTeacherParam.setStudentId(userId);
        return  assignTeacherService.changeATeacher(studentTeacherParam);
    }

    //10 在 StudentAppRelatedController ensureCourseTimesV11


    //11老师端获取学生的上课邀请列表详情
    @RequestMapping(value = "/{student_id}/invitelist/assign/{teacher_id}/check", method = RequestMethod.GET)
    public JsonResultModel getInvitedDetaiCheck(@PathVariable("student_id") Long studentId,@PathVariable("teacher_id") Long teacherId ,Long userId){
        teacherId = userId;
        List<StStudentApplyRecords> stStudentApplyRecordsList = assignTeacherService.checkMyClassesByStudentId(teacherId,studentId);
        if(CollectionUtils.isEmpty(stStudentApplyRecordsList)){
            return JsonResultModel.newJsonResultModel(true);
        }
        return JsonResultModel.newJsonResultModel(true);
    }






}
