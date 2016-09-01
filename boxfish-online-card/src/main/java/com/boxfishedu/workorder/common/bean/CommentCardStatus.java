package com.boxfishedu.workorder.common.bean;

/**
 * Created by ansel on 16/7/18.
 */
public enum CommentCardStatus {
    ASKED(100,"已提问"),
    REQUEST_ASSIGN_TEACHER(200,"请求分配教师"),
    ASSIGNED_TEACHER(300,"已分配教师"),
    ANSWERED(400,"已回答"),
    OVERTIME(500,"教师24小时未回答"),
    STUDENT_COMMENT_TO_TEACHER(600,"学生评价教师"),

    STUDENT_READ(1,"学生已读"),
    STUDENT_UNREAD(0,"学生未读"),
    TEACHER_READ(1,"外教已读"),
    TEACHER_UNREAD(0,"外教未读"),

    ASSIGN_TEACHER_ONCE(1,"第一次分配外教"),
    ASSIGN_TEACHER_TWICE(2,"第二次分配外教"),
    ASSIGN_TEACHER_TRIPLE(3,"强制换掉外教"),

    AMOUNT_ADD(1,"点评次数加1"),
    AMOUNT_MINUS(-1,"点评次数减1");
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
