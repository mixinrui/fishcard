package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/4/19.
 * 时间片
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeSlots implements Cloneable, Serializable {

    private final static long serialVersionUID = 1L;

    public final static Integer NOT_ASSIGN_STATUS = 0;
    public final static String CACHE_KEY = "timeSlots";
    private Long slotId;
    private String startTime;
    private String endTime;
    private boolean selected;
    private TimeSlotsStatus status = TimeSlotsStatus.FREE;
    @JsonIgnore
    private String courseId;
    @JsonIgnore
    private String courseName;
    private String courseType;
    @JsonProperty(value = "courseInfo")
    private CourseView courseView;
    // 默认为未分配
    private Integer courseScheduleStatus = NOT_ASSIGN_STATUS;
    private Long workOrderId;

    public TimeSlots() {}

    public TimeSlots(CourseSchedule courseSchedule) {
        initTimeSlots(courseSchedule);
    }

    public void initTimeSlots(CourseSchedule courseSchedule) {
        this.slotId = courseSchedule.getTimeSlotId().longValue();
        this.courseId = courseSchedule.getCourseId();
        this.courseName = courseSchedule.getCourseName();
        this.courseType = courseSchedule.getCourseType();
        this.selected = true;
        this.status = TimeSlotsStatus.ASSIGNED;
        this.workOrderId = courseSchedule.getWorkorderId();
        this.courseScheduleStatus = courseSchedule.getStatus();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Integer getStatus() {
        return status.value();
    }

    public boolean free() {
        return !selected;
    }

    /**
     * 是否有课,通过状态大于0来判断
     * @return
     */
    public boolean isHaveCourse() {
        return this.courseScheduleStatus > FishCardStatusEnum.UNKNOWN.getCode();
    }

    public String getCourseType() {
        if(courseView == null || CollectionUtils.isEmpty(courseView.getCourseType())) {
            return courseType;
        } else {
            return courseView.getCourseType().get(0);
        }

    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        this.status = selected ? TimeSlotsStatus.SELECTED : TimeSlotsStatus.FREE;
    }

    public void setCourseView(CourseView courseView) {
        this.courseView = courseView;
    }

    /**
     * 根据语言环境设置课程信息, Accept-Language: zh-CN
     * @param courseView
     * @param locale
     */
    public void setCourseView(CourseView courseView, Locale locale) {
        this.courseView = courseView;
        if(Objects.isNull(courseView)) {
            return;
        }

        // 非中文环境全部显示成英文
        if(!Objects.equals(locale, Locale.CHINA)) {
            courseView.setName(courseView.getEnglishName());
        }
    }
}
