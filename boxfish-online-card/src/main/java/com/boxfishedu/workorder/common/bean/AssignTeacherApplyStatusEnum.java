package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

public enum AssignTeacherApplyStatusEnum {
    // 0 不匹配  1 匹配  2 无时间片待匹配
    NO(0, "不匹配",""),
    YES(1, "匹配",""),
    WAITING(2, "无时间片待匹配","");
    AssignTeacherApplyStatusEnum() {
    }

    AssignTeacherApplyStatusEnum(int code, String desc, String remark) {
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

    private static Map<Integer, AssignTeacherApplyStatusEnum> varMap = new HashMap<>();

    static {
        for (AssignTeacherApplyStatusEnum v : AssignTeacherApplyStatusEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static AssignTeacherApplyStatusEnum get(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code);
        }
        return null;
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
