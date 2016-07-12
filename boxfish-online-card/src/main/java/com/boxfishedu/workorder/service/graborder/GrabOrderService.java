package com.boxfishedu.workorder.service.graborder;

import com.boxfishedu.workorder.dao.jpa.WorkOrderGrabJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.web.view.fishcard.GrabOrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;


    /**
     * 根据teacherId获取鱼卡列表
     * @return
     */
    public List<WorkOrderGrab> findByTeacherIdAndFlagAndStartTimeGreaterThan(WorkOrderGrab workOrderGrab){
        return workOrderGrabJpaRepository.findByTeacherIdAndFlagAndStartTimeGreaterThan(workOrderGrab.getTeacherId(),workOrderGrab.getFlag(),new Date());
    }

    /**
     * 抢单更新workordergrab 中的teacherId
     * @return
     */
    public int setFlagAndTeacherId(GrabOrderView grabOrderView){
        return workOrderGrabJpaRepository.setFlagAndTeacherId(grabOrderView.getTeacherId(),grabOrderView.getWorkOrderId());
    }

    /**
     * 抢单更新workorder 中的teacherId
     * @return
     */
    public int setTeacherIdByWorkOrderId(GrabOrderView grabOrderView){
        return workOrderJpaRepository.setTeacherIdByWorkOrderId(grabOrderView.getTeacherId(),grabOrderView.getWorkOrderId());
    }
}
