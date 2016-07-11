package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.WorkOrderGrabHistory;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * 抢单历史记录表
 * Created by jiaozijun on 16/7/11.
 */
public interface WorkOrderGrabHistoryJpaRepository extends JpaRepository<WorkOrderGrabHistory, Long> {



}
