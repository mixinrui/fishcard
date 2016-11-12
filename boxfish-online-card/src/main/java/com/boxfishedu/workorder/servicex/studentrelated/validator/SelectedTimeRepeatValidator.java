package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by LuoLiBing on 16/9/24.
 * 提交上课时间重复性验证
 */
@Order(3)
@Component
public class SelectedTimeRepeatValidator implements StudentTimePickerValidator {

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Override
    public void prepareValidate(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> serviceList) {
        List<SelectedTime> selectedTimes = timeSlotParam.getSelectedTimes();
        // 重复选择时间
        Set<String> selectTimesSet = new HashSet<>();
        for(SelectedTime selectedTime : selectedTimes) {
            if(!selectTimesSet.add(selectedTime.getSelectedDate() + "-" + selectedTime.getTimeSlotId())) {
                TimeSlots timeSlot = teacherStudentRequester.getTimeSlot(selectedTime.getTimeSlotId());
                throw new ValidationException("选择有重复的时间"
                        + selectedTime.getSelectedDate() + " " + timeSlot.getStartTime());
            }
        }
    }
}
