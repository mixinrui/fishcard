package com.boxfishedu.workorder.servicex.assignTeacher;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.common.zookeeper.NullWatcher;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentSchemaJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.WorkOrderLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.boxfishedu.workorder.entity.mysql.StStudentSchema;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.web.param.ScheduleBatchReqSt;
import com.boxfishedu.workorder.web.param.bebase3.ScheduleModelSt;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by olly on 2016/12/15.
 */
public class AssignTeacherServiceX {
    @Autowired
    WorkOrderJpaRepository workOrderJpaRepository;
    @Autowired
    CourseScheduleRepository courseScheduleRepository;
    @Autowired
    StStudentApplyRecordsJpaRepository stStudentApplyRecordsJpaRepository;
    @Autowired
    StStudentSchemaJpaRepository stStudentSchemaJpaRepository;
    @Autowired
    private WorkOrderLogMorphiaRepository workOrderLogMorphiaRepository;
    @Autowired
    private ServiceSDK serviceSDK;
    @Autowired
    private DataCollectorService dataCollectorService;

    /**
     * 是查询鱼卡还是查询课表? 鱼卡和课表的开始时间不一样的是怎么回事?
     *
     *
     * stp1 48小时后所有<未冻结>的课表
     * stp2 指定老师所对应的其他学生的鱼卡
     * stp3 模式表入库
     * stp4 请求师生运营进行教师匹配
     * stp5 根据匹配结果如果匹配成功 更新鱼卡 如果匹配失败不更新 如果无时间片申请表入库
     *
     * @param teacherId 指定老师ID
     * @param studentId 当前学生ID
     */
    @Transactional
    public JsonResultModel doAssignTeacher(Long teacherId, Long studentId){

        StStudentSchema stStudentSchema = stStudentSchemaJpaRepository.findByStudentId(studentId);
        if(null == stStudentSchema){
            stStudentSchema = new StStudentSchema();
            stStudentSchema.setCreateTime(new Date());
            stStudentSchema.setUpdateTime(new Date());
            stStudentSchema.setStSchema(StStudentSchema.StSchema.assgin);
            stStudentSchema.setStudentId(studentId);
            stStudentSchema.setTeacherId(teacherId);
        }else{
            stStudentSchema.setStSchema(StStudentSchema.StSchema.assgin);
            stStudentSchema.setTeacherId(teacherId);
            stStudentSchema.setUpdateTime(new Date());
        }
        stStudentSchemaJpaRepository.save(stStudentSchema);
        Date startTime = DateTime.now().plusHours(48).toDate();
        List<CourseSchedule> aggressorCourseSchedules = courseScheduleRepository.findByStudentIdAndStartTimeAndIsFreezeAndTeacherIdNot(studentId,startTime,0,teacherId);//TODO 发起指定老师的学生的48小时候的课表
        List<Integer> timeslotsList = Collections3.extractToList(aggressorCourseSchedules,"timeSlotId");
        List<Date> classDateList = Collections3.extractToList(aggressorCourseSchedules,"classDate");
        List<CourseSchedule> victimCourseSchedules = courseScheduleRepository.findByTeacherIdAndTimeslotsIdInAndClassDateInAndIsFreeze(teacherId,timeslotsList,classDateList,0);//TODO 当前指定老师的其他学生的课表
        if(Collections3.isNotEmpty(victimCourseSchedules)){
            CourseSchedule courseSchedule = null;
            StStudentSchema stStudentSchemaTmp = null;
            for (Iterator<CourseSchedule> iter = victimCourseSchedules.iterator(); iter.hasNext();) {
                courseSchedule = iter.next();
                stStudentSchemaTmp = stStudentSchemaJpaRepository.findByStudentIdAndTeacherIdAndStSchema(courseSchedule.getStudentId(),courseSchedule.getTeacherId(), StStudentSchema.StSchema.assgin);
                if(null != stStudentSchemaTmp){
                    iter.remove();//把是这个指定老师的排除掉
                }
            }
        }
        ScheduleBatchReqSt scheduleBatchReqSt = match(studentId,teacherId,aggressorCourseSchedules,victimCourseSchedules);
        //TODO 此处去请求师生运营

        ScheduleBatchReqSt responseScheduleBatchReqSt = scheduleBatchReqSt;
        //TODO 此处请求师生运营进行教师重新匹配
        //TODO 分为3中状态 匹配成功直接更新鱼卡和课表 不匹配不更新 无时间片 请求记录入库
        List<ScheduleModelSt> scheduleModelStList = responseScheduleBatchReqSt.getScheduleModelList();
        List<Long> macthedWorkOrderIdList = Lists.newArrayList();
        List<StStudentApplyRecords> stStudentApplyRecordsList = Lists.newArrayList();
        StStudentApplyRecords stStudentApplyRecords = null;
        for(ScheduleModelSt scheduleModelSt : scheduleModelStList){
            if(scheduleModelSt.getMatchStatus() == ScheduleModelSt.MatchStatus.matched){
                macthedWorkOrderIdList.add(scheduleModelSt.getWorkOrderId());
            }else if(scheduleModelSt.getMatchStatus() == ScheduleModelSt.MatchStatus.wait2apply){
                stStudentApplyRecords = new StStudentApplyRecords();
                stStudentApplyRecords.setTeacherId(teacherId);
                stStudentApplyRecords.setStudentId(studentId);
                stStudentApplyRecords.setCreateTime(new Date());
                stStudentApplyRecords.setUpdateTime(new Date());
                stStudentApplyRecords.setApplyStatus(StStudentApplyRecords.ApplyStatus.pending);
                stStudentApplyRecords.setIsRead(StStudentApplyRecords.ReadStatus.no);
                stStudentApplyRecords.setWorkOrderId(scheduleModelSt.getWorkOrderId());
                stStudentApplyRecords.setCourseScheleId(scheduleModelSt.getId());
                stStudentApplyRecordsList.add(stStudentApplyRecords);
            }
        }
        //无时间片 请求记录入库
        if(Collections3.isNotEmpty(stStudentApplyRecordsList)){
            stStudentApplyRecordsJpaRepository.save(stStudentApplyRecordsList);
        }

        if(Collections3.isNotEmpty(macthedWorkOrderIdList)){
            List<WorkOrder> workOrders = workOrderJpaRepository.findWorkOrderAll((Long[]) macthedWorkOrderIdList.toArray());
            List<CourseSchedule> courseSchedules  = courseScheduleRepository.findByWorkorderIdIn((Long[]) macthedWorkOrderIdList.toArray());
            for(WorkOrder workOrder : workOrders){
                workOrder.setTeacherId(teacherId);
                workOrder.setTeacherName(responseScheduleBatchReqSt.getAssginTeacherName());
                workOrder.setAssignTeacherTime(new Date());
            }
            workOrderJpaRepository.save(workOrders);
            for(CourseSchedule courseSchedule : courseSchedules){
                courseSchedule.setTeacherId(teacherId);
                courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
            }
            notifyOthers(workOrders);
            changeTeacherLog(workOrders);
        }
        return JsonResultModel.newJsonResultModel(null);
    }

    /**
     * 组合生成请求
     * @param studentId
     * @param teacherId
     * @param aggressorCourseSchedules
     * @param victimCourseSchedules
     * @return
     */
    private ScheduleBatchReqSt match(Long studentId,Long teacherId,List<CourseSchedule> aggressorCourseSchedules,List<CourseSchedule> victimCourseSchedules){
        ScheduleBatchReqSt scheduleBatchReqSt = new ScheduleBatchReqSt();
        List<ScheduleModelSt> scheduleModelSts = Lists.newArrayList();
        ScheduleModelSt scheduleModelSt = null;
        for(CourseSchedule courseSchedule : aggressorCourseSchedules){
            scheduleModelSt = new ScheduleModelSt(courseSchedule);
            for (CourseSchedule  victimCourseSchedule :victimCourseSchedules){
                if(courseSchedule.getClassDate().compareTo(victimCourseSchedule.getClassDate()) == 0 && courseSchedule.getTimeSlotId() == victimCourseSchedule.getTimeSlotId() ){
                    scheduleModelSt.setGrabedStudentId(victimCourseSchedule.getStudentId());
                    scheduleModelSt.setGrabedId(victimCourseSchedule.getId());
                    scheduleModelSt.setGrabedDay(victimCourseSchedule.getClassDate().getTime());
                    scheduleModelSt.setGrabedcourseType(victimCourseSchedule.getCourseType());
                    scheduleModelSt.setGrabedSlotId(victimCourseSchedule.getTimeSlotId());
                    scheduleModelSt.setGrabedRoleId(victimCourseSchedule.getRoleId());
                    scheduleModelSt.setGrabedWorkOrderId(victimCourseSchedule.getWorkorderId());
                }
            }
            scheduleModelSts.add(scheduleModelSt);
        }
        scheduleBatchReqSt.setUserId(studentId);
        scheduleBatchReqSt.setAssginTeacherId(teacherId);
        scheduleBatchReqSt.setOperateType("auto");
        scheduleBatchReqSt.setScheduleModelList(scheduleModelSts);
        return scheduleBatchReqSt;
    }

    @Async
    private void changeTeacherLog(List<WorkOrder> workOrders){
        List<WorkOrderLog> workOrderLogs = Lists.newArrayList();
        WorkOrderLog workOrderLog = null;
        for(WorkOrder workOrder :workOrders){
            workOrderLog = new WorkOrderLog();
            workOrderLog.setCreateTime(new Date());
            workOrderLog.setWorkOrderId(workOrder.getId());
            workOrderLog.setStatus(workOrder.getStatus());
            workOrderLog.setContent("指定更换教师:" + FishCardStatusEnum.getDesc(workOrder.getStatus()));
            workOrderLogs.add(workOrderLog);
        }
        workOrderLogMorphiaRepository.save(workOrderLog);
    }

    @Async
    private void notifyOthers(List<WorkOrder> workOrders){
        for(WorkOrder workOrder :workOrders){
            dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());
            //通知小马添加新的群组
            serviceSDK.createGroup(workOrder);
        }
    }
}
