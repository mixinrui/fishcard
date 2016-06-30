package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by hucl on 16/5/12.
 */
@Data
public class AvaliableTimeParam implements Serializable {
    private Long studentId;
    private Long roleId;
    //是否免费
    private Boolean isFree;
    private Long orderId;
    private String date;

    //给换课时候判断换课的可用时间使用
    private Long workOrderId;
}
