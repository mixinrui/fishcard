package com.boxfishedu.workorder.web.param;

import lombok.Data;

/**
 * Created by hucl on 16/5/23.
 */
@Data
public class TrialLectureParam {
    private Long teacherId;
    private Long studentId;
    private String courseId;
    private String courseName;
    private String startTime;
    private String endTime;
    private String courseType;
    private Integer timeSlotId;

}
