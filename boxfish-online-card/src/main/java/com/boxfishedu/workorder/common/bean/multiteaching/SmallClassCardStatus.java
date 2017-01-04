package com.boxfishedu.workorder.common.bean.multiteaching;

import java.util.HashMap;
import java.util.Map;

public enum SmallClassCardStatus {
    UNKNOWN(0, "未知"),
    CREATE(100, "创建"),
    TEACHER_ENTER_ROOM(200, "教师进入课堂"),
    CLASSING(300,"开始上课"),
    COMPLETED(400, "完成");

    private static Map<Integer, Enum> varMap = new HashMap<>();

    SmallClassCardStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    static {
        for (SmallClassCardStatus v : SmallClassCardStatus.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static SmallClassCardStatus get(int code) {
        if (varMap.containsKey(code)) {
            return (SmallClassCardStatus) varMap.get(code);
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
