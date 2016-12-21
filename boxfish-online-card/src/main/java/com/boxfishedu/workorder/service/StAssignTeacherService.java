package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentSchemaJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.WorkOrderLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.boxfishedu.workorder.entity.mysql.StStudentSchema;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.servicex.assignTeacher.RemoteService;
import com.boxfishedu.workorder.web.param.ScheduleBatchReqSt;
import com.boxfishedu.workorder.web.param.bebase3.ScheduleModelSt;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by olly on 2016/12/20.
 */
@Component
public class StAssignTeacherService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    WorkOrderJpaRepository workOrderJpaRepository;
    @Autowired
    CourseScheduleRepository courseScheduleRepository;
    @Autowired
    StStudentApplyRecordsJpaRepository stStudentApplyRecordsJpaRepository;
    @Autowired
    StStudentSchemaJpaRepository stStudentSchemaJpaRepository;
    @Autowired
    WorkOrderLogMorphiaRepository workOrderLogMorphiaRepository;
    @Autowired
    ServiceSDK serviceSDK;
    @Autowired
    DataCollectorService dataCollectorService;
    @Autowired
    RemoteService remoteService;

    @Transactional
    public void doAssignTeacher(Long teacherId, Long studentId, List<CourseSchedule> aggressorCourseSchedules, String channel, Integer skuId){
        StStudentSchema stStudentSchema = checkSchema(studentId,teacherId,skuId);
        List<Integer> timeslotsList = Collections3.extractToList(aggressorCourseSchedules,"timeSlotId");
        List<Date> classDateList = Collections3.extractToList(aggressorCourseSchedules,"classDate");
        //TODO 查询出不同的类型的课表
        List<CourseSchedule> victimCourseSchedules = courseScheduleRepository.findByTeacherIdAndTimeSlotIdInAndClassDateInAndIsFreezeAndRoleId(teacherId,timeslotsList,classDateList,0,skuId);//TODO 当前指定老师的其他学生的课表
        logger.info("指定老师stp-2::排除相同指定老师课表开始::::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>老师其他学生的鱼卡IDS:{}===>>>总共{}条",studentId,teacherId,skuId,stStudentSchema.getTeacherId(),Collections3.extractToList(victimCourseSchedules,"workorderId").toArray(),victimCourseSchedules.size());
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
            logger.info("指定老师stp-2:::排除相同指定老师课表结束:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>(排除那些也指定过这个老师的之后)老师其他学生的鱼卡IDS:{}===>>>总共{}条",studentId,teacherId,skuId,stStudentSchema.getTeacherId(),Collections3.extractToList(victimCourseSchedules,"workorderId").toArray(),victimCourseSchedules.size());
        }

        ScheduleBatchReqSt scheduleBatchReqSt = match(studentId,teacherId,aggressorCourseSchedules,victimCourseSchedules,channel);
        logger.info("指定老师stp-2:::生成要匹配的鱼卡数据去请求师生运营匹配:::======>>>APP端学生ID:{}====>>请求师生运营去匹配的数据{}",studentId,teacherId,skuId,stStudentSchema.getTeacherId(),scheduleBatchReqSt.toString());

        //TODO 此处去请求师生运营

        ScheduleBatchReqSt responseScheduleBatchReqSt = remoteService.matchTeacher(scheduleBatchReqSt);
        //TODO 此处请求师生运营进行教师重新匹配
        //TODO 分为3中状态 匹配成功直接更新鱼卡和课表 不匹配不更新 无时间片 请求记录入库
        List<ScheduleModelSt> scheduleModelStList = responseScheduleBatchReqSt.getScheduleModelList();
        if(Collections3.isEmpty(scheduleModelStList)){
            throw new BusinessException("请求师生运营系统匹配老师返回数据空");
        }
        /** ---------------假数据测试开始------------------
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
         -*/
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
        logger.info("指定老师stp-2:::师生运营完成匹配:::======>>>APP端学生ID:{}====>>师生运营完成匹配,其中匹配上IDS:{}",studentId,teacherId,skuId,stStudentSchema.getTeacherId(),macthedWorkOrderIdList.toArray());
        logger.info("指定老师stp-2:::师生运营完成匹配:::======>>>APP端学生ID:{}====>>师生运营完成匹配,其中未匹配上IDS:{}",studentId,teacherId,skuId,stStudentSchema.getTeacherId(),Collections3.extractToList(wait2applyWorkOrderIdList,"workOrderId"));


        if(channel.equals(ConstantUtil.STUDENT_CHANNLE)){
            makeApplyRecords(teacherId,studentId,wait2applyWorkOrderIdList,skuId);
        }else if(channel.equals(ConstantUtil.TEACHER_CHANNLE)){
            changeApplyRecords(studentId,teacherId,macthedWorkOrderIdList);
        }else if(channel.equals(ConstantUtil.TIMER_CHANNLE)){
            makeApplyRecords(teacherId,studentId,wait2applyWorkOrderIdList,skuId);
        }
        logger.info("指定老师stp-2::::匹配鱼卡完成:::::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>匹配上的鱼卡共{}条",studentId,teacherId,skuId,macthedWorkOrderIdList.size());
        if(Collections3.isNotEmpty(macthedWorkOrderIdList)){
            logger.info("指定老师stp-2:::开始更新鱼卡和课表入库:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>匹配上的鱼卡IDS{}",studentId,teacherId,skuId,wait2applyWorkOrderIdList.toArray());
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
            logger.info("指定老师stp-2::::通知更新群组:::::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>鱼卡IDS{}",studentId,teacherId,skuId,macthedWorkOrderIdList.toArray());
//            notifyOthers(workOrders);
            logger.info("指定老师stp-2::::异步记录鱼卡日志:::::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>鱼卡IDS{}",studentId,teacherId,skuId,macthedWorkOrderIdList.toArray());
//            changeTeacherLog(workOrders);
        }


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
                    if(channel.equals(ConstantUtil.TEACHER_CHANNLE)){
                        scheduleModelSt.setMatchStatus(ScheduleModelSt.MatchStatus.wait2apply);
                    }
                    break;
                }
            }
            scheduleModelSts.add(scheduleModelSt);
        }
        scheduleBatchReqSt.setUserId(studentId);
        scheduleBatchReqSt.setAssginTeacherId(teacherId);

        if(channel.equals(ConstantUtil.STUDENT_CHANNLE)){
            scheduleBatchReqSt.setOperateType(ConstantUtil.MANUAL_OPERATOR);
        }else if(channel.equals(ConstantUtil.TEACHER_CHANNLE)){
            scheduleBatchReqSt.setOperateType(ConstantUtil.MANUAL_OPERATOR);
        }else if(channel.equals(ConstantUtil.TIMER_CHANNLE)){
            scheduleBatchReqSt.setOperateType(ConstantUtil.TIMER_OPERATOR);
        }
        scheduleBatchReqSt.setScheduleModelList(scheduleModelSts);
        return scheduleBatchReqSt;
    }
    /**
     *
     * @param studentId
     * @param teacherId
     * @param skuId
     * @return
     */
    public StStudentSchema checkSchema(Long studentId, Long teacherId, Integer skuId){
        StStudentSchema stStudentSchema = stStudentSchemaJpaRepository.findByStudentIdAndTeacherIdAndSkuId(studentId,teacherId, StStudentSchema.CourseType.getEnum(skuId));
        if(null == stStudentSchema){
            logger.info("指定老师stp-2:::检查该学生的上课模式:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>当前学生以前未指定过老师}",studentId,teacherId,skuId);
            stStudentSchema = new StStudentSchema();
            stStudentSchema.setCreateTime(new Date());
            stStudentSchema.setUpdateTime(new Date());
            stStudentSchema.setStSchema(StStudentSchema.StSchema.assgin);
            stStudentSchema.setStudentId(studentId);
            stStudentSchema.setTeacherId(teacherId);
            stStudentSchema.setSkuId(StStudentSchema.CourseType.getEnum(skuId));
        }else{
            logger.info("指定老师stp-2:::检查该学生的上课模式:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>当前学生以前定过老师:{}",studentId,teacherId,skuId,stStudentSchema.getTeacherId());
            stStudentSchema.setStSchema(StStudentSchema.StSchema.assgin);
            stStudentSchema.setTeacherId(teacherId);
            stStudentSchema.setUpdateTime(new Date());
            stStudentSchema.setSkuId(StStudentSchema.CourseType.getEnum(skuId));
        }
        stStudentSchemaJpaRepository.save(stStudentSchema);
        return stStudentSchema;
    }

    /**
     *
     * @param teacherId
     * @param studentId
     * @param wait2applyWorkOrderIdList
     */
    @Transactional
    public void makeApplyRecords(Long teacherId, Long studentId, List<ScheduleModelSt> wait2applyWorkOrderIdList, Integer skuId){
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
    @Transactional
    public void changeApplyRecords(Long studentId,Long teacherId,List<Long> macthedWorkOrderIdList){
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
}
