package com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.initstrategy;

import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
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

    private final String GENERATE_PUBLIC_SERVICE = "GENERATE_PUBLIC_SERVICE";

    @Autowired
    private
    @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate redisTemplate;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Override
    public RecommandCourseView getRecommandCourse(SmallClass smallClass) {
        return smallClassRequester.getPublicCourse(smallClass);
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

        this.writeCourseBack(smallClass, Arrays.asList(workOrder));

        this.writeTeacherInfoBack(smallClass, Arrays.asList(workOrder), teacherView);

        this.persistGroupClass(smallClass, recommandCourseView);
    }

    @Override
    @Transactional
    public void persistGroupClass(SmallClass smallClass, RecommandCourseView recommandCourseView) {
        this.persistSmallClass(smallClass, smallClassJpaRepository);
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
        workOrder.setService(service);

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
