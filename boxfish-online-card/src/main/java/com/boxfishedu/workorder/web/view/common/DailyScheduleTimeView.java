package com.boxfishedu.workorder.web.view.common;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by hucl on 16/4/14.
 */
@Data
public class DailyScheduleTimeView implements Serializable{
    private Long slotId;
    private String startTime;
    private String endTime;
    private boolean selected;
    private String courseId;
    private String courseName;
}
