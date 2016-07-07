package com.boxfishedu.workorder.service.timer;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.web.view.fishcard.TeacherAlterView;
import com.boxfishedu.workorder.web.view.fishcard.TeacherAssignedCourseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/7/7.
 */
public class DailyCourseAssignedService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private EntityManager entityManager;

    public List<TeacherAssignedCourseView> getCardAssignedDaily(){
        String sql = "select new com.boxfishedu.workorder.web.view.fishcard.TeacherAlterView" +
                "(count(cs.id),cs.roleId)" +
                " from  CourseSchedule cs where (cs.status=? and cs.classDate between ? and ?) ";
        Query query = entityManager.createQuery(sql).setParameter(1,FishCardStatusEnum.COURSE_ASSIGNED.getCode()).setParameter(2, beginDate).setParameter(3, endDate);
        List<TeacherAlterView> teacherAlterViews=query.getResultList();
        return teacherAlterViews;

        Date date=new Date();
        DateUtil.date


        String sql = "select new com.boxfishedu.workorder.web.view.fishcard.TeacherAssignedCourseView" +
                "((count(wo.id),wo.teacherId)" +
                " from  WorkOrder wo where wo.assignTeacherTime >?";

//        return teacherAlterViews;
    }
}
