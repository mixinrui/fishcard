package com.boxfishedu.workorder.web.controller.assignteacher;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseInfo;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.boxfishedu.workorder.servicex.studentrelated.AssignTeacherService;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceX;
import com.boxfishedu.workorder.web.param.MakeUpCourseParam;
import com.boxfishedu.workorder.web.param.StTeacherInviteParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    //1 判断指定这位老师上课 按钮是否出现
    // 新增老鱼卡ID oldWorkOrderId
    @RequestMapping(value = "{workorder_Id}/show/assign", method = RequestMethod.GET)
    public JsonResultModel showAssign(@PathVariable("workorder_Id") Long oldWorkOrderId) {
        return assignTeacherService.checkAssignTeacherFlag(oldWorkOrderId);
    }


    //2 获取指定老师带的课程列表  studentId 学生id   assignteacherId 分配老师ID
    @RequestMapping(value = "/assign/teacher/page")
    public JsonResultModel getFinishCourseSchedulePage(Long oldWorkOrderId,
            Long studentId, Long teacherId, @PageableDefault(value = 10, sort = {"classDate", "timeSlotId"},
            direction = Sort.Direction.DESC) Pageable pageable) {
        return assignTeacherService.getAssginTeacherCourseList(oldWorkOrderId,studentId,teacherId,pageable);
    }

    //3 开始上课界面接口
    @RequestMapping(value = "/{workorder_Id}/beginclass/assign", method = RequestMethod.GET)
    public JsonResultModel showBeginClass(@PathVariable("workorder_Id") Long workOrderId){
        return JsonResultModel.newJsonResultModel(  assignTeacherService.getCourseInfo(workOrderId));

    }


    //4 老师端 查看我的上课邀请（提醒）
    @RequestMapping(value = "/{teacher_Id}/invitenum/assign", method = RequestMethod.GET)
    public JsonResultModel getInvitedNum(@PathVariable("teacher_Id")Long teahcerId){
        JSONObject jo = new JSONObject();
        jo.put("unreadnum",assignTeacherService.getMyInvited(teahcerId));
        return JsonResultModel.newJsonResultModel(jo);
    }


    // 5 老师端上课邀请列表
    @RequestMapping(value = "/{teacher_Id}/invitelist/assign", method = RequestMethod.GET)
    public JsonResultModel getInvitedList(@PathVariable("teacher_Id") Long teacherId,@PageableDefault(value = 10, sort = {"applyTime"},
            direction = Sort.Direction.DESC) Pageable pageable){
        JSONObject jo = new JSONObject();
        jo.put("baseHours",48);
        jo.put("results",assignTeacherService.getmyInviteList(teacherId,pageable));
        return JsonResultModel.newJsonResultModel(jo);
    }

    // 6学生的上课邀请列表详情
    @RequestMapping(value = "/{student_id}/invitelist/assign/{teacher_id}", method = RequestMethod.GET)
    public JsonResultModel getInvitedDetailList(@PathVariable("student_id") Long studentId,@PathVariable("teacher_id") Long teacherId ,@PageableDefault(value = 10, sort = {"applyTime"},
            direction = Sort.Direction.DESC) Pageable pageable){

        Page<StStudentApplyRecords> stStudentApplyRecordsList = assignTeacherService.getMyClassesByStudentId(teacherId,studentId,pageable);
        return JsonResultModel.newJsonResultModel(stStudentApplyRecordsList);
    }



    // 7 接受上课邀请接口
    @RequestMapping(value = "/acceptInvite", method = RequestMethod.POST)
    public JsonResultModel acceptInvite(@RequestBody StTeacherInviteParam stTeacherInviteParam){
        return JsonResultModel.newJsonResultModel("OK");
    }

    //8 下单 是否弹出 是否继续指定该老师上课
    @RequestMapping(value = "/{student_Id}/show/assign/first/{sku_id}", method = RequestMethod.GET)
    public JsonResultModel showAssignFirst(@PathVariable("student_Id") Long studentId,@PathVariable("sku_id") Integer sku_id) {

        return JsonResultModel.newJsonResultModel(assignTeacherService.getMyLastAssignTeacher(studentId,sku_id));
    }

}
