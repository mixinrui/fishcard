package com.boxfishedu.mall.enums;

import org.springframework.util.StringUtils;

/**
 * Created by LuoLiBing on 16/8/16.
 */
public enum TutorType {

    CN,FRN,MIXED, UNKNOW;

    public static TutorType resolve(String tutorType) {
        if(StringUtils.isEmpty(tutorType)) {
            return UNKNOW;
        }
        switch (tutorType) {
            case "CN": return CN;
            case "FRN": return FRN;
            case "MIXED": return MIXED;
            default: return UNKNOW;
        }
    }
}
