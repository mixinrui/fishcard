package com.boxfishedu.workorder.common.bean.instanclass;

public enum TeacherInstantClassStatus {
    FAIL_TO_MATCH(10,"未匹配到学生"),
    MATCHED(50,"匹配成功");

    private int code;
    private String desc;

    TeacherInstantClassStatus(int code, String desc){
        this.desc = desc;
        this.code = code;
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
