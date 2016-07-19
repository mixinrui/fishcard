package com.boxfishedu.workorder.common.bean;


/**
 * Created by ansel on 16/7/18.
 */
public enum CommentCardStatus {
    ASKED("已提问",100),
    REQUEST_ASSIGN_TEACHER("请求分配教师",200),
    ASSIGNED_TEACHER("已分配教师",300),
    ANSWERED("已回答",400),
    UNREAD("未读取",500),
    READ("已读取",600),
    OVERTIME("教师超时未回答",700);

    private int code;
    private String status;

    CommentCardStatus(String status,int code){
        this.status = status;
        this.code = code;
    }
    public static int getCode(String status){
        for (CommentCardStatus commentCardStatus: CommentCardStatus.values()){
            if (commentCardStatus.status == status)
                return commentCardStatus.code;
        }
        return -1;
    }
}
