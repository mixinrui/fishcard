package com.boxfishedu.workorder.servicex.instantclass.config;

public enum InstantConfigEnum {
    UNKNOWN(0,"默认状态"),
    WAIT_TO_MATCH(10,"等待匹配教师"),
    ASK_TOO_BUSY(20,"您访问过于频繁"),
    HAVE_CLASS_IN_HALF_HOURS(30,"半小时内有即将上的课程,不能立即上课"),
    NOT_IN_RANGE(31,"该时间段不允许即时上课"),
    TUTOR_TYPE_NOT_SUPPORT(32,"目前只支持外教实时上课"),
    OUT_OF_NUM(33,"没有可上的课程"),
    FAIL_RECOMMAND_COURSE(34,"获取推荐课程失败"),
    NO_MATCH(40,"无可用教师"),
    MATCHED(50,"匹配上教师");

    private int code;
    private String desc;

    InstantConfigEnum(int code, String desc){
        this.desc = desc;
        this.code = code;
    }

    public static InstantConfigEnum getEnumByCode(int code){
        for (InstantConfigEnum instantClassRequestStatus: InstantConfigEnum.values()){
            if (instantClassRequestStatus.code == code)
                return instantClassRequestStatus;
        }
        return InstantConfigEnum.UNKNOWN;
    }

    public int getCode(){
        return this.code;
    }
    public String getDesc(){
        for (InstantConfigEnum instantClassRequestStatus: InstantConfigEnum.values()){
            if (instantClassRequestStatus.code == this.getCode())
                return instantClassRequestStatus.desc;
        }
        return null;
    }
}
