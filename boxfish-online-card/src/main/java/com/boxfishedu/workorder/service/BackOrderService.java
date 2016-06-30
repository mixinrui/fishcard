package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.base.BaseService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 关于退单的操作
 * Created by zijun.jiao  on 16/6/15.
 */

@Component
public class BackOrderService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {

    public List<WorkOrder> findByOrderId(Long orderId){
        return jpa.findByOrderId(orderId);
    }

    public List<WorkOrder> findByOrderIdAndStatus(Long orderId,int status){
        return jpa.findByOrderIdAndStatus(orderId,status);
    }





}
