package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

public enum ScheduleTypeEnum {
    UNKNOWN(0, "UNKNOWN",""),
    NORMAL(1,"NORMAL",""),
    TRIAL(10, "TRIAL","");


    ScheduleTypeEnum() {
    }

    ScheduleTypeEnum(int code, String desc, String remark) {
        this.code = code;
        this.desc = desc;
        this.remark = remark;
    }

    private int code;
    private String desc;
    private String remark;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getRemark() {
        return remark;
    }

    private static Map<Integer, ScheduleTypeEnum> varMap = new HashMap<>();

    static {
        for (ScheduleTypeEnum v : ScheduleTypeEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static ScheduleTypeEnum get(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code);
        }
        return UNKNOWN;
    }

    public static String getDesc(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code).getDesc();
        }
        return "未知";
    }

    public static String getRemark(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code).getRemark();
        }
        return "";
    }

}
