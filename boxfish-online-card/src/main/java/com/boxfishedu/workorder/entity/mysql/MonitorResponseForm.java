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

    private Integer sum;

    public MonitorResponseForm(Date startTime, Date endTime, Integer sum){
        this.startTime = startTime;
        this.endTime = endTime;
        this.sum = sum;
    }
}
