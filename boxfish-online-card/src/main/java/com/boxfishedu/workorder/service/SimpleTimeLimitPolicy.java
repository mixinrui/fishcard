package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/5/19.
 * 周一到周五 上课时间 19-24 周六-周日 7-24
 *
 */
@Service
public class SimpleTimeLimitPolicy implements TimeLimitPolicy {

    public TimeRange[] dayTimeRange;

    /**
     * 一周的第一天为周日,最后一天为周六
     */
    public SimpleTimeLimitPolicy() {
        dayTimeRange = new TimeRange[7];
        for(int i = 0; i< 7; i++) {
            if(i == 0 || i == 6) {
                dayTimeRange[i] = new TimeRange(7, 24);
            } else {
                dayTimeRange[i] = new TimeRange(18, 24);
            }
        }
    }

    @Override
    public boolean isAvailable(Date date, Date time, TimeSlots timeSlots) {
        // 可用时间为在可用时间范围之内或者是已经选择过的时间片
        return (dayTimeRange[getDayOfWeek(date)]).isDuring(time) || timeSlots.isSelected();
    }

    /**
     * 时间限制过滤
     * @param dayTimeSlots
     * @return
     */
    @Override
    public DayTimeSlots limit(DayTimeSlots dayTimeSlots) {
        if(dayTimeSlots == null || CollectionUtils.isEmpty(dayTimeSlots.getDailyScheduleTime())) {
            return dayTimeSlots;
        }

        Iterator<TimeSlots> it = dayTimeSlots.getDailyScheduleTime().iterator();
        Date day = DateUtil.String2SimpleDate(dayTimeSlots.getDay());

        while (it.hasNext()) {
            TimeSlots timeSlots = it.next();
            Date startTime = DateUtil.parseTime(timeSlots.getStartTime());
            if(! isAvailable(day, startTime, timeSlots)) {
                it.remove();
            }
        }
        return dayTimeSlots;
    }

    @Override
    public List<DayTimeSlots> limit(List<DayTimeSlots> dayTimeSlotsList) {
        return dayTimeSlotsList.stream().map(this::limit).collect(Collectors.toList());
    }

    private int getDayOfWeek(Date dt){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }

    @Data
    public static class TimeRange {
        Date from;

        Date to;

        public TimeRange() {}

        public TimeRange(Date from, Date to) {
            this.from = from;
            this.to = to;
        }

        public TimeRange(Integer from, Integer to) {
            this.from = parseFrom(from);
            this.to = parseTo(to);
        }

        private Date parseFrom(Integer from) {
            from = from-1;
            return DateUtil.parseTime(String.format("%s:59:59", from < 10? "0" + from: from));
        }

        private Date parseTo(Integer to) {
            return DateUtil.parseTime(String.format("%s:59:59", to < 10? "0" + to: to));
        }

        public boolean isDuring(Date time) {
            return time.after(from) && time.before(to);
        }
    }
}
