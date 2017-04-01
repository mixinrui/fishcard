package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import java.util.Date;

/**
 * Created by ansel on 2017/3/20.
 */
@Data
public class MonitorResponseForm {
    private Date startTime;

    private Date endTime;

    private Long sum;

    public MonitorResponseForm(Date startTime, Date endTime, Long sum){
        this.startTime = startTime;
        this.endTime = endTime;
        this.sum = sum;
    }
}
