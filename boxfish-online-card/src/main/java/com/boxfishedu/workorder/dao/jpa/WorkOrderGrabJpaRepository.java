package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;


/**
 * Created by jiaozijun on 16/7/11.
 */
public interface WorkOrderGrabJpaRepository extends JpaRepository<WorkOrderGrab, Long> {

    // 获取今天之前的数据
    public List<WorkOrderGrab> findByCreateTimeLessThan(Date date);

}
