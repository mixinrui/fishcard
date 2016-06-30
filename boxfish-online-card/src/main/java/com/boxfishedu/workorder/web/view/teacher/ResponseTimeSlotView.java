package com.boxfishedu.workorder.web.view.teacher;

import com.boxfishedu.workorder.web.view.base.ResponseBaseView;
import com.boxfishedu.workorder.web.view.common.TimeSlotView;
import lombok.Data;

/**
 * Created by hucl on 16/3/22.
 */
@Data
public class ResponseTimeSlotView extends ResponseBaseView {
    private TimeSlotView data;
}
