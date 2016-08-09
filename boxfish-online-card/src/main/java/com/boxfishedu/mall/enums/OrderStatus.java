package com.boxfishedu.mall.enums;

/**
 * Created by lauzhihao on 2016/06/07.
 */
public enum OrderStatus {
    WAIT_PAY,   //待支付
    PAID,       //已支付
    CHOSE,      //已选课
    FINISHED,   //已完成
    CLOSED,     //已关闭
    REFUNDING,  //退款中(暂未使用)
    REFUNDED;   //退款完成(暂未使用)
}
