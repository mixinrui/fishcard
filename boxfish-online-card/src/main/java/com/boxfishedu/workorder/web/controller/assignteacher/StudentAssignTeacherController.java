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
    @RequestMapping(value = "{student_Id}/show/assign", method = RequestMethod.GET)
    public JsonResultModel showAssign(@PathVariable("student_Id") Long studentId) {
        return JsonResultModel.newJsonResultModel(true);
    }


    //2 获取指定老师带的课程列表  studentId 学生id   assignteacherId 分配老师ID
    @RequestMapping(value = "/assign/teacher/page")
    public JsonResultModel getFinishCourseSchedulePage(
            Long studentId, Long teacherId, @PageableDefault(value = 10, sort = {"classDate", "timeSlotId"},
            direction = Sort.Direction.DESC) Pageable pageable) {
        return assignTeacherService.getAssginTeacherCourseList(studentId,teacherId,pageable);
    }

    //3 开始上课界面接口
    @RequestMapping(value = "/{workorder_Id}/beginclass/assign", method = RequestMethod.GET)
    public JsonResultModel showBeginClass(@PathVariable("workorder_Id") Long workOrderId){
        CourseInfo c  = new CourseInfo();
        c.setAssignFlag(true);
        c.setWordNum(new Integer(180));
        c.setReadNum(new Integer(264));
        c.setListenNum(new Integer(168));
        c.setTeacherImg("www.baidu.com");
        c.setTeacherName("AssinName");
        return JsonResultModel.newJsonResultModel(c);

    }


    //4 老师端 查看我的上课邀请（提醒）
    @RequestMapping(value = "/{teacher_Id}/invitenum/assign", method = RequestMethod.GET)
    public JsonResultModel getInvitedNum(@PathVariable("teacher_Id")Long teahcerId){
        JSONObject jo = new JSONObject();
        jo.put("unreadnum",3);
        return JsonResultModel.newJsonResultModel(jo);
    }


    // 5 老师端上课邀请列表
    @RequestMapping(value = "/{teacher_Id}/invitelist/assign", method = RequestMethod.GET)
    public JsonResultModel getInvitedList(@PathVariable("teacher_Id") Long teacherId,@PageableDefault(value = 10, sort = {"applyTime"},
            direction = Sort.Direction.DESC) Pageable pageable){

        List<StStudentApplyRecords> stStudentApplyRecordsList = Lists.newArrayList();

        StStudentApplyRecords st = new StStudentApplyRecords();
        st.setStudentId(new Long(3232l));
        st.setTeacherId(new Long(31123l));
        st.setStudentImg("wwww.baidu.com");
        st.setApplyTime(DateUtil.addMinutes(new Date(),190)   );
        st.setCourseNum(3);
        stStudentApplyRecordsList.add(st);

        StStudentApplyRecords st1 = new StStudentApplyRecords();
        st1.setStudentId(new Long(111111l));
        st1.setTeacherId(new Long(323211l));
        st1.setStudentImg("wwww.baidu.com");
        st1.setApplyTime(DateUtil.addMinutes(new Date(),10) );
        st1.setCourseNum(3);
        stStudentApplyRecordsList.add(st1);

        return JsonResultModel.newJsonResultModel(stStudentApplyRecordsList);
    }

    // 6学生的上课邀请列表详情
    @RequestMapping(value = "/{teacher_id}/invitelist/assign/{student_id}", method = RequestMethod.GET)
    public JsonResultModel getInvitedDetailList(@PathVariable("teacher_id") Long teacherId,@PageableDefault(value = 10, sort = {"applyTime"},
            direction = Sort.Direction.DESC) Pageable pageable){

        List<StStudentApplyRecords> stStudentApplyRecordsList = Lists.newArrayList();

        StStudentApplyRecords st = new StStudentApplyRecords();

        st.setWorkOrderId(323L);
        st.setCourseScheleId(32311l);
        st.setStartTime(DateUtil.String2Date("2016-10-10 11:00:00"));
        st.setTimeSlotId(32);
        st.setEndTime(DateUtil.String2Date("2016-10-10 11:25:00"));
        st.setStudentId(new Long(3232l));
        st.setTeacherId(new Long(31123l));
        st.setStudentImg("wwww.baidu.com");
        st.setApplyTime(DateUtil.addMinutes(new Date(),190)   );
        st.setCourseNum(3);
        stStudentApplyRecordsList.add(st);

        StStudentApplyRecords st1 = new StStudentApplyRecords();
        st1.setWorkOrderId(323L);
        st1.setCourseScheleId(32311l);
        st1.setStartTime(DateUtil.String2Date("2016-12-19 11:00:00"));
        st1.setTimeSlotId(32);
        st.setEndTime(DateUtil.String2Date("2016-12-19 11:25:00"));
        st1.setStudentId(new Long(111111l));
        st1.setTeacherId(new Long(323211l));
        st1.setStudentImg("wwww.baidu.com");
        st1.setApplyTime(DateUtil.addMinutes(new Date(),10) );
        st1.setCourseNum(3);
        stStudentApplyRecordsList.add(st1);
        return JsonResultModel.newJsonResultModel(stStudentApplyRecordsList);
    }



    // 7 接受上课邀请接口
    @RequestMapping(value = "/acceptInvite", method = RequestMethod.POST)
    public JsonResultModel acceptInvite(@RequestBody StTeacherInviteParam stTeacherInviteParam){
        return JsonResultModel.newJsonResultModel("OK");
    }

    //8 下单 是否弹出 是否继续指定该老师上课
    @RequestMapping(value = "/{student_Id}/show/assign/first", method = RequestMethod.GET)
    public JsonResultModel showAssignFirst(@PathVariable("student_Id") Long studentId) {
        JSONObject jo = new JSONObject();
        jo.put("teacherId",32323l);
        jo.put("teacherImg","www.baidu.com");
        return JsonResultModel.newJsonResultModel(jo);
    }

}
