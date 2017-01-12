package com.boxfishedu.workorder.common.bean.multiteaching;

import java.util.HashMap;
import java.util.Map;

public enum SmallClassCardStatus {
    UNKNOWN(0, "未知"),
    CREATE(10, "创建"),
    COURSE_ASSIGNED(20, "分配课程"),
    TEACHER_ASSIGNED(30, "分配老师"),
    CARD_VALIDATED(1000, "工单校验通过"),
    //进入音视频房间失败
    VOICE_VIDEO_FAIL(1100, "接入言视频失败"),
    //进入视频房间成功
    CLASSING(1110, "开始上课"),
    SWITCH_STUDENT(1120, "上课切换学生"),
    COMPLETED(1200, "正常完成"),
    COMPLETED_FORCE(1210, "强制完成"),
    LEAVE_EARLY(1220, "教师早退");


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
