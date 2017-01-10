package com.boxfishedu.workorder.servicex.multiteaching.statusdealer.initstrategy;

import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.requester.SmallClassTeacherRequester;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 17/1/8.
 */
@Component(ConstantUtil.PUBLIC_CLASS_INIT)
public class PublicClassStrategy implements GroupInitStrategy {
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

    @Override
    public RecommandCourseView getRecommandCourse() {
        return null;
    }

    @Override
    public TeacherView getRecommandTeacher(SmallClass smallClass) {
        return null;
    }

    @Override
    public void initGroupClass(SmallClass smallClass) {

    }

    @Override
    public void writeTeacherInfoBack(SmallClass smallClass, List<WorkOrder> workOrders, TeacherView teacherView) {

    }

    @Override
    public void writeCourseBack(SmallClass smallClass, List<WorkOrder> workOrders) {

    }

    @Override
    public void persistGroupClass(SmallClass smallClass, RecommandCourseView recommandCourseView) {

    }

    @Override
    public void postCreate() {

    }

}
