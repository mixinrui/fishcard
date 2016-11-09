package com.boxfishedu.workorder.common.bean.instanclass;

public enum TeacherInstantClassStatus {
    UNKNOWN(0,"未知"),
    FAIL_TO_MATCH(10,"抢单失败"),
    MATCHED(20,"抢单成功");

    private int code;
    private String desc;

    TeacherInstantClassStatus(int code, String desc){
        this.desc = desc;
        this.code = code;
    }
    public static TeacherInstantClassStatus getEnumByCode(int code){
        for (TeacherInstantClassStatus teacherInstantClassStatus: TeacherInstantClassStatus.values()){
            if (teacherInstantClassStatus.code == code)
                return teacherInstantClassStatus;
        }
        return TeacherInstantClassStatus.UNKNOWN;
    }

    public int getCode(){
        return this.code;
    }
    public String getDesc(){
        for (TeacherInstantClassStatus instantClassRequestStatus: TeacherInstantClassStatus.values()){
            if (instantClassRequestStatus.code == this.getCode())
                return instantClassRequestStatus.desc;
        }
        return null;
    }
}
