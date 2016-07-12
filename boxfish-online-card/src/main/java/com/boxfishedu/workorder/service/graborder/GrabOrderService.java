package com.boxfishedu.workorder.service.graborder;

import com.boxfishedu.workorder.dao.jpa.WorkOrderGrabJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.service.base.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by mk on 16/7/12.
 */
@Component
public class GrabOrderService extends BaseService<WorkOrderGrab, WorkOrderGrabJpaRepository, Long> {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderGrabJpaRepository workOrderGrabJpaRepository;

    /**
     * 根据teacherId获取鱼卡列表
     * @return
     */
    public List<WorkOrderGrab> findByTeacherIdAndFlagAndStartTimeGreaterThan(WorkOrderGrab workOrderGrab){
        return workOrderGrabJpaRepository.findByTeacherIdAndFlagAndStartTimeGreaterThan(workOrderGrab.getTeacherId(),workOrderGrab.getFlag(),new Date());
    }
}
