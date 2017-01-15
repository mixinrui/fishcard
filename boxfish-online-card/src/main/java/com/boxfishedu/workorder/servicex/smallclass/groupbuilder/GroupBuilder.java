package com.boxfishedu.workorder.servicex.smallclass.groupbuilder;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    protected abstract void initGroup(Map<Integer, List<WorkOrder>> groups);

    protected abstract WorkOrder selectLeader(List<WorkOrder> workOrders);

    protected abstract Integer smallClassMemeberNum();

    public void group() {
        List<WorkOrder> cards = this.cardsToGroup();

        Map<String, List<WorkOrder>> timeGrouped = this.groupByTime(cards);

        timeGrouped.forEach((timeKey, timedWorkOrders) -> {
            Map<String, List<WorkOrder>> levelGrouped = this.groupbyLevel(timedWorkOrders);

            levelGrouped.forEach((key, workOrders) -> {
                Map<Integer, List<WorkOrder>> finalGroups = this.divideGroup(workOrders);
                this.initGroup(finalGroups);
            });

//            levelGrouped.forEach((levelKey, leveledWorkOrders) -> {
//                Map<String, List<WorkOrder>> studyCounterGrouped = this.groupByStudyCounter(leveledWorkOrders);
//
//                Integer groupSeq = 0;
            //TODO:等待确定规则
//                studyCounterGrouped.forEach((studyCounterKey, studyCounterWorkOrders) -> {
//                    Map<String, List<WorkOrder>> relationGrouped = this.groupByRelation(studyCounterWorkOrders);
////                    groups.putAll(relationGrouped);
//                });
        });
    }

    //将小班按照信息获取
    protected Map<Integer, List<WorkOrder>> divideGroup(List<WorkOrder> workOrders) {
        Integer memNum = this.smallClassMemeberNum();
        Map<Integer, List<WorkOrder>> finalGroup = new HashMap<>();

        Integer groupCounter = 0;
        List<WorkOrder> currentGroup = null;
        for (int i = 0; i < workOrders.size(); i++) {
            if (Objects.isNull(currentGroup)) {
                currentGroup = Lists.newArrayList();
                finalGroup.put(groupCounter, currentGroup);
            }
            if (currentGroup.size() == memNum) {
                groupCounter++;
                currentGroup = Lists.newArrayList();
                finalGroup.put(groupCounter, currentGroup);
            } else {
                currentGroup.add(workOrders.get(i));
            }
        }
        return finalGroup;
    }
}
