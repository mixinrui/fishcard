package com.boxfishedu.workorder.web.view.teacher;

import com.boxfishedu.workorder.web.view.common.DailyScheduleTimeView;
import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/4/14.
 */
@Data
public class MonthScheduleDataView {
    private String day;
    private List<DailyScheduleTimeView> dailyScheduleTime;
}
