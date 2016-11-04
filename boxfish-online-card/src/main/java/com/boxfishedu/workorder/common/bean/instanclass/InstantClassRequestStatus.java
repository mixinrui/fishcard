package com.boxfishedu.workorder.common.bean.instanclass;

public enum InstantClassRequestStatus {
    UNKNOWN(0,"默认状态"),
    WAIT_TO_MATCH(10,"等待匹配教师"),
    ASK_TOO_BUSY(20,"您访问过于频繁"),
    HAVE_CLASS_IN_HALF_HOURS(30,"半小时内有即将上的课程,不能立即上课"),
    NOT_IN_RANGE(31,"该时间段不允许即时上课"),
    OUT_OF_NUM(32,"没有可上的课程"),//课程表入口点击进入,没有课程的时候返回
    NO_MATCH(40,"无可用教师"),
    MATCHED(50,"匹配上教师");

    private int code;
    private String desc;

    InstantClassRequestStatus(int code, String desc){
        this.desc = desc;
        this.code = code;
    }

    public static InstantClassRequestStatus getEnumByCode(int code){
        for (InstantClassRequestStatus instantClassRequestStatus: InstantClassRequestStatus.values()){
            if (instantClassRequestStatus.code == code)
                return instantClassRequestStatus;
        }
        return InstantClassRequestStatus.UNKNOWN;
    }

    public int getCode(){
        return this.code;
    }
    public String getDesc(){
        for (InstantClassRequestStatus instantClassRequestStatus: InstantClassRequestStatus.values()){
            if (instantClassRequestStatus.code == this.getCode())
                return instantClassRequestStatus.desc;
        }
        return null;
    }
}
