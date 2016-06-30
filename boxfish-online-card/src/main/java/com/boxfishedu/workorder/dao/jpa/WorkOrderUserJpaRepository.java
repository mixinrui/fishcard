package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.WorkOrderUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by zijun.jiao on 16/6/28.
 */
public interface WorkOrderUserJpaRepository extends JpaRepository<WorkOrderUser, Long> {

    public WorkOrderUser findByUserCodeAndFlag(String userCode, String flag);

}
