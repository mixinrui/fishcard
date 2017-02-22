package com.boxfishedu.workorder.common.bean;


/**
 * 课程难度
 */

import java.util.HashMap;
import java.util.Map;

public enum LevelEnum {
    UNKNOWN(0, "未知", ""),
    LEVEL_1(1, "LEVEL_1", ""),
    LEVEL_2(2, "LEVEL_2", ""),
    LEVEL_3(3, "LEVEL_3", ""),
    LEVEL_4(4, "LEVEL_4", ""),
    LEVEL_5(5, "LEVEL_5", ""),
    LEVEL_6(6, "LEVEL_6", ""),
    LEVEL_7(7, "LEVEL_7", ""),
    LEVEL_8(8, "LEVEL_8", "");


    LevelEnum() {
    }

    LevelEnum(int code, String desc, String remark) {
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

    private static Map<Integer, LevelEnum> varMap = new HashMap<>();

    static {
        for (LevelEnum v : LevelEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static LevelEnum get(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code);
        }
        return UNKNOWN;
    }

    public static String getDesc(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code).getDesc();
        }
        return "未知:[" + code + "]";
    }

    public static String getRemark(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code).getRemark();
        }
        return "";
    }

}
