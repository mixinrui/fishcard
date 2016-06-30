package com.boxfishedu.workorder.service.workorderlog;

import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;

import java.util.Comparator;

/**
 * Created by hucl on 16/6/25.
 */
public class WorkOrderLogComparator implements Comparator<WorkOrderLog> {
    public int compare(WorkOrderLog workOrderLog1, WorkOrderLog workOrderLog2) {
        if (workOrderLog1.getCreateTime().after(workOrderLog2.getCreateTime())) {
            return 1;
        } else if (workOrderLog1.getCreateTime().equals(workOrderLog2.getCreateTime())) {
            return 1;
        } else {
            return -1;
        }
    }
}
