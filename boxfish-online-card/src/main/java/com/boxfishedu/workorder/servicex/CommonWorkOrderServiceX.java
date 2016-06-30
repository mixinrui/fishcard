package com.boxfishedu.workorder.servicex;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import org.jdto.DTOBinder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by hucl on 16/3/31.
 */
@org.springframework.stereotype.Service
public class CommonWorkOrderServiceX {
    @Autowired
    private DTOBinder binder;

    @Autowired
    private WorkOrderService workOrderService;

    public WorkOrder findWorkOrderById(Long workOrderId) {
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        return workOrder;
    }
}
