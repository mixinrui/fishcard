package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

public enum RedisTypeEnum {
    ORDER2SERVICE(1,"ORDER2SERVICE");

    private static Map<Integer, Enum> varMap = new HashMap<>();

    RedisTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    static {
        for (RedisTypeEnum v : RedisTypeEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static RedisTypeEnum get(int code) {
        if (varMap.containsKey(code)) {
            return (RedisTypeEnum) varMap.get(code);
        }
        return ORDER2SERVICE;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc == null ? ORDER2SERVICE.getDesc() : desc;
    }

}
