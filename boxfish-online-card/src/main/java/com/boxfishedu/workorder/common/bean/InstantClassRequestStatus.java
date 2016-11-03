package com.boxfishedu.workorder.common.bean;

public enum InstantClassRequestStatus {
    WAIT_TO_MATCH(10,"等待匹配教师"),
    ASK_TOO_BUSY(20,"您访问过于频繁"),//TODO:标准?
    HAVE_CLASS_IN_HALF_HOURS(30,"半小时内有即将上的课程,不能立即上课"),
    NO_MATCH(40,"无可用教师"),
    MATCHED(50,"匹配上教师");

    private int code;
    private String status;

    InstantClassRequestStatus(int code, String status){
        this.status = status;
        this.code = code;
    }
    public int getCode(){
        return this.code;
    }
    public String getStatus(){
        for (InstantClassRequestStatus instantClassRequestStatus: InstantClassRequestStatus.values()){
            if (instantClassRequestStatus.code == this.getCode())
                return instantClassRequestStatus.status;
        }
        return null;
    }
}
