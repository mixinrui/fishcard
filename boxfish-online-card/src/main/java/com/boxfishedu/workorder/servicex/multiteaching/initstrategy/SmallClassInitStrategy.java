package com.boxfishedu.workorder.servicex.multiteaching.initstrategy;

import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.requester.SmallClassTeacherRequester;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.WorkOrderService;
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
    public void initGroupClass(SmallClass smallClass) {
        //找出leader鱼卡
        WorkOrder leader = this.selectLeader(smallClass.getAllCards());

        //获取推荐课程
        RecommandCourseView recommandCourseView =
                smallClassRequester.fetchClassCourseByUserIds(this.workOrders2Students(
                        smallClass.getAllCards()), smallClass.getDifficultyLevel(), leader.getRecommendSequence(), this.teachingType2TutorType(smallClass));

        //回写课程信息
        this.writeCourseBack(smallClass, smallClass.getAllCards()
                , recommandCourseView, recommandCourseRequester);

        //获取推荐教师
        TeacherView teacherView = this.getRecommandTeacher(smallClass);

        if (0 != teacherView.getId()) {
            this.writeTeacherInfoBack(smallClass, smallClass.getAllCards(), teacherView);
        }

        //将小班课,公开课相关信息保存入库
        this.persistGroupClass(smallClass, smallClass.getAllCards(), recommandCourseView);
    }

    @Override
    public FishCardGroupsInfo buildChatRoom(SmallClass smallClass) {
        return null;
    }

    @Override
    @Transactional
    public void persistGroupClass(SmallClass smallClass, List<WorkOrder> workOrders, RecommandCourseView recommandCourseView) {
        this.persistSmallClass(smallClass, smallClassJpaRepository);
        smallClass.setAllCards(workOrders);
        this.persistCardRelatedInfo(smallClass, workOrderService, scheduleCourseInfoService, recommandCourseView);
    }

}
