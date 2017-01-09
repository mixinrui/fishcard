package com.boxfishedu.workorder.common.bean.multiteaching;

import java.util.HashMap;
import java.util.Map;

public enum SmallClassCardStatus {
    UNKNOWN(0, "未知"),
    CREATE(100, "创建"),
    COURSE_ASSIGNED(110, "分配课程"),
    TEACHER_ASSIGNED(120, "分配老师"),
    CARD_VALIDATED(200, "工单校验通过"),
    //进入视频房间成功
    CLASSING(300, "开始上课"),
    SWITCH_STUDENT(330, "上课切换学生"),
    COMPLETED(400, "正常完成"),
    COMPLETED_FORCE(410, "强制完成"),
    LEAVE_EARLY(420, "教师早退");


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
