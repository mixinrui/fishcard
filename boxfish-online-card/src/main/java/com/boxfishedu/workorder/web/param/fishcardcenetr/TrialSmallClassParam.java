package com.boxfishedu.workorder.web.param.fishcardcenetr;

import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 17/3/20.
 */
@Data
public class TrialSmallClassParam {
    //教师
    private Long teacherId;
    private String teacherName;
    private Long roleId;

    //课程
    private String courseId;
    private String courseName;
    private String courseType;
    //小班课难度
    private String difficultyLevel;

    //小班课时间
    private String startTime;
    private String endTime;
    private Integer timeSlotId;

    //学生列表
    private List<Long> studentIds;
}
