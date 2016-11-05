package com.boxfishedu.workorder.service.instantclass;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

/**
 * Created by hucl on 16/11/3.
 */
@Component
public class InstantClassService {

    public Optional<TimeSlots> getMostSimilarSlot(DayTimeSlots dayTimeSlots){
        LocalDateTime nextSlotTime=LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(30);
        return dayTimeSlots.getDailyScheduleTime().stream()
                .filter(timeSlot->nextSlotTime
                        .isAfter(DateUtil.string2LocalDateTime(String.join(" ",DateUtil.date2SimpleString(new Date()),timeSlot.getStartTime()))))
                .max(Comparator.comparing(timeSlots -> timeSlots.getSlotId()));
    }

}
