package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * Created by ansel on 16/7/22.
 */
@Data
public class CommentCardForm {
    private Long id;
    private Long studentId;
    private String questionName;
    private String courseId;
    private String courseName;
    private String cover;
    private String askVoicePath;
    private Long voiceTime;
    private String teacherName;
    private String answerVideoPath;

    //测试用数据
    private Integer status;
    private Long answerVideoTime;
    private Long answerVideoSize;
}