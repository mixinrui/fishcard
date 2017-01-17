package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * 课程相关信息显示
 * Created by jiaozijun on 16/12/14.
 */
@Data
public class CourseInfo {
    private String courseId;
    private Long teacherId;
    private String teacherName;
    private String teacherImg;
    private boolean assignFlag ;//是否为指定的老师

    private Integer wordNum; //单词量
    private Integer readNum; //阅读量
    private Integer scoreNum;//积分

    private boolean showBeginClass; //课前五分钟内 显示该按钮


}
