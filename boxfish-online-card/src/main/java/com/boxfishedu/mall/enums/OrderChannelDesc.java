package com.boxfishedu.mall.enums;

/**
 * 订单渠道 含有描述
 * @author jiaozijun
 */
public enum OrderChannelDesc {

    STANDARD(  "STANDARD","标准付费订单",""),
    EXPERIENCE("EXPERIENCE","免费体验订单",""),
    ADJUST(    "ADJUST","后台调整订单",""),
    EXCHANGE(  "EXCHANGE","金币兑换订单","");

    OrderChannelDesc(){}

    OrderChannelDesc(String code,String desc,String remark){
        this.code = code;
        this.desc = desc;
        this.remark =remark;
    }

    private String code;
    private String desc;
    private String remark;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getRemark() {  return remark;  }
}
