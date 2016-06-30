package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.workorder.web.view.base.BaseView;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import java.util.Date;

/**
 * Created by LuoLiBing on 16/4/25.
 */

public class TeacherCourseSchedule extends BaseView {

    private Long id;

    private Long studentId;

    private Long teacherId;

    private Integer timeSlotId;

    private String courseId;

    /**
     * 10:老师已选,20:学生已选,30:已推课,40:已上课,50:已过期,60:已作废
     */
    private Integer status = 10;

    private Long workorderId;

    private Date classDate;

    @Column(name = "role_id", nullable = true)
    private Integer roleId;

    private String courseType;

    @JsonProperty(value = "courseInfo")
    private CourseView courseView;
}
