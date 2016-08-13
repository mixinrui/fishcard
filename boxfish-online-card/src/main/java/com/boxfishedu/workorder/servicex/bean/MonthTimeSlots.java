package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.web.view.base.DateIntervalView;
import com.boxfishedu.workorder.web.view.base.ResponseBaseView;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/4/19.
 * 月
 */
@Data
public class MonthTimeSlots extends ResponseBaseView implements Serializable {

    private String returnCode = "200";
    private String returnMsg;

    private List<DayTimeSlots> data;

    private boolean hasMoreHistory = false;

    /**
     * 一天的毫秒数
     */
    public final static int DAY_OF_MILLIONS = 86400000;

    public final static String CACHE_KEY_TEACHER_FIRST_DAY = "teacherFirstDayCacheKey";

    public MonthTimeSlots() {
        this.data = Lists.newArrayList();
    }

    public MonthTimeSlots(List<DayTimeSlots> dayTimeSlotsList) {
        this();
        dayTimeSlotsList.forEach( dayTimeSlots -> addDayTimeSlots(dayTimeSlots));
    }

    public void addDayTimeSlots(DayTimeSlots dayTimeSlots) {
        if(dayTimeSlots != null) {
            data.add(dayTimeSlots);
        }
    }

    public void initDayTimeSlots(DayTimeSlots dayTimeSlotsTemplate, DateIntervalView dateIntervalView) {
        long begin = dateIntervalView.beginDate().getTime();
        long end = dateIntervalView.endDate().getTime();
        for(long time = begin; time <= end; time += DAY_OF_MILLIONS) {
            try {
                DayTimeSlots clone = (DayTimeSlots) dayTimeSlotsTemplate.clone();
                clone.setDay(DateUtil.simpleDate2String(new Date(time)));
                addDayTimeSlots(clone);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 覆盖时间片
     * @param
     */
    public void override(Map<String, Map<String, CourseSchedule>> courseSchedules,
                         ServiceSDK serviceSDK) {
        Iterator<DayTimeSlots> it = data.iterator();
        while (it.hasNext()){
            DayTimeSlots dayTimeSlots = it.next();
            // 世超传过来的是一个long型,这边是用一个long型的字符串接收.....
            // dayTimeSlots.setDay(DateUtil.simpleDateLong2String(Long.valueOf(dayTimeSlots.getDay())));
            dayTimeSlots.override(courseSchedules, serviceSDK);
            if(CollectionUtils.isEmpty(dayTimeSlots.getDailyScheduleTime())) {
                it.remove();
            }
        }
    }

    public void transferTimestampToDate() {
        for(DayTimeSlots dayTimeSlots : data) {
            dayTimeSlots.setDay(DateUtil.simpleDateLong2String(Long.valueOf(dayTimeSlots.getDay())));
        }
    }

    public MonthTimeSlots filter(Predicate<DayTimeSlots> dayPredicate, Predicate<TimeSlots> timePredicate) {

        this.data = data.parallelStream()
                .map(d -> d.filter(dayPredicate, timePredicate))
                .filter(d -> d!=null)
                .collect(Collectors.toList());
        return this;
    }
}
