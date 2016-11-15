package com.boxfishedu.workorder.common.bean;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  鱼卡退款状态(退款进度)
 */

public enum FishCardChargebackStatusEnum {
    UNKNOWN(0, "其它",""),
    /**  等待人工确认点击退款状态  **/
    NEED_RECHARGEBACK(10, "待退款",""),
    /**  人工确认点击退款状态  **/
    RECHARGBACKING(20, "退款中",""),
    /**  退款成功  **/
    RECHARGEBACK_SUCCESS(30, "退款成功",""),
    /**  退款失败   **/
    RECHARGEBACK_FAILED(40, "退款失败",""),
    /**  驳回 **/
    REFUSED(50, "退款驳回","");

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

    public static Map<Integer, String> varMapout = new HashMap<>();

    static {
        for (FishCardChargebackStatusEnum v : FishCardChargebackStatusEnum.values()) {
            varMap.put(v.getCode(), v);
        }
        for (FishCardChargebackStatusEnum v : FishCardChargebackStatusEnum.values()) {
            varMapout.put(v.getCode(), v.getDesc());
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
        return "其它";
    }

    public static String getRemark(int code) {
        if (varMap.containsKey(code)) {
            return varMap.get(code).getRemark();
        }
        return "";
    }

}
