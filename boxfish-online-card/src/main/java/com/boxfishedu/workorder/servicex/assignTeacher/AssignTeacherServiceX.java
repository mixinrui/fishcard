package com.boxfishedu.workorder.servicex.assignTeacher;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentSchemaJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.WorkOrderLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.StStudentSchema;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.StAssignTeacherService;
import com.boxfishedu.workorder.service.StStudentApplyRecordsService;
import com.boxfishedu.workorder.service.StStudentSchemaService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by olly on 2016/12/15.
 */
@Service
public class AssignTeacherServiceX {
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
    @Autowired
    StStudentSchemaService stStudentSchemaService;
    @Autowired
    StStudentApplyRecordsService stStudentApplyRecordsService;
    @Autowired
    StAssignTeacherService stAssignTeacherService;

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
    public JsonResultModel maualAssign(Long teacherId, Long studentId,Integer skuId){
        logger.info("@@@@assign 指定老师stp-1::::::======>>>APP端学生ID{}===>>>>发起指定老师{}===>>skuId{}=====>>>channel::{}",studentId,teacherId,skuId,"手动");
        Date startTime = DateTime.now().plusHours(48).toDate();

        List<CourseSchedule> aggressorCourseSchedules = courseScheduleRepository.
                findByStudentIdAndRoleIdAndStartTimeGreaterThanAndIsFreezeAndTeacherIdNot(studentId,skuId,startTime,0,teacherId);//TODO 发起指定老师的学生的48小时候的课表
        //TODO 去掉小班课和公开课的
        filterClassType(aggressorCourseSchedules);

        List<CourseSchedule> alreadyCourseSchedules = courseScheduleRepository.findByStudentIdAndRoleIdAndStartTimeGreaterThanAndIsFreezeAndTeacherId(studentId,skuId,startTime,0,teacherId);
        filterClassType(alreadyCourseSchedules);
        stAssignTeacherService.doAssignTeacher(teacherId,studentId,aggressorCourseSchedules,alreadyCourseSchedules, ConstantUtil.STUDENT_CHANNLE,skuId);
        return JsonResultModel.newJsonResultModel(null);
    }

    /**
     * 去掉小班课和公开课的
     * @param courseSchedules
     */
    private void filterClassType(List<CourseSchedule> courseSchedules){
        List<String> classTypes = Lists.newArrayList(ClassTypeEnum.PUBLIC.name(),ClassTypeEnum.SMALL.name());
        CourseSchedule courseSchedule = null;
        for (Iterator<CourseSchedule> iter = courseSchedules.iterator(); iter.hasNext();) {
            courseSchedule = iter.next();
            if(null != courseSchedule.getClassType() && classTypes.contains(courseSchedule.getClassType())){
                iter.remove();
            }
        }

    }

    /**
     * 老师点接受
     * @param teacherId
     * @param studentId
     * @param workOrderIds
     * @return
     */
    public JsonResultModel teacherAccept(Long teacherId,Long studentId,List<Long> workOrderIds){
        Integer skuId = stStudentSchemaJpaRepository.findByStudentIdAndTeacherId(studentId,teacherId).getSkuId().ordinal();
        logger.info("@@@@assign 指定老师stp-1::::::======>>>APP端学生ID{}===>>>>发起指定老师{}===>>skuId{}=====>>>channel::{}",studentId,teacherId,skuId,"老师接受");
        List<CourseSchedule> aggressorCourseSchedules = courseScheduleRepository.findByWorkorderIdInAndIsFreeze(workOrderIds,0);
        if(Collections3.isNotEmpty(aggressorCourseSchedules)){
            stAssignTeacherService.doAssignTeacher(teacherId,studentId,aggressorCourseSchedules,null, ConstantUtil.TEACHER_CHANNLE,skuId);
        }
        return JsonResultModel.newJsonResultModel(null);

    }

    /**
     * 定时任务调用
     */
    public void autoAssign(){
        logger.info("@@@@assign-timer指定老师stp-1::::::开始==================");
        List<StStudentSchema> list = stStudentSchemaJpaRepository.findByStSchema(StStudentSchema.StSchema.assgin);
        Date startTime = DateTime.now().plusHours(48).toDate();
        List<CourseSchedule> aggressorCourseSchedules = null;
        List<String> classTypes = Lists.newArrayList(ClassTypeEnum.PUBLIC.name(),ClassTypeEnum.SMALL.name());
        for(StStudentSchema stStudentSchema : list){
            aggressorCourseSchedules = courseScheduleRepository.
                    findByStudentIdAndRoleIdAndStartTimeGreaterThanAndIsFreezeAndTeacherIdNot(stStudentSchema.getStudentId(),
                            stStudentSchema.getSkuId().ordinal(),startTime,0,stStudentSchema.getTeacherId());//TODO 发起指定老师的学生的48小时候的课表
            filterClassType(aggressorCourseSchedules);
            List<CourseSchedule> alreadyCourseSchedules = courseScheduleRepository.findByStudentIdAndRoleIdAndStartTimeGreaterThanAndIsFreezeAndTeacherId(stStudentSchema.getStudentId(),
                    stStudentSchema.getSkuId().ordinal(),startTime,0,stStudentSchema.getTeacherId());
            filterClassType(alreadyCourseSchedules);
            logger.info("@@@@assign-timer 指定老师 <定时任务> stp-1::::::======>>>学生ID{}===>>>>指定老师{}===>>skuId===>>{}",
                    stStudentSchema.getStudentId(),stStudentSchema.getTeacherId(),stStudentSchema.getSkuId().ordinal());
            stAssignTeacherService.doAssignTeacher(stStudentSchema.getTeacherId(),stStudentSchema.getStudentId(),
                    aggressorCourseSchedules,alreadyCourseSchedules,ConstantUtil.TIMER_CHANNLE,stStudentSchema.getSkuId().ordinal());
        }
    }

    /**
     * @param studentId
     * @param teacherId
     * @param skuId
     * @return
     */
    public StStudentSchema insertOrUpdateSchema(Long studentId, Long teacherId, Integer skuId ,StStudentSchema.StSchema stSchema) {
        StStudentSchema stStudentSchema = stStudentSchemaJpaRepository.findByStudentIdAndSkuId(studentId, StStudentSchema.CourseType.getEnum(skuId));
        if (null == stStudentSchema) {
            logger.info("insertOrUpdateSchema 指定老师 stp-2:::检查该学生的上课模式:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>当前学生以前未指定过老师}",
                    studentId, teacherId, skuId);
            stStudentSchema = new StStudentSchema();
            stStudentSchema.setCreateTime(new Date());
            stStudentSchema.setUpdateTime(new Date());
            stStudentSchema.setStSchema(stSchema);
            stStudentSchema.setStudentId(studentId);
            stStudentSchema.setTeacherId(teacherId);
            stStudentSchema.setSkuId(StStudentSchema.CourseType.getEnum(skuId));
        } else {
            logger.info("insertOrUpdateSchema 指定老师 stp-2:::检查该学生的上课模式:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>当前学生以前定过老师:{}",
                    studentId, teacherId, skuId, stStudentSchema.getTeacherId());
            stStudentSchema.setStSchema(stSchema);
            stStudentSchema.setTeacherId(teacherId);
            stStudentSchema.setUpdateTime(new Date());
            stStudentSchema.setSkuId(StStudentSchema.CourseType.getEnum(skuId));
        }
        stStudentSchemaJpaRepository.save(stStudentSchema);
        return stStudentSchema;
    }


}
