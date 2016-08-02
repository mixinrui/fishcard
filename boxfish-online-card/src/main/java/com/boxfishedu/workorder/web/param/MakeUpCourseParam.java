package com.boxfishedu.workorder.web.param;

import lombok.Data;

/**
 * Created by hucl on 16/6/16.
 */
@Data
public class MakeUpCourseParam {
    private Long workOrderId;
    private Integer timeSlotId;
    private String startTime;
    private String endTime;
}
