package com.boxfishedu.workorder.servicex.smallclass.initstrategy;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
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
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventDispatch;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    @Autowired
    private SmallClassEventDispatch smallClassEventDispatch;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

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
    public void initGroupClass(SmallClass smallClass) {
        //找出leader鱼卡
        WorkOrder leader = this.selectLeader(smallClass.getAllCards());

        //获取推荐课程
        RecommandCourseView recommandCourseView =
                smallClassRequester.fetchClassCourseByUserIds(this.workOrders2Students(
                        smallClass.getAllCards()), smallClass.getDifficultyLevel()
                        , leader.getRecommendSequence(), this.teachingType2TutorType(smallClass));

        //回写课程信息
        this.writeCourseBack(smallClass, smallClass.getAllCards()
                , recommandCourseView, recommandCourseRequester);

        //获取推荐教师
        TeacherView teacherView = this.getRecommandTeacher(smallClass);

        if (0 != teacherView.getId()) {
            this.writeTeacherInfoBack(smallClass, smallClass.getAllCards(), teacherView);
        } else {
            logger.debug("@initGroupClass#getTeacher#fail#获取教师失败,退出小班课创建#smallclass[{}]"
                    , JacksonUtil.toJSon(smallClass));
            return;
        }

        //将小班课,公开课相关信息保存入库
        this.persistGroupClass(smallClass, smallClass.getAllCards(), recommandCourseView);


        this.recordLog(smallClass, PublicClassInfoStatusEnum.COURSE_ASSIGNED);
        this.recordLog(smallClass, PublicClassInfoStatusEnum.TEACHER_ASSIGNED);
    }

    @Override
    public FishCardGroupsInfo buildChatRoom(SmallClass smallClass) {
        return courseOnlineRequester.buildsmallClassChatRoom(smallClass);
    }

    @Override
    @Transactional
    public void persistGroupClass(SmallClass smallClass, List<WorkOrder> workOrders, RecommandCourseView recommandCourseView) {
        this.persistSmallClass(smallClass);
        smallClass.setAllCards(workOrders);
        this.persistCardRelatedInfo(smallClass, recommandCourseView);
    }

    @Override
    public List<CourseSchedule> saveOrUpdateCourseSchedules(Service service, List<WorkOrder> workOrders) {
        List<CourseSchedule> courseSchedules = workOrderService
                .batchUpdateCourseScheduleByWorkOrder(service, workOrders);
        return courseSchedules;
    }

}
