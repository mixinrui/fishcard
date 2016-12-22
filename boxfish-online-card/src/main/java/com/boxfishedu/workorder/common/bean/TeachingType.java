package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangshichao on 16/3/23.
 */
public enum TeachingType {

    UNKNOWN(0, "未知", ""),
    ZHONGJIAO(1,"中教",""),
    WAIJIAO(2, "外教", ""),
    DEMO(3,"DEMO","");


    TeachingType() {
    }

    TeachingType(int code, String desc, String remark) {
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

    private static Map<Integer, Enum> varMap = new HashMap<>();

    static {
        for (TeachingType v : TeachingType.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static Enum get(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code);
        }
        return UNKNOWN;
    }

    public static String getDesc(int code) {
        if (varMap.containsKey(code)) {
            return ((TeachingType) varMap.get(code)).getDesc();
        }
        return "未知";
    }

    public static String getRemark(int code) {
        if (varMap.containsKey(code)) {
            return ((TeachingType) varMap.get(code)).getRemark();
        }
        return "";
    }

    public TutorTypeEnum teachingType2TutorType(){
        switch(this){
            case ZHONGJIAO:
                return TutorTypeEnum.CN;
            case WAIJIAO:
                return TutorTypeEnum.FRN;
            default:
                return TutorTypeEnum.UNKNOWN;
        }
    }

}
