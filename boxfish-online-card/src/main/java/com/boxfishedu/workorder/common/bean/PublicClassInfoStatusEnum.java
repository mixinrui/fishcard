package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LuoLiBing on 17/1/11.
 */
public enum PublicClassInfoStatusEnum {

    /**
     * 应用状态
     */
    UNKNOWN(0, "未知"),
    CREATE(10, "创建"),
    COURSE_ASSIGNED(20, "分配课程"),
    TEACHER_ASSIGNED(30, "分配老师"),

    /**
     * 学生状态
     */
    STUDENT_ENTER(100, "学生校验通过"),
    STUDENT_VOICE_VIDEO_SUCCESS(200, "学生接入音视频成功"),
    STUDENT_VOICE_VIDEO_FAIL(210, "学生接入言视频失败"),
    STUDENT_CLASSING(300, "学生正在上课"),
    STUDENT_ON_WHEAT(400, "学生上麦"),
    STUDENT_OFF_WHEAT(410, "学生下麦"),
    STUDENT_LEAVE_UNACTIVE(500, "学生被动退出"),
    STUDENT_LEAVE_ACTIVE(510, "学生主动退出"),

    //主要给服务器用
    STUDENT_COMPLETED(520, "学生正常完成"),
    STUDENT_LEAVE_EARLY(530, "学生早退"),

    STUDENT_QUIT(550, "学生退出"),


    /**
     * 教师状态
     */
    TEACHER_CARD_VALIDATED(1000, "教师工单校验通过"),
    //进入音视频房间失败
    TEACHER_VOICE_VIDEO_FAIL(1100, "教师接入言视频失败"),
    //进入视频房间成功
    TEACHER_CLASSING(1110, "教师开始上课"),
    TEACHER_SWITCH_STUDENT(1120, "教师上课切换学生"),
    TEACHER_COMPLETED(1200, "教师正常完成"),
    TEACHER_COMPLETED_FORCE(1210, "教师强制完成"),
    TEACHER_LEAVE_EARLY(1220, "教师早退");


    private static Map<Integer, Enum> varMap = new HashMap<>();

    PublicClassInfoStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    public static final int WOCODE=PublicClassInfoStatusEnum.UNKNOWN.getCode();

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
