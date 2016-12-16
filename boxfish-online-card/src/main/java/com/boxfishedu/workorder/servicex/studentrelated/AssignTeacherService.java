package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.bean.AssignTeacherApplyStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.requester.TeacherPhotoRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.StStudentApplyRecordsService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.param.ScheduleBatchReqSt;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jiaozijun on 16/12/14.
 */
@Component
public class AssignTeacherService {


    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private StStudentApplyRecordsService stStudentApplyRecordsService;

    @Autowired
    private TeacherPhotoRequester teacherPhotoRequester;

    //2 获取指定老师带的课程列表
    public JsonResultModel getAssginTeacherCourseList(Long oldWorkOrderId ,Long studentId, Long teacherId, Pageable pageable) {
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findFinishCourseSchedulePage(studentId, pageable);
        trimPage(courseSchedulePage);
        return JsonResultModel.newJsonResultModel(courseSchedulePage);
    }



    // 2.1 组装向师生运营发送的消息内容   teacherId 指定的老师id
    public ScheduleBatchReqSt makeScheduleBatchReqSt(Long oldWorkOrderId,Long teacherId){
        ScheduleBatchReqSt scheduleBatchReqSt = new ScheduleBatchReqSt();
        List<WorkOrder> workOrders =   this.getAssignTeacherList(oldWorkOrderId);
        if(CollectionUtils.isEmpty(workOrders))
            return null;

        // 获取和该 workOrders 未冻结 开始时间相同 并且为指定老师的鱼卡信息
        List startTimelist = Lists.newArrayList();
        workOrders.forEach(w->{
            startTimelist.add(w.getStartTime());
        });
        List<WorkOrder> workOrdersB = workOrderService.getMatchWorkOrders(teacherId,startTimelist);

        return scheduleBatchReqSt;
    }

    private void trimPage(Page<CourseSchedule> page) {
        ((List<CourseSchedule>) page.getContent()).forEach(courseSchedule -> {
            if (courseSchedule.getId() % 2 == 0) {
                courseSchedule.setMatchStatus(1);
            } else {
                courseSchedule.setMatchStatus(2);
            }
        });
    }


    public JsonResultModel checkAssignTeacherFlag(Long workOrderId){
        List<WorkOrder> workOrders =   this.getAssignTeacherList(workOrderId);
        if(CollectionUtils.isEmpty(workOrders)){
            return JsonResultModel.newJsonResultModel(false);
        }else {
            return JsonResultModel.newJsonResultModel(true);
        }

    }



    public List<WorkOrder> getAssignTeacherList(Long workOrderId){
        StStudentApplyRecords stStudentApplyRecords =   stStudentApplyRecordsService.getStStudentApplyRecordsBy(workOrderId, StStudentApplyRecords.ApplyStatus.agree);
        if(null==stStudentApplyRecords){
            WorkOrder workOrder = workOrderService.findOne(workOrderId);
            if(workOrder==null)
                throw new BusinessException("课程信息有误");
            List<WorkOrder>  listWorkOrders =  workOrderService.findByStartTimeMoreThanAndSkuIdAndIsFreeze(workOrder);
            if(CollectionUtils.isEmpty(listWorkOrders)){
                return null;
            }else {
                return listWorkOrders;
            }
        }else {
            return null;
        }
    }


    //3 开始上课界面接口
    public CourseInfo  getCourseInfo(Long workOrderId){
        //1 获取鱼卡信息
        WorkOrder  workOrder = workOrderService.findOne(workOrderId);
        if(null==workOrder){
            throw new BusinessException("课程信息有误");
        }

        CourseInfo courseInfo = new CourseInfo();
        //2 获取是否本课为指定老师
        StStudentApplyRecords stStudentApplyRecords =   stStudentApplyRecordsService.getStStudentApplyRecordsBy(workOrderId,StStudentApplyRecords.ApplyStatus.agree);
        if(null!=stStudentApplyRecords){
            courseInfo.setAssignFlag(true);// 指定老师
        }else {
            courseInfo.setAssignFlag(false);// 非指定老师
        }



        //3 获取教师信息 老师头像 老师姓名
        if(workOrder.getTeacherId()!=null && workOrder.getTeacherId()>0){
            courseInfo.setTeacherId(workOrder.getTeacherId());   // 设置教师ID
            Map<String ,String> teacherInfoMap =   teacherPhotoRequester.getTeacherInfo(workOrder.getTeacherId());
            courseInfo.setTeacherId(workOrder.getTeacherId());
            if(!CollectionUtils.isEmpty(teacherInfoMap)){
                courseInfo.setTeacherImg(teacherInfoMap.get("figure_url"));// 教师头像url
                courseInfo.setTeacherName(teacherInfoMap.get("nickname")); // 教师姓名
            }

        }


        //4 获取课程信息 词义数 阅读量 听力时长
        if(!StringUtils.isEmpty(workOrder.getCourseId() )){
            courseInfo.setCourseId(workOrder.getCourseId());
            Map<String ,Integer> courseMap =   teacherPhotoRequester.getCourseInfo(workOrder.getCourseId());
            if(!CollectionUtils.isEmpty(courseMap)){
//                private Integer wordNum; //词义数
//                private Integer readNum; //阅读量
//                private Integer listenNum;//积分
                courseInfo.setWordNum(courseMap.get("multiwordCount")); //词义数
                courseInfo.setReadNum(courseMap.get("readWordCount"));//阅读量
                courseInfo.setListenNum(courseMap.get("score"));//积分

            }
        }


        return courseInfo;

    }


    //4 查看我的邀请数量(学生的数量 未读)
    public Integer getMyInvited(Long teacherId){
        Date date = DateUtil.addMinutes(new Date(),-60*24*2);
        return stStudentApplyRecordsService.getUnreadInvitedNum(teacherId,date);
    }

    // 5 老师端上课邀请列表
    public Page<StStudentApplyRecordsResult> getmyInviteList(Long teacherId,Pageable pageable){
        Date now = new Date();
        Date date = DateUtil.addMinutes(now,-60*24*2);
        Page<StStudentApplyRecordsResult>  results =  stStudentApplyRecordsService.getmyInviteList(teacherId,date,pageable);

        if (results == null || null == results.getContent())
           return results;

        ((List<StStudentApplyRecordsResult>) results.getContent()).stream().forEach(rs -> {
            Map<String ,String> teacherInfoMap =teacherPhotoRequester.getTeacherInfo(rs.getStudentId());
            if(!CollectionUtils.isEmpty(teacherInfoMap)){
                rs.setStudentImg( teacherInfoMap.get("figure_url"));
                rs.setStudentName(teacherInfoMap.get("nickname"));
                rs.setSystemTime(now);
            }
        });

        return results;
    }

    // 6 学生的邀请
    public Page<StStudentApplyRecords> getMyClassesByStudentId(Long teacherId,Long studentId, Pageable pageable){
        Date now = new Date();
        Date date = DateUtil.addMinutes(now,-60*24*2);
        Page<StStudentApplyRecords> results =  stStudentApplyRecordsService.getMyClassesByStudentId(teacherId,studentId,date,pageable);
        return results;
    }

    public StStudentApplyRecords getMyLastAssignTeacher(Long studentId){
       return null;// stStudentApplyRecordsService.findMyLastAssignTeacher
    }


}
