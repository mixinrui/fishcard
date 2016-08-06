package com.boxfishedu.workorder.web.param.requester;

import lombok.Data;

/**
 * Created by hucl on 16/8/4.
 */
@Data
public class DataAnalysisLogParam {
    private Long userId;
    private Long startTime;
    private Long endTime;
    private String event;
}
