package com.boxfishedu.workorder.servicex.smallclass;


import com.boxfishedu.workorder.common.bean.FishCardAuthEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.LevelEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassStuJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.ScheduleCourseInfoMorphiaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.SmallClassStu;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.requester.TeacherPhotoRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.smallclass.initstrategy.GroupInitStrategy;
import com.boxfishedu.workorder.servicex.smallclass.initstrategy.SmallClassInitStrategy;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventDispatch;
import com.boxfishedu.workorder.web.param.StudentForSmallClassParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicClassBuilderParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.TrialSmallClassParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by hucl on 17/1/10.
 */
@Component
public class SmallClassBackServiceX {
    @Autowired
    private SmallClassEventDispatch smallClassEventDispatch;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private SmallClassStuJpaRepository smallClassStuJpaRepository;

    @Autowired
    private SmallClassRequester smallClassRequester;

    @Autowired
    private ServeService serveService;

    @Autowired
    private TeacherPhotoRequester teacherPhotoRequester;

    @Autowired
    private
    @Qualifier(ConstantUtil.SMALL_CLASS_INIT)
    GroupInitStrategy smallClassInitStrategy;

    @Autowired
    private ScheduleCourseInfoMorphiaRepository scheduleCourseInfoMorphiaRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void configPublicClass(PublicClassBuilderParam publicClassBuilderParam) {
        SmallClass smallClass = new SmallClass(publicClassBuilderParam);
        addTime(publicClassBuilderParam, smallClass);
        smallClass.setClassStatusEnum(PublicClassInfoStatusEnum.CREATE);
        new SmallClassEvent(smallClass, smallClassEventDispatch, smallClass.getClassStatusEnum());
    }

    private void addTime(PublicClassBuilderParam publicClassBuilderParam, SmallClass smallClass) {
        TimeSlots timeSlots = teacherStudentRequester.getTimeSlot(publicClassBuilderParam.getSlotId().intValue());
        smallClass.setStartTime(
                DateUtil.String2Date(String.join(" ", publicClassBuilderParam.getDate(), timeSlots.getStartTime())));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(
                smallClass.getStartTime().toInstant(), ZoneId.systemDefault());
        smallClass.setEndTime(DateUtil.localDate2Date(localDateTime.plusMinutes(35)));
    }

    @Transactional
    public void delete(Long smallClassId) {
        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        teacherStudentRequester.notifyCancelSmallClassTeacher(smallClass);
        List<WorkOrder> workOrders = workOrderJpaRepository.findBySmallClassId(smallClassId);
        List<CourseSchedule> courseSchedules = courseScheduleRepository.findBySmallClassId(smallClassId);
        workOrderJpaRepository.delete(workOrders);
        courseScheduleRepository.delete(courseSchedules);
        smallClassJpaRepository.delete(smallClassId);
        logger.debug("@delete#删除小班课,smallclass[{}],鱼卡[{}],课表[{}]"
                , JacksonUtil.toJSon(smallClass), JacksonUtil.toJSon(workOrders), JacksonUtil.toJSon(courseSchedules));
    }

    //根据小班课id 和 学生列表获取
    public JsonResultModel getStudentList(Long smallClassId) {
        //获取小班课信息
        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        // 1 此时没有课程的学生
        List<SmallClassStu> smallClassStus = smallClassStuJpaRepository.findByStuWithOutClasses(smallClass.getStartTime());
        //2 增加level 过滤level
        filterSmallClassStu(smallClassStus);
        smallClassStus = filterEqualLevel(smallClass, smallClassStus);

        //3 课程过滤  还未讨论

        return JsonResultModel.newJsonResultModel(smallClassStus);
    }

    //查询小班课补课学生名单含有level
    public JsonResultModel getStudentList() {

        List<SmallClassStu> smallClassStus = smallClassStuJpaRepository.findAll();
        //2 增加level
        filterSmallClassStu(smallClassStus);
        return JsonResultModel.newJsonResultModel(smallClassStus);
    }


    // 获取 level
    private void filterSmallClassStu(List<SmallClassStu> smallClassStus) {
        smallClassStus.stream().forEach(smallClassStu -> {
            smallClassStu.setLevel(smallClassRequester.fetchUserDifficultyInfo(smallClassStu.getStudentId()));
        });

    }


    private List<SmallClassStu> filterEqualLevel(SmallClass smallClass, List<SmallClassStu> smallClassStus) {
        // 过滤level 可以过滤相同level  如果level5  可以放level 5,6,7,8 的学生  枚举类 LevelEnum
        return smallClassStus.stream().filter(smallClassStu ->
                (
                        smallClass.getDifficultyLevel().equals(smallClassStu.getLevel())
                )
        ).collect(Collectors.toList());
    }


    public JsonResultModel getStudentBackUpList(Long studentId, Pageable pageable) {
        if (Objects.isNull(studentId)) {
            return JsonResultModel.newJsonResultModel(smallClassStuJpaRepository.findAll(pageable));
        } else {
            return JsonResultModel.newJsonResultModel(smallClassStuJpaRepository.findByStudentId(studentId, pageable));
        }

    }


    @Transactional
    public JsonResultModel deletebackup(Long studentId) {
        SmallClassStu smallClassStu = smallClassStuJpaRepository.findByStudentId(studentId);
        if (Objects.isNull(smallClassStu)) {
            return JsonResultModel.newJsonResultModel("该学生不存在");
        }

        smallClassStuJpaRepository.delete(smallClassStu.getId());
        return JsonResultModel.newJsonResultModel("OK");
    }

    @Transactional
    public JsonResultModel addbackup(StudentForSmallClassParam studentForSmallClassParam) {
        SmallClassStu smallClassStu = smallClassStuJpaRepository.findByStudentId(studentForSmallClassParam.getStudentId());
        if (!Objects.isNull(smallClassStu)) {
            return JsonResultModel.newJsonResultModel("该学生已经存在");
        }

        smallClassStu = new SmallClassStu();
        smallClassStu.setStudentId(studentForSmallClassParam.getStudentId());

        smallClassStu.setPhone(studentForSmallClassParam.getPhone());
        smallClassStu.setStudentName(studentForSmallClassParam.getStudentName());
        smallClassStuJpaRepository.save(smallClassStu);
        return JsonResultModel.newJsonResultModel("OK");
    }


    public void buildTrialSmallClass(TrialSmallClassParam trialSmallClassParam) {
        ThreadLocalUtil.trialSmallClassParamLocal.set(trialSmallClassParam);
        SmallClass smallClass = new SmallClass(trialSmallClassParam);
        smallClass.setTeacherPhoto(teacherPhotoRequester.getTeacherPhoto(trialSmallClassParam.getTeacherId()));
        smallClass.setClassStatusEnum(PublicClassInfoStatusEnum.CREATE);
        new SmallClassEvent(smallClass, smallClassEventDispatch, smallClass.getClassStatusEnum());
    }

    @Transactional
    public void saveSmallClassAndCards(SmallClass smallClass, List<WorkOrder> workOrders) {
        workOrderJpaRepository.save(workOrders);

        smallClass.setAllCards(workOrders);
        smallClass.setGroupLeader(workOrders.get(0).getStudentId());
        smallClass.setGroupLeaderCard(workOrders.get(0).getId());
        smallClass.setClassType(ClassTypeEnum.SMALL.name());
        smallClass.setStatus(PublicClassInfoStatusEnum.TEACHER_ASSIGNED.getCode());

        smallClassJpaRepository.save(smallClass);
        //回写群组信息到smallclass
        FishCardGroupsInfo fishCardGroupsInfo = smallClassInitStrategy.buildChatRoom(smallClass);
        smallClassInitStrategy.writeChatRoomBack(smallClass, workOrders, fishCardGroupsInfo);
        serveService.batchSaveWorkOrderAndCourses(smallClass, workOrders);
    }
    @Transactional
    public void dissmissSmallClass(Long smallClassId){
        //重置鱼卡
        List<WorkOrder> workOrders = workOrderJpaRepository.findBySmallClassId(smallClassId);
        workOrders.stream().forEach(workOrder ->{
            workOrder.setTeacherId(0l);
            workOrder.setTeacherName(null);
            workOrder.setAssignTeacherTime(null);
            workOrder.setStatus(FishCardStatusEnum.CREATED.getCode());
            workOrder.setCourseId(null);
            workOrder.setCourseName(null);
            workOrder.setCourseType(null);
            workOrder.setSmallClassId(null);
            workOrderJpaRepository.save(workOrder);
            //删除ScheduleCourseInfo
            scheduleCourseInfoMorphiaRepository.deleteByworkOrderId(workOrder.getId());
                }
        );
        //重置CourseSchedule
        List<CourseSchedule> courseSchedules=courseScheduleRepository.findBySmallClassId(smallClassId);
        courseSchedules.stream().forEach(courseSchedule -> {
            courseSchedule.setTeacherId(0l);
            courseSchedule.setCourseId(null);
            courseSchedule.setStatus(FishCardStatusEnum.CREATED.getCode());
            courseSchedule.setCourseName(null);
            courseSchedule.setCourseType(null);
            courseSchedule.setSmallClassId(null);
            courseScheduleRepository.save(courseSchedule);
        });
        //通知释放teacher
        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        teacherStudentRequester.notifyCancelSmallClassTeacher(smallClass);
        //删除小班课
        smallClassJpaRepository.delete(smallClassId);
    }
}
