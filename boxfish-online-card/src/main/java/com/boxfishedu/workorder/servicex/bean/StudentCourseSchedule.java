package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;

/**
 * Created by LuoLiBing on 16/4/22.
 */
@Data
public class StudentCourseSchedule implements Serializable {

    private Long id;

    private String time;

    @JsonProperty(value = "teacherInfo")
    private TeacherView teacherView;

    @JsonIgnore
    private String courseId;

    @JsonProperty(value = "courseInfo")
    private CourseView courseView;

    private String courseType;

    private Long workOrderId;

    private Integer status;

    public String getCourseType() {
        if(courseView == null || CollectionUtils.isEmpty(courseView.getCourseType())) {
            return courseType;
        } else {
            return courseView.getCourseType().get(0);
        }

    }
}
