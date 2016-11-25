package com.boxfishedu.workorder.common.bean.instanclass;

import org.joda.time.Minutes;

public enum InstantClassRequestStatus {
    UNKNOWN(0,"默认状态"),
    WAIT_TO_MATCH(10,"正在呼叫外教..."),
    ASK_TOO_BUSY(20,"您访问过于频繁"),
    HAVE_CLASS_IN_HALF_HOURS(30,"半小时内有即将上的课程,不能立即上课"),
    NOT_IN_RANGE(31,"该时间段不允许即时上课"),
    TUTOR_TYPE_NOT_SUPPORT(32,"目前只支持外教实时上课"),
    OUT_OF_NUM(33,"先去购买外教课程吧,然后才能实时上课~"),
    FAIL_RECOMMAND_COURSE(34,"暂时没有与你Level匹配的课程,请调整起点Level或者稍后再试吧"),
    MATCHED_LESS_THAN_30MINUTES(35,"半小时之内有匹配上的课程"),
    NO_MATCH(40,"现在是上课高峰,稍后再来试试吧"),
    MATCHED(50,"老师即将开始上课...");

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
