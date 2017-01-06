package com.boxfishedu.workorder.servicex.multiteaching.classgroup.groupbuilder;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.multiteaching.statusdealer.SmallClassEvent;
import com.google.common.collect.Maps;

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

    protected abstract void initGroup(Map<String, List<WorkOrder>> groups);

    protected abstract WorkOrder selectLeader(List<WorkOrder> workOrders);


    public void group() {
        //小班群组
        Map<String, List<WorkOrder>> groups = Maps.newHashMap();

        List<WorkOrder> cards = this.cardsToGroup();

        Map<String, List<WorkOrder>> timeGrouped = this.groupByTime(cards);

        timeGrouped.forEach((timeKey, timedWorkOrders) -> {
            Map<String, List<WorkOrder>> levelGrouped = this.groupbyLevel(timedWorkOrders);

            levelGrouped.forEach((levelKey, leveledWorkOrders) -> {
                Map<String, List<WorkOrder>> studyCounterGrouped = this.groupByStudyCounter(leveledWorkOrders);

                studyCounterGrouped.forEach((studyCounterKey, studyCounterWorkOrders) -> {
                    Map<String, List<WorkOrder>> relationGrouped = this.groupByRelation(studyCounterWorkOrders);
                    groups.putAll(relationGrouped);
                });
            });
        });

        this.initGroup(groups);
    }
}
