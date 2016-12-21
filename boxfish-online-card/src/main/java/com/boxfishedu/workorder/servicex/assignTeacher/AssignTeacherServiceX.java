package com.boxfishedu.workorder.servicex.assignTeacher;

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
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by olly on 2016/12/15.
 */
@Service
public class AssignTeacherServiceX {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    private final static String STUDENT_CHANNLE = "ss_manual";
//    private final static String TEACHER_CHANNLE = "st_manual";
//    private final static String TIMER_CHANNLE = "auto";
//    private final static String MANUAL_OPERATOR ="manual";//OperateType
//    private final static String TIMER_OPERATOR ="auto";//OperateType
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
        logger.info("指定老师stp-1::::::======>>>APP端学生ID{}===>>>>发起指定老师{}===>>skuId{}",studentId,teacherId,skuId);
        Date startTime = DateTime.now().plusHours(48).toDate();
        List<CourseSchedule> aggressorCourseSchedules = courseScheduleRepository.findByStudentIdAndStartTimeGreaterThanAndIsFreezeAndTeacherIdNot(studentId,startTime,0,teacherId);//TODO 发起指定老师的学生的48小时候的课表
        stAssignTeacherService.doAssignTeacher(teacherId,studentId,aggressorCourseSchedules, ConstantUtil.STUDENT_CHANNLE,skuId);
        return JsonResultModel.newJsonResultModel(null);
    }

    /**
     * 老师点接受
     * @param teacherId
     * @param studentId
     * @param courseScheleIds
     * @param workOrderIds
     * @return
     */
    public JsonResultModel teacherAccept(Long teacherId,Long studentId,List<Long> courseScheleIds,List<Long> workOrderIds){
        Integer skuId = stStudentSchemaJpaRepository.findByStudentIdAndTeacherId(studentId,teacherId).getSkuId().ordinal();
        List<CourseSchedule> aggressorCourseSchedules = courseScheduleRepository.findByWorkorderIdIn(workOrderIds);
        stAssignTeacherService.doAssignTeacher(teacherId,studentId,aggressorCourseSchedules,ConstantUtil.STUDENT_CHANNLE,skuId);
        return JsonResultModel.newJsonResultModel(null);

    }

    /**
     *
     */
    public void autoAssign(){
        logger.info("<定时任务>指定老师stp-1::::::开始==================");
        List<StStudentSchema> list = stStudentSchemaJpaRepository.findByStSchema(StStudentSchema.StSchema.assgin);
        Date startTime = DateTime.now().plusHours(48).toDate();
        List<CourseSchedule> aggressorCourseSchedules = null;
        for(StStudentSchema stStudentSchema : list){
            aggressorCourseSchedules = courseScheduleRepository.findByStudentIdAndStartTimeGreaterThanAndIsFreezeAndTeacherIdNot(stStudentSchema.getStudentId(),startTime,0,stStudentSchema.getTeacherId());//TODO 发起指定老师的学生的48小时候的课表
            if(Collections3.isNotEmpty(aggressorCourseSchedules)){
                logger.info("<定时任务>指定老师stp-1::::::======>>>学生ID{}===>>>>指定老师{}===>>skuId{}",stStudentSchema.getStudentId(),stStudentSchema.getTeacherId(),stStudentSchema.getSkuId().ordinal());
                stAssignTeacherService.doAssignTeacher(stStudentSchema.getTeacherId(),stStudentSchema.getStudentId(),aggressorCourseSchedules,ConstantUtil.TIMER_CHANNLE,stStudentSchema.getSkuId().ordinal());
            }

        }
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



}
