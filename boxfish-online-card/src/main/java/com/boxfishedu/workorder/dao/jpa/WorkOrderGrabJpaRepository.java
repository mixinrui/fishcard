package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Created by jiaozijun on 16/7/11.
 */
public interface WorkOrderGrabJpaRepository extends JpaRepository<WorkOrderGrab, Long> {



}
