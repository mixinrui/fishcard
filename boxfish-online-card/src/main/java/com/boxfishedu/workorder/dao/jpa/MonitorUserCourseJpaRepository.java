package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.MonitorResponseForm;
import com.boxfishedu.workorder.entity.mysql.MonitorUserCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * Created by ansel on 2017/3/22.
 */
public interface MonitorUserCourseJpaRepository extends JpaRepository<MonitorUserCourse, Long> {

    @Query("select new com.boxfishedu.workorder.entity.mysql.MonitorResponseForm(muc.startTime,muc.endTime,count(muc)) " +
            "from MonitorUserCourse muc where muc.classType = ?1 and muc.startTime between ?2 and ?3 and muc.userId = ?4 group by muc.startTime")
    Page<MonitorResponseForm> getClassPage(String classType, Date startTime, Date endTime, Long userId, Pageable pageable);
}
