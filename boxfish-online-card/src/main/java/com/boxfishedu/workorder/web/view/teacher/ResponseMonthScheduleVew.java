package com.boxfishedu.workorder.web.view.teacher;

import com.boxfishedu.workorder.web.view.base.ResponseBaseView;
import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/4/14.
 * 获取课程表接口
 */
@Data
public class ResponseMonthScheduleVew extends ResponseBaseView{
    private List<MonthScheduleDataView> data;
}
