package com.boxfishedu.workorder.web.view.common;

import lombok.Data;

/**
 * Created by hucl on 16/3/18.
 */
@Data
public class TimeSlotView {
    private Long id;
    private String startTime;
    private Integer count;
    private String endTime;
    private Integer index;
}
