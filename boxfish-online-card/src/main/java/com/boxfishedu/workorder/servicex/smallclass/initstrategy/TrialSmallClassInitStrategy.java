package com.boxfishedu.workorder.servicex.smallclass.initstrategy;

import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.requester.SmallClassTeacherRequester;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassBackServiceX;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventDispatch;
import com.boxfishedu.workorder.servicex.tiallecture.TrialLectureServiceX;
import com.boxfishedu.workorder.web.param.TrialLectureParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by hucl on 17/1/8.
 */
@SuppressWarnings("ALL")
@Component(ConstantUtil.SMALLCLASS_TRIAL_INIT)
public class TrialSmallClassInitStrategy implements GroupInitStrategy {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SmallClassRequester smallClassRequester;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private SmallClassTeacherRequester smallClassTeacherRequester;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    @Autowired
    private SmallClassEventDispatch smallClassEventDispatch;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private TrialLectureServiceX trialLectureServiceX;

    @Autowired
    private OnlineAccountService onlineAccountService;

    @Autowired
    private SmallClassBackServiceX smallClassBackServiceX;

    @Autowired
    private ServeService serveService;

    private final String GENERATE_DEMO_SERVICE = "GENERATE_DEMO_SERVICE";

    @Autowired
    private
    @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate redisTemplate;

    public WorkOrder selectLeader(List<WorkOrder> workOrders) {
        logger.debug("小班鱼卡[{}]", JacksonUtil.toJSon(workOrders));
        workOrders.sort(Comparator.comparing(workOrder -> workOrder.getStartTime()));
        logger.debug("小班课leader[{}]", workOrders.get(0));
        return workOrders.get(0);
    }

    //推荐课写到初始化代码里去
    @Override
    public RecommandCourseView getRecommandCourse(SmallClass smallClass) {
        return null;
    }

    @Override
    public TeacherView getRecommandTeacher(SmallClass smallClass) {
        return smallClassTeacherRequester.getSmallClassTeacher(smallClass);
    }

    @Override
    public SmallClassJpaRepository getSmallClassRepository() {
        return smallClassJpaRepository;
    }

    @Override
    public WorkOrderService getWorkOrderService() {
        return workOrderService;
    }

    @Override
    public ScheduleCourseInfoService getScheduleCourseInfoService() {
        return scheduleCourseInfoService;
    }

    @Override
    public SmallClassEventDispatch getSmallEventDispathch() {
        return smallClassEventDispatch;
    }

    @Override
    @Transactional
    public void initGroupClass(SmallClass smallClass) {

        this.buildFishCards(smallClass);

        logger.debug("@initGroupClass#small#创建试讲小班成功,小班是[{}],小班里的鱼卡是[{}]"
                , JacksonUtil.toJSon(smallClass), JacksonUtil.toJSon(smallClass.getAllCards()));
    }

    @Override
    public List<CourseSchedule> saveOrUpdateCourseSchedules(Service service, List<WorkOrder> workOrders) {
        List<CourseSchedule> courseSchedules = workOrderService
                .batchUpdateCourseScheduleByWorkOrder(service, workOrders);
        return courseSchedules;
    }

    @Override
    public FishCardGroupsInfo buildChatRoom(SmallClass smallClass) {
        return null;
    }

    @Override
    public void persistGroupClass(SmallClass smallClass, List<WorkOrder> workOrders, RecommandCourseView recommandCourseView) {

    }

    public void buildFishCards(SmallClass smallClass) {
        List<WorkOrder> workOrders = Lists.newArrayList();

        smallClass.getAllStudentIds().forEach(studentId -> {
            CourseSchedule courseSchedule = trialLectureServiceX.getOldCourseSchedule(
                    DateUtil.Date2String(smallClass.getStartTime()), studentId, smallClass.getSlotId());
            if (null != courseSchedule) {
                throw new BusinessException("学生在当前时间片内已存在课程,请勿重复安排课程");
            }

            Service service = this.getDemoService();
            WorkOrder workOrder = new WorkOrder();

            workOrder.setService(service);
            workOrder.setIsFreeze(0);
            workOrder.setClassType(ClassTypeEnum.SMALL.name());
            workOrder.setComboType(service.getComboType());
            workOrder.setOrderId(service.getOrderId());
            workOrder.setStudentId(studentId);
            workOrder.setComboType(ComboTypeEnum.SMALLCLASS.name());

            //虚拟订单
            this.saveParamIntoWorkOrder(workOrder, smallClass);

            workOrders.add(workOrder);

        });

        smallClass.getAllStudentIds().forEach(studentId -> {
            onlineAccountService.add(studentId);
        });

        smallClassBackServiceX.saveSmallClassAndCards(smallClass, workOrders);

        logger.info("试讲课鱼卡生成结束,鱼卡列表[{}]:", JacksonUtil.toJSon(workOrders));
    }

    private void saveParamIntoWorkOrder(WorkOrder workOrder, SmallClass smallClass) {
        workOrder.setCourseType(smallClass.getCourseType());
        workOrder.setCourseId(smallClass.getCourseId());
        workOrder.setCourseName(smallClass.getCourseName());
        workOrder.setCreateTime(new Date());

        workOrder.setTeacherId(smallClass.getTeacherId());
        workOrder.setTeacherName(smallClass.getTeacherName());

        workOrder.setSlotId(smallClass.getSlotId());
        workOrder.setStartTime(smallClass.getStartTime());
        workOrder.setEndTime(smallClass.getEndTime());
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
    }

    private Service getDemoService() {
        List<Service> services = serviceJpaRepository.findByOrderId(this.virtualOrderId());
        if (CollectionUtils.isEmpty(services)) {
            return this.createDemoService();
        }
        return services.get(0);
    }

    private Service createDemoService() {
        Service service = new Service();
        service.setCreateTime(new Date());
        service.setStudentId(0l);
        service.setOrderId(this.virtualOrderId());
        service.setClassSize(0);
        service.setUserType(0);

        if (redisTemplate.opsForValue().setIfAbsent(GENERATE_DEMO_SERVICE, Boolean.TRUE.toString())) {
            service = serviceJpaRepository.save(service);
            redisTemplate.delete(GENERATE_DEMO_SERVICE);
        }

        return service;
    }

    public Long virtualOrderId() {
        return Long.MAX_VALUE - 3;
    }

}
