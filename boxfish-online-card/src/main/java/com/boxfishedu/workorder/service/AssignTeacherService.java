package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.base.BaseService;
import org.springframework.stereotype.Component;

/**
 * Created by olly on 2016/12/16.
 */
@Component
public class AssignTeacherService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {

    public void assignTeacher(){

    }
}
