package com.boxfishedu.workorder.servicex.assignTeacher;

import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentSchemaJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.boxfishedu.workorder.entity.mysql.StStudentSchema;
import com.boxfishedu.workorder.web.param.ScheduleBatchReqSt;
import com.boxfishedu.workorder.web.param.bebase3.ScheduleModelSt;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by olly on 2016/12/15.
 */
public class AssignTeacherService {
    @Autowired
    WorkOrderJpaRepository workOrderJpaRepository;
    @Autowired
    CourseScheduleRepository courseScheduleRepository;
    @Autowired
    StStudentApplyRecordsJpaRepository stStudentApplyRecordsJpaRepository;
    @Autowired
    StStudentSchemaJpaRepository stStudentSchemaJpaRepository;
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
    public void doAssign(Long teacherId,Long studentId){
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
        List<CourseSchedule> victimCourseSchedules = null;//courseScheduleRepository.findByTeacherIdAndTimeslotsIdInAndClassDateInAndIsFreeze(teacherId,timeslotsList,classDateList,0);//TODO 当前指定老师的其他学生的课表
        if(Collections3.isNotEmpty(victimCourseSchedules)){
            CourseSchedule courseSchedule;
            for (Iterator<CourseSchedule> iter = victimCourseSchedules.iterator(); iter.hasNext();) {
                courseSchedule = iter.next();
//                if(courseSchedule.isAssgin()){
//                    iter.remove();//把是这个指定老师的排除掉
//                }
            }
        }



//        List<CourseSchedule> victimCourseSchedules =
        //TODO 此处请求师生运营进行教师重新匹配
        //TODO 分为3中状态 匹配成功直接更新鱼卡和课表 不匹配不更新 无时间片 请求记录入库

//        List<>

        //无时间片 请求记录入库
        StStudentApplyRecords stStudentApplyRecords = new StStudentApplyRecords();
        stStudentApplyRecords.setTeacherId(teacherId);
        stStudentApplyRecords.setStudentId(studentId);
        stStudentApplyRecords.setCreateTime(new Date());
        stStudentApplyRecords.setUpdateTime(new Date());
        stStudentApplyRecords.setApplyStatus(StStudentApplyRecords.ApplyStatus.pending);
        stStudentApplyRecords.setIsRead(StStudentApplyRecords.ReadStatus.no);
        stStudentApplyRecords.setWorkOrderId(1l);
        stStudentApplyRecords.setCourseScheleId(1l);
        stStudentApplyRecordsJpaRepository.save(stStudentApplyRecords);
    }

    private List match(List<CourseSchedule> aggressorCourseSchedules,List<CourseSchedule> victimCourseSchedules){
        ScheduleBatchReqSt scheduleBatchReqSt = new ScheduleBatchReqSt();
        List<ScheduleModelSt> scheduleModelSts = Lists.newArrayList();
        ScheduleModelSt scheduleModelSt = null;
        for(CourseSchedule courseSchedule : aggressorCourseSchedules){
            scheduleModelSt = new ScheduleModelSt(courseSchedule);
            scheduleModelSt.setCourseType(courseSchedule.getCourseType());
            for (CourseSchedule  victimCourseSchedule :victimCourseSchedules){
                if(courseSchedule.getClassDate().compareTo(victimCourseSchedule.getClassDate()) == 0 && courseSchedule.getTimeSlotId() == victimCourseSchedule.getTimeSlotId() ){

                }
            }
        }
        return null;
    }
}
