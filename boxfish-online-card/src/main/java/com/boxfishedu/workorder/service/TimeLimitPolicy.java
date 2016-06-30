package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;

import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 16/5/19.
 */
public interface TimeLimitPolicy {

    boolean isAvailable(Date date, Date time, TimeSlots timeSlots);

    DayTimeSlots limit(DayTimeSlots dayTimeSlots);

    List<DayTimeSlots> limit(List<DayTimeSlots> dayTimeSlotsList);
}
