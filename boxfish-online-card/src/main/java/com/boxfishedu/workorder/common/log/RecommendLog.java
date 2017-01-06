package com.boxfishedu.workorder.common.log;

import lombok.Data;

/**
 * Created by LuoLiBing on 17/1/6.
 */
@Data
public class RecommendLog extends BaseLog {

    public RecommendLog(Long userId) {
        this();
        this.UserId = userId;
    }

    public RecommendLog() {
        this.ModuleCode = "recommend";
        this.BusinessObject = "courseSchedule";
    }
}
