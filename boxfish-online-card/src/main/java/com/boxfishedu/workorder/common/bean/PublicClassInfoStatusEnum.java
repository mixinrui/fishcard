package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LuoLiBing on 17/1/11.
 */
public enum PublicClassInfoEnum {
    UNKNOWN(0, "未知"),
    ENTER(100, "校验通过"),
    VOICE_VIDEO_SUCCESS(200, "接入音视频成功"),
    VOICE_VIDEO_FAIL(210, "接入言视频失败"),
    CLASSING(300, "正在上课"),
    ON_WHEAT(400, "上麦"),
    OFF_WHEAT(410, "下麦"),
    COMPLETED(500, "正常完成"),
    COMPLETED_FORCE(510, "强退"),
    LEAVE_EARLY(520, "早退");


    private static Map<Integer, Enum> varMap = new HashMap<>();

    PublicClassInfoEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    static {
        for (PublicClassInfoEnum v : PublicClassInfoEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static PublicClassInfoEnum get(int code) {
        if (varMap.containsKey(code)) {
            return (PublicClassInfoEnum) varMap.get(code);
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
