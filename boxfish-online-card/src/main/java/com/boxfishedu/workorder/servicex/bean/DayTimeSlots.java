package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/4/19.
 * 天
 */
@Data
public class DayTimeSlots implements Cloneable, Serializable {

    public final static String CACHE_KEY = "DayTimeSlots";

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String day;

    private List<TimeSlots> dailyScheduleTime;

    public DayTimeSlots() {}

    public long getDayStamp() {
        if(day == null) {
            return -1;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(day).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public DayTimeSlots(String day) {
        this.day = day;
        dailyScheduleTime = Lists.newArrayList();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        DayTimeSlots dayTimeSlots = new DayTimeSlots(day);
        for(TimeSlots timeSlots: dailyScheduleTime) {
            dayTimeSlots.addTimeSlots((TimeSlots) timeSlots.clone());
        }
        return dayTimeSlots;
    }

    public void addTimeSlots(TimeSlots timeSlots) {
        if(timeSlots != null) {
            this.dailyScheduleTime.add(timeSlots);
        }
    }

    public boolean isSameDate(String day) {
        return StringUtils.equals(day, this.day);
    }

    public void resetDailyScheduleTime(List<TimeSlots> dailyScheduleTime) {
        cleanDailyScheduleTime();
        this.dailyScheduleTime.addAll(dailyScheduleTime);
    }

    public void cleanDailyScheduleTime() {
        if(dailyScheduleTime != null) {
            dailyScheduleTime.clear();
        }
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void override(List<CourseSchedule> courseScheduleList, ServiceSDK serviceSDK) {
        // 世超返回的是long型
        if(CollectionUtils.isEmpty(courseScheduleList)) {
            return;
        }
        Map<Integer, CourseSchedule> courseScheduleMap = groupByTimeSlotId(courseScheduleList);

        dailyScheduleTime.forEach( timeSlots -> {
            if(! timeSlots.free()) {
                timeSlots.setStatus(TimeSlotsStatus.SELECTED);
                CourseSchedule courseSchedule = courseScheduleMap.get(timeSlots.getSlotId().intValue());
                if (checkCourseSchedule(courseSchedule)) {
                    timeSlots.initTimeSlots(courseSchedule);
                    if (StringUtils.isNotEmpty(courseSchedule.getCourseId())) {
                        timeSlots.setCourseView(serviceSDK.getCourseInfo(courseSchedule.getId()));
                    }
                }
            }
        });
    }


    /**
     *
     * @param courseSchedules
     * @param serviceSDK
     */
    public void override(Map<String, Map<String, CourseSchedule>> courseSchedules,
                         ServiceSDK serviceSDK) {
        Map<String, CourseSchedule> courseScheduleMap = courseSchedules.get(day);
        if(courseScheduleMap == null) {
            courseScheduleMap = Maps.newHashMap();
        }
        Iterator<TimeSlots> it = dailyScheduleTime.iterator();
        while(it.hasNext()) {
            TimeSlots timeSlots = it.next();
            // 没有选中的过滤掉
            if(timeSlots.free()) {
                it.remove();
            } else {
                timeSlots.setStatus(TimeSlotsStatus.SELECTED);
                CourseSchedule courseSchedule = courseScheduleMap.get(timeSlots.getSlotId().toString());
                if (checkCourseSchedule(courseSchedule)) {
                    timeSlots.initTimeSlots(courseSchedule);
                    if (StringUtils.isNotEmpty(courseSchedule.getCourseId())) {
                        timeSlots.setCourseView(
                                serviceSDK.getCourseInfo(courseSchedule.getId()));
                    }
                }
            }
        }
    }

    public void transferTimestampToDate() {
        if(StringUtils.isNotEmpty(day)) {
            this.day = DateUtil.simpleDateLong2String(Long.valueOf(day));
        }
    }


    public static Map<Integer, CourseSchedule> groupByTimeSlotId(List<CourseSchedule> courseScheduleList) {
        Map<Integer, CourseSchedule> resultMap = Maps.newLinkedHashMap();
        for(CourseSchedule courseSchedule: courseScheduleList) {
            resultMap.put(courseSchedule.getTimeSlotId(), courseSchedule);
        }
        return resultMap;
    }

    /**
     * 不为空且是同一天
     * @param courseSchedule
     * @return
     */
    private boolean checkCourseSchedule(CourseSchedule courseSchedule) {
        return courseSchedule != null && DateUtil.simpleDate2String(courseSchedule.getClassDate()).equals(day);
    }

    /**
     * filter过滤
     * @param timePredicate
     * @param dayPredicate
     * @return
     */
    public DayTimeSlots filter(Predicate<DayTimeSlots> dayPredicate, Predicate<TimeSlots> timePredicate) {
        if(!dayPredicate.test(this)) {
            return this;
        }
        this.dailyScheduleTime = dailyScheduleTime.stream()
                .filter(timePredicate)
                .collect(Collectors.toList());
        return CollectionUtils.isEmpty(this.getDailyScheduleTime()) ? null : this;
    }

}
