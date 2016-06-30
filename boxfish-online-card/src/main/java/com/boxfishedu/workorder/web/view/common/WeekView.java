package com.boxfishedu.workorder.web.view.common;

import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/3/18.
 */
@Data
public class WeekView {
    private List<TimeSlotView> day1;
    private List<TimeSlotView> day2;
    private List<TimeSlotView> day3;
    private List<TimeSlotView> day4;
    private List<TimeSlotView> day5;
    private List<TimeSlotView> day6;
    private List<TimeSlotView> day7;
}
