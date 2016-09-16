package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

public enum FishCardStatusEnum {
    UNKNOWN(0, "未知",""),
    CREATED(10, "创建",""),
    COURSE_ASSIGNED(20, "分配课程",""),
    TEACHER_ASSIGNED(30, "分配教师",""),//分配教师以后其实就已经是就绪,目前这两个状态有重叠
    WAITFORSTUDENT(31, "等待学生上课应答",""),
    //添加学生主动进入房间的状态
    STUDENT_ENTER_ROOM(36,"学生进入房间",""),
    //师生连通关系介于等待与接受之间
    CONNECTED(35,"师生已连通",""),
    STUDENT_ACCEPTED(32,"学生接受上课请求",""),
    //学生资源准备完成
    READY(33, "就绪",""),
    ONCLASS(34, "正在上课",""),
    COMPLETED(40, "正常完成",""),
    COMPLETED_FORCE(41,"强制完成",""),
    //下课5分钟后强制下课
    COMPLETED_FORCE_SERVER(42,"服务器强制完成",""),
    COMPLETED_BEYOND_MAKEUP_TIME(43,"超出补课期限,强制完成",""),
    TEACHER_ABSENT(50,"教师旷课",""),
    STUDENT_ABSENT(51,"学生旷课",""),
    TEACHER_LEAVE_EARLY(52,"教师早退",""),
    STUDENT_LEAVE_EARLY(53,"学生早退",""),
    TEACHER_CANCEL_PUSH(54,"教师取消请求上课",""),
    FISHCARD_CANCELED(55,"退课",""),
    //客户端双方都上报信息
    EXCEPTION(60, "系统异常",""),
    EXCEPTION_RESOURCE_DOWNLOAD_FAIL(61,"资源下载异常",""),
    //学生旷课扣积分
    DEDUCT_SCORE(70,"旷课已扣积分","");

    FishCardStatusEnum() {
    }

    FishCardStatusEnum(int code, String desc, String remark) {
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

    private static Map<Integer, FishCardStatusEnum> varMap = new HashMap<>();

    static {
        for (FishCardStatusEnum v : FishCardStatusEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static FishCardStatusEnum get(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code);
        }
        return UNKNOWN;
    }

    public static String getDesc(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code).getDesc();
        }
        return "未知:["+code+"]";
    }

    public static String getRemark(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code).getRemark();
        }
        return "";
    }

}
