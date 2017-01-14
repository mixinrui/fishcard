package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LuoLiBing on 17/1/11.
 */
public enum PublicClassInfoStatusEnum {
    //学生端
    UNKNOWN(0, "未知"),
    STUDENT_ENTER(100, "校验通过"),
    STUDENT_VOICE_VIDEO_SUCCESS(200, "接入音视频成功"),
    STUDENT_VOICE_VIDEO_FAIL(210, "接入言视频失败"),
    STUDENT_CLASSING(300, "正在上课"),
    STUDENT_ON_WHEAT(400, "上麦"),
    STUDENT_OFF_WHEAT(410, "下麦"),
    STUDENT_LEAVE_UNACTIVE(500, "被动退出"),
    STUDENT_LEAVE_ACTIVE(510, "主动退出"),

    //主要给服务器用
    STUDENT_COMPLETED(520, "正常完成"),
    STUDENT_LEAVE_EARLY(530, "早退"),

    STUDENT_QUIT(550, "退出"),


    //教师端
    CREATE(10, "创建"),
    COURSE_ASSIGNED(20, "分配课程"),
    TEACHER_ASSIGNED(30, "分配老师"),
    TEACHER_CARD_VALIDATED(1000, "工单校验通过"),
    //进入音视频房间失败
    TEACHER_VOICE_VIDEO_FAIL(1100, "接入言视频失败"),
    //进入视频房间成功
    TEACHER_CLASSING(1110, "开始上课"),
    TEACHER_SWITCH_STUDENT(1120, "上课切换学生"),
    TEACHER_COMPLETED(1200, "正常完成"),
    TEACHER_COMPLETED_FORCE(1210, "强制完成"),
    TEACHER_LEAVE_EARLY(1220, "教师早退");


    private static Map<Integer, Enum> varMap = new HashMap<>();

    PublicClassInfoStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    static {
        for (PublicClassInfoStatusEnum v : PublicClassInfoStatusEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static PublicClassInfoStatusEnum get(int code) {
        if (varMap.containsKey(code)) {
            return (PublicClassInfoStatusEnum) varMap.get(code);
        }
        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }

    public static PublicClassInfoStatusEnum getByCode(int code) {
        for (PublicClassInfoStatusEnum publicClassInfoStatusEnum : PublicClassInfoStatusEnum.values()) {
            if (publicClassInfoStatusEnum.getCode() == code) {
                return publicClassInfoStatusEnum;
            }
        }
        return UNKNOWN;
    }

    public String getDesc() {
        return desc == null ? UNKNOWN.getDesc() : desc;
    }
}
