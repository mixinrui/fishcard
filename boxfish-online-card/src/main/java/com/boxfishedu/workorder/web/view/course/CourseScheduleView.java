package com.boxfishedu.workorder.web.view.course;

import com.boxfishedu.workorder.web.view.base.BaseView;
import lombok.Data;

@Data
public class CourseScheduleView extends BaseView {
    private Long studentId;
    private Long teacherId;
    private Long timeSlotId;
    private String courseId;
    private Integer status;
    private Long workOrderId;
    private String courseScheduleId;

}