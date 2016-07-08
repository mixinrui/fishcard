package com.boxfishedu.workorder.service.timer;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.base.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;


/**
 * Created by hucl on 16/7/7.
 */
@Component
public class DailyCourseAssignedService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private EntityManager entityManager;

    public List<TeacherAssignedCourseView> getCardAssignedDaily() {
        Date date = new Date();
        Date simpleDate = DateUtil.date2SimpleDate(date);

        String sql = "select new com.boxfishedu.workorder.web.view.fishcard.TeacherAssignedCourseView" +
                "(count(wo.id),wo.teacherId) " +
                " from  WorkOrder wo where wo.assignTeacherTime >? group by wo.teacherId";
        Query query = entityManager.createQuery(sql).setParameter(1, simpleDate);
        List<TeacherAssignedCourseView> teacherAssignedCourseViews = query.getResultList();
        return teacherAssignedCourseViews;
    }
}
