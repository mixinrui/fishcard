package com.boxfishedu.workorder.servicex.multiteaching.statusdealer.initstrategy;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.requester.SmallClassTeacherRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by hucl on 17/1/8.
 */
@Component(ConstantUtil.SMALL_CLASS_INIT)
public class SmallClassInitStrategy implements GroupInitStrategy {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SmallClassRequester smallClassRequester;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private SmallClassTeacherRequester smallClassTeacherRequester;

    @Autowired
    private WorkOrderService workOrderService;

    public WorkOrder selectLeader(List<WorkOrder> workOrders) {
        logger.debug("小班鱼卡[{}]", JacksonUtil.toJSon(workOrders));
        workOrders.sort(Comparator.comparing(workOrder -> workOrder.getStartTime()));
        logger.debug("小班课leader[{}]", workOrders.get(0));
        return workOrders.get(0);
    }

    @Override
    public RecommandCourseView getRecommandCourse() {
        return null;
    }

    @Override
    public TeacherView getRecommandTeacher(SmallClass smallClass) {
        return smallClassTeacherRequester.getSmallClassTeacher(smallClass);
    }

    @Override
    public void initGroupClass(SmallClass smallClass) {
        //找出leader鱼卡
        WorkOrder leader = this.selectLeader(smallClass.getAllCards());

        //获取推荐课程
        RecommandCourseView recommandCourseView = smallClassRequester
                .fetchClassCourseByUserIds(this.workOrders2Students(
                        smallClass.getAllCards()), smallClass.getDifficultyLevel(), leader.getSeqNum(), this.teachingType2TutorType(smallClass));

        //获取推荐教师
        TeacherView teacherView=this.getRecommandTeacher(smallClass);

        //回写课程信息
        this.writeCourseBack(smallClass,smallClass.getAllCards());

        this.writeTeacherInfoBack(smallClass,smallClass.getAllCards());

        //将课程信息保存入库
        this.persistGroupClass(smallClass);
    }

    @Override
    public void writeTeacherInfoBack(SmallClass smallClass, List<WorkOrder> workOrders) {
        if (!Objects.isNull(smallClass.getTeacherId())) {
            workOrders.forEach(workOrder -> {
                workOrder.setSmallClassId(smallClass.getId());
                workOrder.setTeacherId(smallClass.getTeacherId());
                workOrder.setTeacherName(smallClass.getTeacherName());
                workOrder.setUpdateTime(new Date());
                workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
            });
            smallClass.setAllCards(workOrders);
        }
    }

    @Override
    public void writeCourseBack(SmallClass smallClass, List<WorkOrder> workOrders) {
        if (Objects.isNull(smallClass.getCourseId())) {
            logger.error("@writeCourseBack#小班课没有课程信息,不回写到数据库,smallclass[{}],workOrders[{}]"
                    , JacksonUtil.toJSon(smallClass), JacksonUtil.toJSon(workOrders));
            throw new BusinessException("小班课没有获取到课程信息,不作回写");
        }
        workOrders.forEach(workOrder -> {
            workOrder.setCourseId(smallClass.getCourseId());
            workOrder.setCourseName(smallClass.getCourseName());
            if (workOrder.getStatus() != FishCardStatusEnum.TEACHER_ASSIGNED.getCode()) {
                workOrder.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
            }
        });
        smallClass.setAllCards(workOrders);
    }

    @Override
    @Transactional
    public void persistGroupClass(SmallClass smallClass) {
        smallClassJpaRepository.save(smallClass);
        smallClass.getAllCards().forEach(workOrder -> {
//            workOrderService.saveWorkOrderAndSchedule();
        });
    }

    @Override
    public void postCreate() {

    }
}
