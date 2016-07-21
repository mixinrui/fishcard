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
    READ(600,"已读取"),
    OVERTIME(700,"教师超时未回答");

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
