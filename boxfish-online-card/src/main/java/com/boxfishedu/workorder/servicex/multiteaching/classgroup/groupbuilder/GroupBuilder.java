package com.boxfishedu.workorder.servicex.multiteaching.classgroup.groupbuilder;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;

import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 17/1/6.
 * 完成小班课的group
 */
public abstract class GroupBuilder {

    protected abstract List<WorkOrder> cardsToGroup();

    protected abstract Map<String, List<WorkOrder>> groupByTime(List<WorkOrder> workOrders);

    protected abstract Map<String, List<WorkOrder>> groupbyLevel(List<WorkOrder> workOrders);

    protected abstract Map<String, List<WorkOrder>> groupByStudyCounter(List<WorkOrder> workOrders);

    protected abstract Map<String, List<WorkOrder>> groupByRelation(List<WorkOrder> workOrders);

//    public void group() {
//        List<WorkOrder> cards = this.cardsToGroup();
//        Map<String,WorkOrder>
//
//    }
}
