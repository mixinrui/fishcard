package com.boxfishedu.workorder.entity.mysql;


/**
 * Created by ansel on 16/7/18.
 */
public enum CommentCardStatus {
    READ("已读",1),
    UNREAD("未读",0);

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

//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public int getCode() {
//        return code;
//    }
//
//    public void setCode(int code) {
//        this.code = code;
//    }
}
