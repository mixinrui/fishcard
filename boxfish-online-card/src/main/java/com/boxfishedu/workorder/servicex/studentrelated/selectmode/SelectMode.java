package com.boxfishedu.workorder.servicex.studentrelated.selectmode;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.param.TimeSlotParam;

import java.util.*;

/**
 * Created by LuoLiBing on 16/9/22.
 * 选时间方式
 */
public interface SelectMode {

    Integer TEMPLATE = 0;

    List<WorkOrder> initWorkOrderList(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> services);

    default WorkOrder initWorkOrder(Service service, int index, Integer slotId) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setStatus(FishCardStatusEnum.CREATED.getCode());
        workOrder.setService(service);
        workOrder.setTeacherId(0l);
        workOrder.setOrderId(service.getOrderId());
        workOrder.setStudentId(service.getStudentId());
        workOrder.setStudentName(service.getStudentName());
        workOrder.setIsCourseOver((short) 0);
        workOrder.setSlotId(slotId);
        workOrder.setSeqNum(index);
        workOrder.setCreateTime(new Date());
        workOrder.setOrderCode(service.getOrderCode());
        workOrder.setIsFreeze(0);
        // skuIdExtra 字段
        workOrder.setSkuIdExtra(service.getSkuId().intValue());
        workOrder.setOrderChannel(service.getOrderChannel());
        return workOrder;
    }

    default Queue<ServiceChoice> createServiceChoice(List<Service> services) {
        Queue<ServiceChoice> serviceQueue = new LinkedList<>();
        for(int i = 0; i < services.size(); i++) {
            serviceQueue.add(new ServiceChoice(i, services.get(i).getAmount()));
        }
        return serviceQueue;
    }

    default int choice(Queue<ServiceChoice> queue) {
        ServiceChoice serviceChoice;
        while(true) {
            serviceChoice = queue.poll();
            if(Objects.isNull(serviceChoice)) {
                return -1;
            }
            if(serviceChoice.hasNext()) {
                serviceChoice.decrement();
                queue.add(serviceChoice);
                break;
            }
        }
        return serviceChoice.index;
    }
}
