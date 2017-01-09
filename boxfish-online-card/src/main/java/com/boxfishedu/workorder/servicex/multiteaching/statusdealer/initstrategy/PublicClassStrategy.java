package com.boxfishedu.workorder.servicex.multiteaching.statusdealer.initstrategy;

import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 17/1/8.
 */
@Component(ConstantUtil.PUBLIC_CLASS_INIT)
public class PublicClassStrategy implements GroupInitStrategy {
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
    public void writeTeacherInfoBack(SmallClass smallClass,List<WorkOrder> workOrders) {

    }

    @Override
    public void writeCourseBack(SmallClass smallClass, List<WorkOrder> workOrders) {

    }

    @Override
    public void persistGroupClass(SmallClass smallClass) {

    }

    @Override
    public void postCreate() {

    }
}
