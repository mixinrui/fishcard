package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/6/16.
 */
public enum TutorTypeEnum {
    CN("CN"),
    FRN("FRN"),
    MIXED("MIXED"),
    UNKNOWN("UNKNOWN");

    private String value;

    TutorTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static TutorTypeEnum getByValue(String value){
        for(TutorTypeEnum tutorTypeEnum:TutorTypeEnum.values()){
            if(tutorTypeEnum.toString().equals(value)){
                return tutorTypeEnum;
            }
        }
        return TutorTypeEnum.UNKNOWN;
    }
}