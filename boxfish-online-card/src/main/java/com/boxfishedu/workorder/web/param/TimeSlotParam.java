package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/4/26.
 */
@Data
public class TimeSlotParam {
    private Long orderId;
    private Integer productType;
    private List<SelectedTime> selectedTimes;
}
