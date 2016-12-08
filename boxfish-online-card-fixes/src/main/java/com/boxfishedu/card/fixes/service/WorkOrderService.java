package com.boxfishedu.card.fixes.service;

import com.boxfishedu.card.fixes.entity.jpa.CourseSchedule;
import com.boxfishedu.card.fixes.entity.jpa.CourseScheduleJpaRepository;
import com.boxfishedu.card.fixes.entity.jpa.WorkOrder;
import com.boxfishedu.card.fixes.entity.jpa.WorkOrderJpaRepository;
import com.boxfishedu.card.fixes.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.card.fixes.entity.mongo.ScheduleCourseInfoMorphiaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by LuoLiBing on 16/11/3.
 */
@Service
public class WorkOrderService {

    private final static Logger logger = LoggerFactory.getLogger(WorkOrderService.class);

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleJpaRepository courseScheduleJpaRepository;

    @Autowired
    private ScheduleCourseInfoMorphiaRepository scheduleCourseInfoMorphiaRepository;

    @Value("${recommend.url.courseInfo}")
    private String url;

    private AtomicInteger counter = new AtomicInteger(0);

    public void handleAllDifferent() {
        List<WorkOrder> workOrders = workOrderJpaRepository.findNotFinishWorkOrder();
        workOrders.parallelStream()
                .filter(workOrder -> !StringUtils.isEmpty(workOrder.getCourseId()))
                .forEach(this::handleDifferent);
    }

    public void handleScheduleCourseInfo() {
        List<WorkOrder> workOrders = workOrderJpaRepository.findNotFinishWorkOrder();
        workOrders.parallelStream()
                .filter(workOrder -> !StringUtils.isEmpty(workOrder.getCourseId()))
                .forEach(workOrder -> {
                    ScheduleCourseInfo sc = scheduleCourseInfoMorphiaRepository.findByWorkOrderId(workOrder.getId());
                    if(sc != null && !Objects.equals(sc.getName(), workOrder.getCourseName())) {
                        logger.info(counter.incrementAndGet() + " : workOrder and mongoScheduleCourse not equals, workOrderId=[{}], courseId=[{}], createTime=[{}]",
                                workOrder.getId(), workOrder.getCourseId(), workOrder.getCreateTime());
                        sc.setCourseId(workOrder.getCourseId());
                        scheduleCourseInfoMorphiaRepository.updateCourseInfo(sc);
                    }
                });
    }

    public void synchronousCourseInfoById(String courseId) {
        List<ScheduleCourseInfo> list = scheduleCourseInfoMorphiaRepository.findByCourseId(courseId);
        list.parallelStream().forEach( sc -> scheduleCourseInfoMorphiaRepository.updateCourseInfo(sc));
    }

    public void handleDifferent(WorkOrder workOrder) {
        Map courseMap = new RestTemplate().getForObject(
                String.format(url, workOrder.getCourseId()), Map.class);
        workOrder.setCourseName((String) courseMap.get("courseName"));
        workOrder.setCourseType((String) courseMap.get("type"));
        workOrderJpaRepository.save(workOrder);
        handleWorkOrderAndCourseSchedule(workOrder);
        handleWorkOrderAndScheduleCourse(workOrder);
    }

    /**
     * 鱼卡与课表的差异
     * @param workOrder
     */
    private void handleWorkOrderAndCourseSchedule(WorkOrder workOrder) {
        Optional<CourseSchedule> op = courseScheduleJpaRepository.findTop1ByWorkorderId(workOrder.getId());
        if(op.isPresent()) {
            CourseSchedule cs = op.get();
            if(!Objects.equals(workOrder.getCourseId(), cs.getCourseId())) {
                logger.info(counter.incrementAndGet() + " : workOrder and courseSchedule not equals, workOrderId=[{}], courseId=[{}], createTime=[{}]",
                        workOrder.getId(), workOrder.getCourseId(), workOrder.getCreateTime());
                cs.setCourseName(workOrder.getCourseName());
                cs.setCourseId(workOrder.getCourseId());
                cs.setCourseType(workOrder.getCourseType());
                courseScheduleJpaRepository.save(cs);
            }
        }
    }

    /**
     * 课表与mongo的差异
     * @param workOrder
     */
    private void handleWorkOrderAndScheduleCourse(WorkOrder workOrder) {
        ScheduleCourseInfo sc = scheduleCourseInfoMorphiaRepository.findByWorkOrderId(workOrder.getId());
        if(Objects.nonNull(sc) && !Objects.equals(workOrder.getCourseId(), sc.getCourseId())) {
            logger.info(counter.incrementAndGet() + " : workOrder and mongoScheduleCourse not equals, workOrderId=[{}], courseId=[{}], createTime=[{}]",
                    workOrder.getId(), workOrder.getCourseId(), workOrder.getCreateTime());
            sc.setCourseId(workOrder.getCourseId());
            scheduleCourseInfoMorphiaRepository.updateCourseInfo(sc);
        }
    }
}
