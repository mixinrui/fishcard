package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

public enum FishCardAuthEnum {
    UNKNOWN(0, "未知",""),
    NOT_EXISTS(1,"不存在对应的工单",""),
    TOO_EARLY(10, "未到上课时间",""),
    TOO_LATE(20, "迟到未在上课时间",""),
    OK(30, "分配教师","");


    FishCardAuthEnum() {
    }

    FishCardAuthEnum(int code, String desc, String remark) {
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

    private static Map<Integer, FishCardAuthEnum> varMap = new HashMap<>();

    static {
        for (FishCardAuthEnum v : FishCardAuthEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static FishCardAuthEnum get(int code) {
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
