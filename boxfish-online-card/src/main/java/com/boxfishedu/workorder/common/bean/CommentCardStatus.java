package com.boxfishedu.workorder.common.bean;

/**
 * Created by ansel on 16/7/18.
 */
public enum CommentCardStatus {
    ASKED(100,"已提问"),
    REQUEST_ASSIGN_TEACHER(200,"请求分配教师"),
    ASSIGNED_TEACHER(300,"已分配教师"),
    ANSWERED(400,"已回答"),
    UNREAD(500,"未读取"),
    TEACHER_UNREADED(510,"教师未读取"),
    READ(600,"已读取"),
    TEACHER_READED(610,"教师已读取"),
    OVERTIME(700,"教师超时未回答"),
    STUDENT_COMMENT_TO_TEACHER(800,"学生评价教师"),
    STUDENT_COMMENT_UNREADED(810,"教师未读取学生评价"),
    STUDENT_COMMENT_READED(820,"教师已读取教师评价");

    private int code;
    private String status;

    CommentCardStatus(int code,String status){
        this.status = status;
        this.code = code;
    }
    public int getCode(){
        return this.code;
    }
    public static String getStatus(int code){
        for (CommentCardStatus commentCardStatus: CommentCardStatus.values()){
            if (commentCardStatus.code == code)
                return commentCardStatus.status;
        }
        return null;
    }
}
