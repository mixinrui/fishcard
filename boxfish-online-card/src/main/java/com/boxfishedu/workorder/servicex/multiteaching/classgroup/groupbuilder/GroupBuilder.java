package com.boxfishedu.workorder.servicex.multiteaching.classgroup.groupbuilder;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.multiteaching.statusdealer.SmallClassEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.org.apache.xml.internal.security.algorithms.implementations.IntegrityHmac;

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

    protected abstract void initGroup(Map<String, List<WorkOrder>> groups);

    protected abstract WorkOrder selectLeader(List<WorkOrder> workOrders);

    protected abstract Integer smallClassMemeberNum();


    public void group() {
        //小班群组
        Map<String, List<WorkOrder>> groups = Maps.newHashMap();

        List<WorkOrder> cards = this.cardsToGroup();

        Map<String, List<WorkOrder>> timeGrouped = this.groupByTime(cards);

        timeGrouped.forEach((timeKey, timedWorkOrders) -> {
            Map<String, List<WorkOrder>> levelGrouped = this.groupbyLevel(timedWorkOrders);

            levelGrouped.forEach((levelKey, leveledWorkOrders) -> {
                Map<String, List<WorkOrder>> studyCounterGrouped = this.groupByStudyCounter(leveledWorkOrders);

                Integer groupSeq = 0;


                //TODO:等待确定规则
//                studyCounterGrouped.forEach((studyCounterKey, studyCounterWorkOrders) -> {
//                    Map<String, List<WorkOrder>> relationGrouped = this.groupByRelation(studyCounterWorkOrders);
////                    groups.putAll(relationGrouped);
//                });
            });
        });

        this.initGroup(groups);
    }

    //将小班按照信息获取
    protected Map<Integer, WorkOrder> divideGroup(List<WorkOrder> workOrders) {
//        Integer memNum = this.smallClassMemeberNum();
//        Integer groupNum = workOrders.size() / memNum;
//        Map<Integer, List<WorkOrder>> finalGroup = new HashMap<>();
//
//        Integer groupCounter = 0;
//        List<WorkOrder> currentGroup = finalGroup.get(groupCounter);
//        for (int i = 0; i < workOrders.size(); i++) {
//            if (Objects.isNull(currentGroup)) {
//                currentGroup = Lists.newArrayList();
//            }
//            if (currentGroup.size() > memNum) {
//                groupCounter++;
//            }
//            if (currentGroup.size() < memNum) {
//                currentGroup.add(workOrders.get(i));
//            } else {
//                groupCounter++;
//            }
//        }

        return null;
    }
}
