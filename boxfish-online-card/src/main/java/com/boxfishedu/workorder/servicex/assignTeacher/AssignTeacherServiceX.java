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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by olly on 2016/12/15.
 */
@Service
public class AssignTeacherServiceX {
    private final static String STUDENT_CHANNLE = "ss_manual";
    private final static String TEACHER_CHANNLE = "st_manual";
    private final static String TIMER_CHANNLE = "auto";
    private final static String MANUAL_OPERATOR ="manual";//OperateType
    private final static String TIMER_OPERATOR ="auto";//OperateType
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
     *
     *
     *
     * 不成功的也会入记录表
     * @param teacherId 指定老师ID
     * @param studentId 当前学生ID
     */

    /**
     * APP端调用
     * @param teacherId
     * @param studentId
     * @param skuId
     * @return
     */
    public JsonResultModel maualAssgin(Long teacherId, Long studentId,Integer skuId){
        Date startTime = DateTime.now().plusHours(48).toDate();
//        Integer skuId = workOrderJpaRepository.findOne(workOrderId).getSkuId();
        List<CourseSchedule> aggressorCourseSchedules = courseScheduleRepository.findByStudentIdAndStartTimeGreaterThanAndIsFreezeAndTeacherIdNot(studentId,startTime,0,teacherId);//TODO 发起指定老师的学生的48小时候的课表
        return doAssignTeacher(teacherId,studentId,aggressorCourseSchedules,STUDENT_CHANNLE,skuId);
    }

    /**
     * 定时任务调用
     * @param teacherId
     * @param studentId
     * @param courseScheleIds
     * @param workOrderIds
     * @return
     */
    public JsonResultModel teacherAccept(Long teacherId,Long studentId,List<Long> courseScheleIds,List<Long> workOrderIds){
        Integer skuId = stStudentSchemaJpaRepository.findByStudentIdAndTeacherId(studentId,teacherId).getSkuId().ordinal();
        List<CourseSchedule> aggressorCourseSchedules = courseScheduleRepository.findByWorkorderIdIn(workOrderIds);
//        Integer skuId = workOrderJpaRepository.findOne(workOrderIds.get(0)).getSkuId();
        return doAssignTeacher(teacherId,studentId,aggressorCourseSchedules,TEACHER_CHANNLE,skuId);
    }
    /**
     *
     * @param teacherId
     * @param studentId
     * @param aggressorCourseSchedules
     * @param channel ss_manual APP端学生手工点击指定老师 auto 系统定时任务出发 st_manual 教师点击接受触发
     * @param skuId 鱼卡ID
     * @return
     */
    @Transactional
    private JsonResultModel doAssignTeacher(Long teacherId, Long studentId,List<CourseSchedule> aggressorCourseSchedules,String channel,Integer skuId){
        StStudentSchema stStudentSchema = stStudentSchemaJpaRepository.findByStudentIdAndTeacherIdAndSkuId(studentId,teacherId, StStudentSchema.CourseType.getEnum(skuId));
        if(null == stStudentSchema){
            stStudentSchema = new StStudentSchema();
            stStudentSchema.setCreateTime(new Date());
            stStudentSchema.setUpdateTime(new Date());
            stStudentSchema.setStSchema(StStudentSchema.StSchema.assgin);
            stStudentSchema.setStudentId(studentId);
            stStudentSchema.setTeacherId(teacherId);
            stStudentSchema.setSkuId(StStudentSchema.CourseType.getEnum(skuId));
        }else{
            stStudentSchema.setStSchema(StStudentSchema.StSchema.assgin);
            stStudentSchema.setTeacherId(teacherId);
            stStudentSchema.setUpdateTime(new Date());
            stStudentSchema.setSkuId(StStudentSchema.CourseType.getEnum(skuId));
        }
        stStudentSchemaJpaRepository.save(stStudentSchema);

        List<Integer> timeslotsList = Collections3.extractToList(aggressorCourseSchedules,"timeSlotId");
        List<Date> classDateList = Collections3.extractToList(aggressorCourseSchedules,"classDate");
        //TODO 查询出不同的类型的课表
        List<CourseSchedule> victimCourseSchedules = courseScheduleRepository.findByTeacherIdAndTimeSlotIdInAndClassDateInAndIsFreezeAndRoleId(teacherId,timeslotsList,classDateList,0,skuId);//TODO 当前指定老师的其他学生的课表
        if(Collections3.isNotEmpty(victimCourseSchedules)){
            CourseSchedule courseSchedule = null;
            StStudentSchema stStudentSchemaTmp = null;
            for (Iterator<CourseSchedule> iter = victimCourseSchedules.iterator(); iter.hasNext();) {
                courseSchedule = iter.next();
                stStudentSchemaTmp = stStudentSchemaJpaRepository.findByStudentIdAndTeacherIdAndSkuIdAndStSchema(courseSchedule.getStudentId(),courseSchedule.getTeacherId(),StStudentSchema.CourseType.getEnum(skuId), StStudentSchema.StSchema.assgin);
                if(null != stStudentSchemaTmp){
                    iter.remove();//把是这个指定老师的排除掉
                }
            }
        }
        ScheduleBatchReqSt scheduleBatchReqSt = match(studentId,teacherId,aggressorCourseSchedules,victimCourseSchedules,channel);
        //TODO 此处去请求师生运营

        ScheduleBatchReqSt responseScheduleBatchReqSt = scheduleBatchReqSt;

        //TODO 此处请求师生运营进行教师重新匹配
        //TODO 分为3中状态 匹配成功直接更新鱼卡和课表 不匹配不更新 无时间片 请求记录入库
        List<ScheduleModelSt> scheduleModelStList = responseScheduleBatchReqSt.getScheduleModelList();

        /** ---------------假数据测试开始-------------------*/
        responseScheduleBatchReqSt.setAssginTeacherName("测试的小王子");
        int num = scheduleModelStList.size()/3;
        int curren = 0;
        for(ScheduleModelSt scheduleModelSt :scheduleModelStList){
            if(curren<=num){
                scheduleModelSt.setMatchStatus(ScheduleModelSt.MatchStatus.matched);
            }
            if(curren<=num*2 && curren>num){
                scheduleModelSt.setMatchStatus(ScheduleModelSt.MatchStatus.un_matched);
            }
            if(curren<=num*3 && curren>num*2){
                scheduleModelSt.setMatchStatus(ScheduleModelSt.MatchStatus.wait2apply);
            }
            curren++;
//
        }
        /** ----------------假数据测试结束------------------*/
        List<Long> macthedWorkOrderIdList = Lists.newArrayList();
        List<ScheduleModelSt> wait2applyWorkOrderIdList = Lists.newArrayList();
        for(ScheduleModelSt scheduleModelSt : scheduleModelStList){
            if(scheduleModelSt.getMatchStatus() == ScheduleModelSt.MatchStatus.matched){
                macthedWorkOrderIdList.add(scheduleModelSt.getWorkOrderId());
            }else{
                wait2applyWorkOrderIdList.add(scheduleModelSt);
            }

//            else if (scheduleModelSt.getMatchStatus() == ScheduleModelSt.MatchStatus.wait2apply){
//                wait2applyWorkOrderIdList.add(scheduleModelSt);
//            }
        }
        if(channel.equals(STUDENT_CHANNLE)){
            makeApplyRecords(teacherId,studentId,wait2applyWorkOrderIdList,skuId);
        }else if(channel.equals(TEACHER_CHANNLE)){
            changeApplyRecords(studentId,teacherId,macthedWorkOrderIdList);
        }else if(channel.equals(TIMER_CHANNLE)){
            makeApplyRecords(teacherId,studentId,wait2applyWorkOrderIdList,skuId);
        }

        if(Collections3.isNotEmpty(macthedWorkOrderIdList)){
            List<WorkOrder> workOrders = workOrderJpaRepository.findWorkOrderAll(macthedWorkOrderIdList);
            List<CourseSchedule> courseSchedules  = courseScheduleRepository.findByWorkorderIdIn(macthedWorkOrderIdList);
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
    private ScheduleBatchReqSt match(Long studentId,Long teacherId,List<CourseSchedule> aggressorCourseSchedules,List<CourseSchedule> victimCourseSchedules,String channel){
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
                    break;
                }
            }
            scheduleModelSts.add(scheduleModelSt);
        }
        scheduleBatchReqSt.setUserId(studentId);
        scheduleBatchReqSt.setAssginTeacherId(teacherId);

        if(channel.equals(STUDENT_CHANNLE)){
            scheduleBatchReqSt.setOperateType(MANUAL_OPERATOR);
        }else if(channel.equals(TEACHER_CHANNLE)){
            scheduleBatchReqSt.setOperateType(MANUAL_OPERATOR);
        }else if(channel.equals(TIMER_CHANNLE)){
            scheduleBatchReqSt.setOperateType(TIMER_OPERATOR);
        }
        scheduleBatchReqSt.setScheduleModelList(scheduleModelSts);
        return scheduleBatchReqSt;
    }

    /**
     *
     * @param teacherId
     * @param studentId
     * @param wait2applyWorkOrderIdList
     */
    private void makeApplyRecords(Long teacherId,Long studentId,List<ScheduleModelSt> wait2applyWorkOrderIdList,Integer skuId){
        StStudentApplyRecords stStudentApplyRecords = null;
        List<StStudentApplyRecords> stStudentApplyRecordsList = Lists.newArrayList();
        Date now = new Date();
        for(ScheduleModelSt scheduleModelSt : wait2applyWorkOrderIdList){
            stStudentApplyRecords = new StStudentApplyRecords();
            stStudentApplyRecords.setTeacherId(teacherId);
            stStudentApplyRecords.setStudentId(studentId);
            stStudentApplyRecords.setApplyTime(now);
            stStudentApplyRecords.setCreateTime(now);
            stStudentApplyRecords.setUpdateTime(now);
            stStudentApplyRecords.setSkuId(skuId);
            stStudentApplyRecords.setValid(StStudentApplyRecords.VALID.yes);
            stStudentApplyRecords.setApplyStatus(StStudentApplyRecords.ApplyStatus.pending);
            stStudentApplyRecords.setIsRead(StStudentApplyRecords.ReadStatus.no);
            stStudentApplyRecords.setWorkOrderId(scheduleModelSt.getWorkOrderId());
            stStudentApplyRecords.setCourseScheleId(scheduleModelSt.getId());
            stStudentApplyRecordsList.add(stStudentApplyRecords);
        }
        //TODO 无时间片 请求记录入库 入库之前,先把之前的申请记录全部作废掉
        if(Collections3.isNotEmpty(stStudentApplyRecordsList)){
            List<StStudentApplyRecords> invalidRecordsList = stStudentApplyRecordsJpaRepository.findByStudentIdAndTeacherIdAndValid(studentId,teacherId, StStudentApplyRecords.VALID.yes);
            if(Collections3.isNotEmpty(invalidRecordsList)){
                for(StStudentApplyRecords studentApplyRecords :invalidRecordsList){
                    studentApplyRecords.setValid(StStudentApplyRecords.VALID.no);
                    studentApplyRecords.setUpdateTime(now);
                }
                stStudentApplyRecordsJpaRepository.save(invalidRecordsList);
            }
            stStudentApplyRecordsJpaRepository.save(stStudentApplyRecordsList);
        }
    }

    /**
     * 更新申请记录
     * @param studentId
     * @param macthedWorkOrderIdList
     */
    private void changeApplyRecords(Long studentId,Long teacherId,List<Long> macthedWorkOrderIdList){
        List<StStudentApplyRecords> invalidRecordsList = stStudentApplyRecordsJpaRepository.findByStudentIdAndTeacherIdAndValid(studentId,teacherId, StStudentApplyRecords.VALID.yes);
        if(Collections3.isNotEmpty(invalidRecordsList)){
            for (StStudentApplyRecords stStudentApplyRecords : invalidRecordsList){
                for(Long workId :macthedWorkOrderIdList){
                    if(workId.equals(stStudentApplyRecords.getWorkOrderId())){
                        stStudentApplyRecords.setUpdateTime(new Date());
                        stStudentApplyRecords.setApplyStatus(StStudentApplyRecords.ApplyStatus.agree);
                        break;
                    }
                }
            }
            stStudentApplyRecordsJpaRepository.save(invalidRecordsList);
        }
    }

    /**
     * 异步 记录日志
     * @param workOrders
     */
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
        workOrderLogMorphiaRepository.save(workOrderLogs);
    }

    /**
     *
     * @param workOrders
     */
    @Async
    private void notifyOthers(List<WorkOrder> workOrders){
        for(WorkOrder workOrder :workOrders){
            dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());
            //通知小马添加新的群组
            serviceSDK.createGroup(workOrder);
        }
    }
}
