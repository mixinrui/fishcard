package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * 课程相关信息显示
 * Created by jiaozijun on 16/12/14.
 */
@Data
public class CourseInfo {
    private Long courseId;
    private Long teacherId;
    private String teacherName;
    private String teacherImg;
    private boolean assignFlag ;//是否为指定的老师

    private Integer wordNum;
    private Integer readNum;
    private Integer listenNum;


}
