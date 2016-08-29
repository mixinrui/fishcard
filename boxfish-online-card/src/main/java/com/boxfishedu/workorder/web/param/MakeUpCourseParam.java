package com.boxfishedu.workorder.web.param;

import lombok.Data;

/**
 * Created by hucl on 16/6/16.
 */
@Data
public class MakeUpCourseParam {
    private String userName;//为退款添加
    private Long workOrderId;
    private Integer timeSlotId;
    private String startTime;
    private String endTime;
    /** 更改鱼卡状态 **/
    private Integer fishStatus;
    private Long workOrderIds[];
    private String orderCode;
    private Boolean successFlag;
}
