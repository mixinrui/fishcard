package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/5/16.
 */
@Data
public class CourseChangeParam {
    private Long studentId;
    private Long orderId;

    //单个鱼卡时使用该参数
    private Long workOrderId;

    //批量换课
    private List<Long> workOrderIds;
}
