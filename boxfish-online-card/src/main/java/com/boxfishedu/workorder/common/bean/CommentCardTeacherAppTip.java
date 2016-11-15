package com.boxfishedu.workorder.common.bean;

/**
 * Created by ansel on 16/11/11.
 */
public enum CommentCardTeacherAppTip {
    COMMENT_CARD_NON_EXISTENT(0,"点评卡不存在"),
    COMMENT_CARD_ADOPT(200,"允许点评"),
    COMMENTED(400,"老师已经点评过此点评卡"),
    COMMENT_CARD_TIME_OUT(500,"点评卡已经超时");

    private int code;
    private String status;

    CommentCardTeacherAppTip(int code, String status){
        this.status = status;
        this.code = code;
    }
    public int getCode(){
        return this.code;
    }
    public static String getStatus(int code){
        for (CommentCardTeacherAppTip commentCardTeacherAppTip: CommentCardTeacherAppTip.values()){
            if (commentCardTeacherAppTip.code == code)
                return commentCardTeacherAppTip.status;
        }
        return null;
    }
}
