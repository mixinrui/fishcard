package com.boxfishedu.workorder.common.bean;

import com.boxfishedu.workorder.entity.mongo.NetPingAnalysisInfo;

import java.util.HashMap;
import java.util.Map;

public enum FishCardNetStatusEnum {
    UNKNOWN(0, "未知"),
    UNSTART(10, "未到上课时间无数据"),
    CLASSING(20, "正在上课,数据未统计"),
    BAD(30, "网络较差"),
    GENERAL(40, "网络一般"),
    GOOD(50, "网络较好");

    private static Map<Integer, Enum> varMap = new HashMap<>();

    FishCardNetStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    static {
        for (FishCardNetStatusEnum v : FishCardNetStatusEnum.values()) {
            varMap.put(v.getCode(), v);
        }
    }

    public static FishCardNetStatusEnum get(int code) {
        if (varMap.containsKey(code)) {
            return (FishCardNetStatusEnum) varMap.get(code);
        }
        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc == null ? UNKNOWN.getDesc() : desc;
    }

    public static FishCardNetStatusEnum anaLysis(NetPingAnalysisInfo netPingAnalysisInfo,double bad,double general) {
        double max = netPingAnalysisInfo.getMaxInternetPing() > netPingAnalysisInfo.getMaxServicePing()
                ? netPingAnalysisInfo.getMaxInternetPing() : netPingAnalysisInfo.getMaxServicePing();
        if(max==(double)0){
            return UNKNOWN;
        }
        if(max<(double)general){
            return GOOD;
        }
        if(max<(double)bad){
            return GENERAL;
        }
        return BAD;
    }

}
