package com.boxfishedu.workorder.servicex.studentrelated.selectmode;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/9/22.
 * 用户自定义方式部署
 */
@Component
public class UserDefinedSelectMode implements SelectMode {

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Override
    public List<WorkOrder> initWorkOrderList(TimeSlotParam timeSlotParams, SelectMode selectMode, List<Service> services) {
        Queue<ServiceChoice> serviceQueue = createServiceChoice(services);

        // 获取自增序列
        Map<Long, AtomicInteger> sequences = createSequences(workOrderJpaRepository, services,
                (Objects.equals(timeSlotParams.getComboType(), ComboTypeToRoleId.EXCHANGE.name())));

        return timeSlotParams.getSelectedTimes()
                .stream()
                .map( selectedTime -> initWorkOrder(serviceQueue, services, sequences, selectedTime))
                .collect(Collectors.toList());
    }

    private WorkOrder initWorkOrder(
            Queue<ServiceChoice> queue, List<Service> services, Map<Long, AtomicInteger> sequences, SelectedTime selectedTime) {
        Service service = services.get(choice(queue));
        int index = sequences.get(service.getId()).incrementAndGet();
        WorkOrder workOrder = initWorkOrder(service, index, selectedTime.getTimeSlotId());
        TimeSlots timeSlots = teacherStudentRequester.getTimeSlot(selectedTime.getTimeSlotId());
        String startTimeString = selectedTime.getSelectedDate() + " " + timeSlots.getStartTime();
        String endTimeString = selectedTime.getSelectedDate() + " " + timeSlots.getEndTime();
        workOrder.setStartTime(DateUtil.String2Date(startTimeString));
        workOrder.setEndTime(DateUtil.String2Date(endTimeString));
        return workOrder;
    }
}
