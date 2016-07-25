package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * Created by ansel on 16/7/22.
 */
@Data
public class CommentCardForm {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long questionId;
    private String questionName;
    private String courseId;
    private String courseName;
    private String cover;
    private Long askVoiceId;
    private String askVoicePath;
    private String teacherName;
    private String answerVideoPath;
}
