package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.SmallClassStu;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 *
 */
public interface SmallClassStuJpaRepository extends JpaRepository<SmallClassStu, Long> {

    //补的这节小班课 此时没有课的学生
    @Query("select scs from  SmallClassStu scs where  scs.studentId not in (select distinct wo.studentId from WorkOrder wo where wo.startTime= ?1)  ")
    public List<SmallClassStu> findByStuWithOutClasses(Date startTime);


}