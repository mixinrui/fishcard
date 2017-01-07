package com.boxfishedu.workorder.common.log;

import lombok.Data;

/**
 * Created by LuoLiBing on 17/1/6.
 */
@Data
public class ServiceLog extends BaseLog {

    public ServiceLog(Long userId) {
        this();
        this.UserId = userId;
    }

    public ServiceLog() {
        this.ModuleCode = "service";
        this.BusinessObject = "service";
    }
}
