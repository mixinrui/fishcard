package com.boxfishedu.workorder.servicex.multiteaching;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.SmallClassEvent;
import com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.SmallClassEventDispatch;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicClassBuilderParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 17/1/10.
 */
@Component
public class SmallClassBackServiceX {
    @Autowired
    private SmallClassEventDispatch smallClassEventDispatch;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    public void configPublicClass(PublicClassBuilderParam publicClassBuilderParam) {
        SmallClass smallClass = new SmallClass(publicClassBuilderParam);
        addTime(publicClassBuilderParam, smallClass);
        new SmallClassEvent(smallClass, smallClassEventDispatch, SmallClassCardStatus.CREATE);
    }

    private void addTime(PublicClassBuilderParam publicClassBuilderParam, SmallClass smallClass) {
        TimeSlots timeSlots = teacherStudentRequester.getTimeSlot(publicClassBuilderParam.getSlotId().intValue());
        smallClass.setStartTime(
                DateUtil.String2Date(String.join(" ", publicClassBuilderParam.getDate(), timeSlots.getStartTime())));
        smallClass.setEndTime(
                DateUtil.String2Date(String.join(" ", publicClassBuilderParam.getDate(), timeSlots.getEndTime())));
    }

    public void delete(Long smallClassId) {
        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        teacherStudentRequester.notifyCancelSmallClassTeacher(smallClass);
        List<WorkOrder> workOrders=workOrderJpaRepository.findBySmallClassId(smallClassId);
        List<CourseSchedule> courseSchedules=courseScheduleRepository.findBySmallClassId(smallClassId);
        workOrderJpaRepository.delete(workOrders);
        courseScheduleRepository.delete(courseSchedules);
        smallClassJpaRepository.delete(smallClassId);
    }
}
