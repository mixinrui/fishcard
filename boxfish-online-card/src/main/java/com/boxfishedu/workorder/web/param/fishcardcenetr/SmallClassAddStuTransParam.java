package com.boxfishedu.workorder.web.param.fishcardcenetr;

import lombok.Data;

import java.util.List;

/**
 * 小班课补学生向订单发送请求
 * jiaozijun
 */
@Data
public class SmallClassAddStuTransParam {

    //小班课id
    private  Long smallClassId;

    private Long userId;

    private String couponCode;


    //couponCode userId smallClassKey
    private String sign;


}
