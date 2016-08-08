package com.boxfishedu.workorder.common.bean;

import java.util.HashMap;
import java.util.Map;

/**
 *  鱼卡退款状态
 */

public enum FishCardChargebackStatusEnum {
    UNKNOWN(0, "未知",""),
    NEED_RECHARGEBACK(10, "需确认发起退款",""),
    CONFIRM_SEND_RECHARGBACK(20, "已确认发起退款",""),
    RECHARGEBACK_SUCCESS(30, "退款成功",""),
    RECHARGEBACK_FAILED(40, "退款失败","");

    FishCardChargebackStatusEnum() {
    }

    FishCardChargebackStatusEnum(int code, String desc, String remark) {
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

    private static Map<Integer, FishCardChargebackStatusEnum> varMap = new HashMap<>();

    static {
        for (FishCardChargebackStatusEnum v : FishCardChargebackStatusEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static FishCardChargebackStatusEnum get(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code);
        }
        return UNKNOWN;
    }

    public static String getDesc(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code).getDesc();
        }
        return "未知";
    }

    public static String getRemark(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code).getRemark();
        }
        return "";
    }

}
