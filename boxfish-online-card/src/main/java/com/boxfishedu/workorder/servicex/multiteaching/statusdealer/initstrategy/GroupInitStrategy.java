package com.boxfishedu.workorder.servicex.multiteaching.statusdealer.initstrategy;

import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Lists;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 17/1/8.
 */
public interface GroupInitStrategy {
    RecommandCourseView getRecommandCourse();

    TeacherView getRecommandTeacher(SmallClass smallClass);

    void initGroupClass(SmallClass smallClass);

    default List<Long> workOrders2Students(List<WorkOrder> workOrders) {
        List<Long> studentds = Lists.newArrayList();
        workOrders.forEach(workOrder -> studentds.add(workOrder.getStudentId()));
        return studentds;
    }

    default TutorTypeEnum teachingType2TutorType(SmallClass smallClass) {
        return ((TeachingType) TeachingType.get(smallClass.getRoleId()))
                .teachingType2TutorType();
    }

    void writeTeacherInfoBack(SmallClass smallClass,List<WorkOrder> workOrders);

    void writeCourseBack(SmallClass smallClass,List<WorkOrder> workOrders);

    @Transactional
    void persistGroupClass(SmallClass smallClass);

    void postCreate();
}
