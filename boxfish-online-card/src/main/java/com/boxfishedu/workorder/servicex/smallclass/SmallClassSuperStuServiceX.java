package com.boxfishedu.workorder.servicex.smallclass;


import com.alibaba.fastjson.JSON;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassUserTypeEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.servicex.studentrelated.SmallClassSuperStuService;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceXV1;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.SmallClassSuperStuParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by hucl on 17/1/16.
 */
@Component
public class SmallClassSuperStuServiceX {



    @Value("${parameter.small_class_size}")
    private Integer smallClassSize;

    @Autowired
    private TimePickerServiceXV1 timePickerServiceXV1;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;


    @Autowired
    private SmallClassSuperStuService smallClassSuperStuService; 


    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private ScheduleCourseInfoService courseInfoService;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //检查是否已经生成课程
    public WorkOrder checkWorkOrder(SmallClassSuperStuParam smallClassSuperStuParam){
        WorkOrder workOrder = null;
        if(ClassTypeEnum.SMALL.name().equals(smallClassSuperStuParam.getClassType())){
            workOrder =  workOrderJpaRepository.findBySmallClassIdAndStudentId(smallClassSuperStuParam.getId(),smallClassSuperStuParam.getStudentId());
        }
        return workOrder;
    }


    //组建课程信息
    @Transactional
    public WorkOrder generatorCourse(SmallClassSuperStuParam smallClassSuperStuParam){
        Service service =  smallClassSuperStuService.createSmallClassSuperService();
        if(Objects.isNull(service)){
            logger.error("@generatorCourse,没有生成service");
            return null;
        }


        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassSuperStuParam.getId());

        if(Objects.isNull(smallClass)){
            logger.error("@generatorCourse,没有小班数据");
            return null;
        }

        // TODO: 17/3/22
//        TimeSlotParam timeSlotParam = makeTimeSlotParam(smallClassSuperStuParam   ,service, smallClass);
//
//        //组装TimeSlotParam 学生id
//        List<CourseSchedule> listcourses = timePickerServiceXV1.smallClassSuperStu(timeSlotParam);
//
//
//        WorkOrder workOrder = Collections3.isEmpty(listcourses)?null:workOrderJpaRepository.findOne(listcourses.get(0).getWorkorderId());
//
//        if(Objects.isNull(workOrder)){
//            logger.error("auToMakeClassesForSmallClassERROR,没有生成workOrder");
//        }



//        CourseSchedule courseSchedule = courseScheduleRepository.findTop1ByWorkorderId(workOrder.getId());

        WorkOrder workOrder = new WorkOrder();
        // 根据smallClass 更新WorkOrder  CourseSchele
        workOrder.setCourseType(smallClass.getCourseType());
        workOrder.setCourseId(smallClass.getCourseId());
        workOrder.setCourseName(smallClass.getCourseName());
        workOrder.setTeacherId(smallClass.getTeacherId());
        workOrder.setTeacherName(smallClass.getTeacherName());
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        workOrder.setSmallClassId(smallClass.getId());
        workOrder.setUpdateTime(new Date());
        workOrder.setGeneratorType(ClassUserTypeEnum.SUPER.name());  // 超级用户


        workOrder.setService(service);
        workOrder.setOrderId(service.getOrderId());
        workOrder.setStudentId(smallClassSuperStuParam.getStudentId());
        workOrder.setStartTime(smallClass.getStartTime());
        workOrder.setEndTime(smallClass.getEndTime());
        workOrder.setCreateTime(new Date());
        workOrder.setSlotId(smallClass.getSlotId());
        workOrder.setSeqNum(1);
        workOrder.setSkuId(TeachingType.WAIJIAO.getCode());


        workOrder.setSkuIdExtra(110);
        workOrder.setIsCourseOver((short)0);
        workOrder.setOrderChannel(service.getOrderChannel());
        workOrder.setComboType(service.getComboType());
        workOrder.setIsFreeze(0);

        workOrder.setClassType(ClassTypeEnum.SMALL.name());

        workOrder = workOrderJpaRepository.save(workOrder);


        CourseSchedule courseSchedule = new CourseSchedule();
        courseSchedule.setWorkorderId(workOrder.getId());

        courseSchedule.setCourseType(smallClass.getCourseType());
        courseSchedule.setCourseId(smallClass.getCourseId());
        courseSchedule.setCourseName(smallClass.getCourseName());
        courseSchedule.setTeacherId(smallClass.getTeacherId());
        courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        courseSchedule.setSmallClassId(smallClass.getId());
        courseSchedule.setUpdateTime(new Date());


        courseSchedule.setStudentId(smallClassSuperStuParam.getStudentId());
        courseSchedule.setTimeSlotId(smallClass.getSlotId());
        courseSchedule.setClassDate(workOrder.getStartTime());
        courseSchedule.setCreateTime(new Date());
        courseSchedule.setStartTime(workOrder.getStartTime());


        courseSchedule.setRoleId(1);
        courseSchedule.setSkuIdExtra(110);
        courseSchedule.setIsFreeze(0);

        courseScheduleRepository.save(courseSchedule);

        logger.debug("@auToMakeClassesForSmallClass#开始保存小班课信息,workOrder[{}]", JacksonUtil.toJSon(workOrder));
        smallClassSuperStuService.restoreClassForSmallClass(workOrder, courseSchedule, smallClass);

        //课程保存到mongo中
        saveCourseScheToMongo(workOrder, courseSchedule);

        return workOrder;


    }


    private TimeSlotParam makeTimeSlotParam(SmallClassSuperStuParam smallClassSuperStuParam,Service service, SmallClass smallClass) {
        TimeSlotParam timeSlotParam = new TimeSlotParam();

        timeSlotParam.setStudentId(smallClassSuperStuParam.getStudentId());
        timeSlotParam.setComboType(service.getComboType());
        timeSlotParam.setOrderId(service.getOrderId());
        timeSlotParam.setProductType(service.getProductType());
        timeSlotParam.setSelectMode(0);
        timeSlotParam.setTutorType(service.getTutorType());
        if (TutorTypeEnum.CN.name().equals(service.getTutorType())) {
            timeSlotParam.setSkuId(1);
        }
        if (TutorTypeEnum.FRN.name().equals(service.getTutorType())) {
            timeSlotParam.setSkuId(2);
        }


        SelectedTime selectedTime = new SelectedTime();
        selectedTime.setSelectedDate(DateUtil.date2SimpleString(smallClass.getStartTime()));
        selectedTime.setTimeSlotId(smallClass.getSlotId());
        List<SelectedTime> selectedTimes = Lists.newArrayList(selectedTime);

        timeSlotParam.setSelectedTimes(selectedTimes);
        logger.info("auToMakeClassesForSmallClass->timeSlotParam:[{}]", JSON.toJSON(timeSlotParam));
        return timeSlotParam;
    }



    //课程保存到mongo中
    public void saveCourseScheToMongo(WorkOrder workOrder, CourseSchedule courseSchedule) {
        RecommandCourseView courseView = recommandCourseRequester.getCourseViewDetail(courseSchedule.getCourseId());
        courseInfoService.saveSingleCourseInfo(workOrder, courseSchedule, courseView);
    }






}
