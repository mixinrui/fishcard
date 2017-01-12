package com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.initstrategy;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 17/1/8.
 */
@Component(ConstantUtil.PUBLIC_CLASS_INIT)
public class PublicClassStrategy implements GroupInitStrategy {
    @Autowired
    private SmallClassRequester smallClassRequester;


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    private final String GENERATE_PUBLIC_SERVICE = "GENERATE_PUBLIC_SERVICE";

    @Autowired
    private
    @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate redisTemplate;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Override
    public RecommandCourseView getRecommandCourse(SmallClass smallClass) {
        RecommandCourseView recommandCourseView = new RecommandCourseView();
        recommandCourseView.setCover("http://api.boxfish.cn/student/publication/data/data/10fb562cdc2b3c184a418c1c18d0d8b8");
        recommandCourseView.setCourseId("ccccccccid");
        recommandCourseView.setCourseName("课程名称");
        recommandCourseView.setCourseType("type");
        recommandCourseView.setDifficulty("LEVEL_4");
        return recommandCourseView;
//        return smallClassRequester.getPublicCourse(smallClass);
    }

    @Override
    public TeacherView getRecommandTeacher(SmallClass smallClass) {
        TeacherView teacherView = new TeacherView();
        teacherView.setTeacherId(smallClass.getTeacherId());
        teacherView.setTeacherName(smallClass.getTeacherName());
        return teacherView;
    }

    @Override
    public void initGroupClass(SmallClass smallClass) {
        //获取推荐课程
        RecommandCourseView recommandCourseView = this.getRecommandCourse(smallClass);

        TeacherView teacherView = this.getRecommandTeacher(smallClass);

        //虚拟鱼卡
        WorkOrder workOrder = this.virtualCard(smallClass);
        smallClass.setAllCards(Arrays.asList(workOrder));

        this.writeCourseBack(smallClass, Arrays.asList(workOrder), recommandCourseView);

        this.writeTeacherInfoBack(smallClass, Arrays.asList(workOrder), teacherView);

        this.persistGroupClass(smallClass, smallClass.getAllCards(), recommandCourseView);
    }

    @Override
    public FishCardGroupsInfo buildChatRoom(SmallClass smallClass) {
        return courseOnlineRequester.buildsmallClassChatRoom(smallClass);
    }

    @Override
    @Transactional
    public void persistGroupClass(SmallClass smallClass, List<WorkOrder> workOrders, RecommandCourseView recommandCourseView) {
        this.persistSmallClass(smallClass, smallClassJpaRepository);

        //回写群组信息到smallclass
        FishCardGroupsInfo fishCardGroupsInfo = this.buildChatRoom(smallClass);
        this.writeChatRoomBack(smallClass, workOrders, fishCardGroupsInfo);

        smallClassJpaRepository.save(smallClass);
        this.persistCardRelatedInfo(smallClass, workOrderService, scheduleCourseInfoService, recommandCourseView);
    }

    private WorkOrder virtualCard(SmallClass smallClass) {
        Service service = this.getPublicService();

        WorkOrder workOrder = new WorkOrder();
        workOrder.setCreateTime(new Date());
        workOrder.setOrderId(service.getOrderId());
        workOrder.setStartTime(smallClass.getStartTime());
        workOrder.setEndTime(smallClass.getEndTime());
        workOrder.setChatRoomId(smallClass.getChatRoomId());
        workOrder.setGroupId(smallClass.getGroupId());
        workOrder.setClassType(smallClass.getClassType());
        workOrder.setSkuId(smallClass.getRoleId());
        workOrder.setService(service);
        workOrder.setStatus(FishCardStatusEnum.CREATED.getCode());
        workOrder.setSeqNum(0);

        return workOrder;
    }

    private Service getPublicService() {
        List<Service> services = serviceJpaRepository.findByOrderId(this.virtualOrderId());
        if (CollectionUtils.isEmpty(services)) {
            return this.createPublicService();
        }
        return services.get(0);
    }

    private Service createPublicService() {
        Service service = new Service();
        service.setCreateTime(new Date());
        service.setStudentId(0l);
        service.setOrderId(this.virtualOrderId());
        service.setClassSize(0);
        service.setUserType(0);

        if (redisTemplate.opsForValue().setIfAbsent(GENERATE_PUBLIC_SERVICE, Boolean.TRUE.toString())) {
            service = serviceJpaRepository.save(service);
            redisTemplate.delete(GENERATE_PUBLIC_SERVICE);
        }

        return service;

    }

    public Long virtualOrderId() {
        return Long.MAX_VALUE - 2;
    }

}
