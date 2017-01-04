package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

public enum SmallClassCardStatusEnum {
    UNKNOWN(0, "未知"),
    TEACHER_ENTER_ROOM(100, "教师进入房间"),
    CLASSING(200, "开始上课"),
    BAD(300, "下课");

    private static Map<Integer, Enum> varMap = new HashMap<>();

    SmallClassCardStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    static {
        for (SmallClassCardStatusEnum v : SmallClassCardStatusEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static SmallClassCardStatusEnum get(int code) {
        if (varMap.containsKey(code)) {
            return (SmallClassCardStatusEnum) varMap.get(code);
        }
        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc == null ? UNKNOWN.getDesc() : desc;
    }

}
