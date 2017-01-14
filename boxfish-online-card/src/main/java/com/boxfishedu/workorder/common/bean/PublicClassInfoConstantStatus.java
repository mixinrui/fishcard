package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 17/1/14.
 */
public class PublicClassInfoConstantStatus {
    //UNKNOWN(0, "未知"),
    public static final int UNKNOWN = 0;

    //CREATE(10, "创建"),
    public static final int CREATE = 10;

    //COURSE_ASSIGNED(20, "分配课程")
    public static final int COURSE_ASSIGNED = 20;

    //TEACHER_ASSIGNED(30, "分配老师"),
    public static final int TEACHER_ASSIGNED = 30;

    /**
     * 学生状态
     */
    //STUDENT_ENTER(100, "学生校验通过"),
    public static final int STUDENT_ENTER = 100;

    //STUDENT_VOICE_VIDEO_SUCCESS(200, "学生接入音视频成功"),
    public static final int STUDENT_VOICE_VIDEO_SUCCESS = 200;

    //STUDENT_VOICE_VIDEO_FAIL(210, "学生接入言视频失败"),
    public static final int STUDENT_VOICE_VIDEO_FAIL = 210;

    //STUDENT_CLASSING(300, "学生正在上课"),
    public static final int STUDENT_CLASSING = 300;

    //STUDENT_ON_WHEAT(400, "学生上麦"),
    public static final int STUDENT_ON_WHEAT = 400;

    //STUDENT_OFF_WHEAT(410, "学生下麦"),
    public static final int STUDENT_OFF_WHEAT = 410;

    //STUDENT_LEAVE_UNACTIVE(500, "学生被动退出"),
    public static final int STUDENT_LEAVE_UNACTIVE = 500;

    //STUDENT_LEAVE_ACTIVE(510, "学生主动退出"),
    public static final int STUDENT_LEAVE_ACTIVE = 510;

    //主要给服务器用
    //STUDENT_COMPLETED(520, "学生正常完成"),
    public static final int STUDENT_COMPLETED = 520;

    //STUDENT_LEAVE_EARLY(530, "学生早退"),
    public static final int STUDENT_LEAVE_EARLY = 530;

    //STUDENT_QUIT(550,"学生退出"),
    public static final int STUDENT_QUIT = 550;


    /**
     * 教师状态
     */
    //TEACHER_CARD_VALIDATED(1000,"教师工单校验通过"),
    public static final int TEACHER_CARD_VALIDATED = 1000;

    //进入音视频房间失败
    //TEACHER_VOICE_VIDEO_FAIL(1100,"教师接入言视频失败"),
    public static final int TEACHER_VOICE_VIDEO_FAIL = 1100;

    //进入视频房间成功
    //TEACHER_CLASSING(1110,"教师开始上课"),
    public static final int TEACHER_CLASSING = 1110;

    //TEACHER_SWITCH_STUDENT(1120,"教师上课切换学生"),
    public static final int TEACHER_SWITCH_STUDENT = 1120;

    //TEACHER_COMPLETED(1200,"教师正常完成"),
    public static final int TEACHER_COMPLETED = 1200;

    //TEACHER_COMPLETED_FORCE(1210,"教师强制完成"),
    public static final int TEACHER_COMPLETED_FORCE = 1210;

    //TEACHER_LEAVE_EARLY(1220,"教师教师早退");
    public static final int TEACHER_LEAVE_EARLY = 1220;
}
