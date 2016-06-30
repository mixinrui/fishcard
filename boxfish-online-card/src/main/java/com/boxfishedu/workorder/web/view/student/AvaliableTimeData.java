package com.boxfishedu.workorder.web.view.student;

import com.boxfishedu.workorder.web.view.common.WeekView;
import lombok.Data;

/**
 * Created by hucl on 16/3/18.
 */
@Data
public class AvaliableTimeData {
    private Long roleId;
    private WeekView days;
}
