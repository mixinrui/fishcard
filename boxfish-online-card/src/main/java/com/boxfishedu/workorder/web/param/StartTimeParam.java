package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.util.Date;

/**
 * Created by jiaozijun on 16/9/2.
 */
@Data
public class StartTimeParam {
    private Long workOrderId;

    private String beginDate;

    private String endDate;

    private Date beginDateFormat;

    private Date endDateFormat;

    private int timeslotId;

}
