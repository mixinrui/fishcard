package com.boxfishedu.workorder.servicex.multiteaching;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.SmallClassEvent;
import com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.SmallClassEventDispatch;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicClassBuilderParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 17/1/10.
 */
@Component
public class SmallClassBackServiceX {
    @Autowired
    private SmallClassEventDispatch smallClassEventDispatch;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

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
}
