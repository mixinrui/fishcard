package com.boxfishedu.workorder.service.timer;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.web.view.fishcard.TeacherAssignedCourseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        LocalDateTime beginLocalDate = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()).minusHours(24);
        LocalDateTime endLocalDate = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault());
        Date beginDate=DateUtil.localDate2Date(beginLocalDate);
        Date endDate=DateUtil.localDate2Date(endLocalDate);

        String sql = "select new com.boxfishedu.workorder.web.view.fishcard.TeacherAssignedCourseView" +
                "(count(wo.id),wo.teacherId) " +
                " from  WorkOrder wo where wo.assignTeacherTime >=? and wo.assignTeacherTime<?  group by wo.teacherId";
        Query query = entityManager.createQuery(sql).setParameter(1, beginDate).setParameter(2,endDate);
        List<TeacherAssignedCourseView> teacherAssignedCourseViews = query.getResultList();
        return teacherAssignedCourseViews;
    }
}
