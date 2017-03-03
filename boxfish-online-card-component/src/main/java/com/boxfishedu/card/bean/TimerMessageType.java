package com.boxfishedu.card.bean;

/**
 * Created by hucl on 16/6/10.
 */
public enum TimerMessageType {

    TEACHER_ASSIGN_NOTIFY(10),
    TEACHER_ASSIGN_NOTIFY_REPLY(11),
    TEACHER_OUT_NUM_NOTIFY(20),
    TEACHER_OUT_NUM_NOTIFY_REPLY(21),
    TEACHER_PREPARE_CLASS_NOTIFY(30),
    TEACHER_ABSENT_QUERY_NOTIFY(40),
    STUDENT_ABSENT_QUERY_NOTIFY(50),
    COMPLETE_FORCE_SERVER_NOTIFY(60),
    //学生没有补课的清空操作定时器
    STUDENT_NOT_MAKEUP_NOTIFY(70),
    //教师当天新匹配到的课程
    TEACHER_COURSE_NEW_ASSIGNEDED_DAY(80),
    //抢单初始化数据队列
    GRAB_ORDER_DATA_INIT(90),
    GRAB_ORDER_DATA_INIT_FOREIGN(91),
    //抢单格式化数据队列(清理历史数据中教)
    GRAB_ORDER_DATA_CLEAR_DAY(100),
    COMMENT_CARD_NO_ANSWER(110),
    //抢单格式化数据队列(清理历史数据外教)
    GRAB_ORDER_DATA_CLEAR_DAY_FOREIGH(101),

    //每天6点向老师发送课程变化的消息
    COURSE_CHANGER_WORKORDER(102),

    //每天6:30点向学生发送明天有几节课
    CLASSS_TOMO_STU_NOTIFY(105),

    //查询学生旷课扣积分队列
    STUDENT_ABSENT_DEDUCT_SCORE(106),

    //每天7点提醒老师今天有课
    CLASSS_TODY_TEA_NOTIFY(108),

    //冻结鱼卡处理
    FREEZE_UPDATE_HOME(120),

    // 课程推荐
    RECOMMEND_COURSES(130),

    // 自动确认状态
    AUTO_CONFIRM_STATUS(135),

    //即时上课
    INSTANT_CLASS(145),

    //标记超过一分钟未匹配教师的课程
    INSTANT_CLASS_MARK_UNMATCH(146),

    //超过35分钟没有匹配上学生的立即上课卡片,课程退回
    INSTANT_CLASS_BACK_COURSES(147),
    //指定老师
    INSTANT_ASSGIN_TEACHER(148),
    // 外教点评会员过期提醒
    EXPIRE_COMMENT_CARD(149),
    ASSGIN_TEACHER(150),

    // 公开课缓存清理
    EXPIRE_PUBLIC_CLASS(151),
    // 公开课提前5分钟推送
    PUBLIC_CLASS_NOTIFY(152),

    //创建小班课
    CREATE_SMALL_CLASS(154),
    //保存小班课学生的上课关系
    SMALLCLASS_STUDENTS_RELATION(155),

    //外教点评订单关闭
    CLOSE_COMMENT_CARD_ORDER(156),

    //向学生系统传入鱼卡状态(已完成)
    SNED_STUDENT_FISHCARD_STATUS(157);


    private int code;

    private TimerMessageType(int code) {
        this.code = code;
    }

    public int value() {
        return this.code;
    }

    @Override
    public String toString() {
        return String.valueOf(this.code);
    }
}
