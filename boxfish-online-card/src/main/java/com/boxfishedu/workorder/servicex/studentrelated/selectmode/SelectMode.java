package com.boxfishedu.workorder.servicex.studentrelated.selectmode;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.param.TimeSlotParam;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        workOrder.setComboType(service.getComboType());
        // skuIdExtra 字段
        workOrder.setSkuIdExtra(service.getSkuId().intValue());
        workOrder.setOrderChannel(service.getOrderChannel());
        // 鱼卡的上课类型: 1对1, 小班课, 公开课
        workOrder.setClassType(ClassTypeEnum.resolveByComboType(service.getComboType()).toString());
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

    /**
     * 创建自增序列, 兑换课【兑换课需要考虑顺序，记录下，根据不同顺序提供不同的课程】
     * @param serviceList
     * @param isExchange
     * @return
     */
    default Map<Long, AtomicInteger> createSequences(
            WorkOrderJpaRepository workOrderJpaRepository, List<Service> serviceList, boolean isExchange) {

        Map<Long, AtomicInteger> result = new HashMap<>();
        // 如果非兑换课程,直接使用一个从0开始的ato
        if(!isExchange) {
            AtomicInteger defaultAto = new AtomicInteger(0);
            for(int i = 0, size = serviceList.size(); i < size; i++) {
                result.put(serviceList.get(i).getId(), defaultAto);
            }
            return result;
        }

        // 否则需要找出这个学生同类型鱼卡最后一个序号自增
        for(int i = 0, size = serviceList.size(); i < size; i++) {
            Service service = serviceList.get(i);
            Optional<Integer> maxSequence = workOrderJpaRepository.findMaxSeqNumByStudentIdComboTypeAndSkuIdExtra(
                    service.getStudentId(), service.getComboType(), service.getSkuId().intValue());
            Integer sequence = 0;
            if(maxSequence.isPresent()) {
                sequence = maxSequence.get();
            }
            result.put(service.getId(), new AtomicInteger(sequence));
        }
        return result;
    }
}
