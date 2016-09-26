package com.boxfishedu.workorder.servicex.studentrelated.selectmode;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/9/22.
 * 用户自定义方式部署
 */
@Component
public class UserDefinedSelectMode implements SelectMode {

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Override
    public List<WorkOrder> initWorkOrderList(TimeSlotParam timeSlotParams, SelectMode selectMode, List<Service> services) {
        Queue<ServiceChoice> serviceQueue = createServiceChoice(services);
        final int[] index = {1};
        return timeSlotParams.getSelectedTimes().stream().map( selectedTime -> {
            Service service = services.get(choice(serviceQueue));
            WorkOrder workOrder = initWorkOrder(service, index[0]++, selectedTime.getTimeSlotId());
            TimeSlots timeSlots = teacherStudentRequester.getTimeSlot(selectedTime.getTimeSlotId());
            String startTimeString = selectedTime.getSelectedDate() + " " + timeSlots.getStartTime();
            String endTimeString = selectedTime.getSelectedDate() + " " + timeSlots.getEndTime();
            workOrder.setStartTime(DateUtil.String2Date(startTimeString));
            workOrder.setEndTime(DateUtil.String2Date(endTimeString));
            return workOrder;
        }).collect(Collectors.toList());
    }
}
