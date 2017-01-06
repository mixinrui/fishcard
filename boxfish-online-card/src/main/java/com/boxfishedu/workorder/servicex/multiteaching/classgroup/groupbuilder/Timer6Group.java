package com.boxfishedu.workorder.servicex.multiteaching.classgroup.groupbuilder;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassType;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.multiteaching.statusdealer.SmallClassEvent;
import com.google.common.collect.Lists;
import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 17/1/6.
 * 6人小班组
 */
@Component
public class Timer6Group extends GroupBuilder {
    @Override
    protected List<WorkOrder> cardsToGroup() {
        return null;
    }

    @Override
    protected Map<String, List<WorkOrder>> groupByTime(List<WorkOrder> workOrders) {
        return null;
    }

    @Override
    protected Map<String, List<WorkOrder>> groupbyLevel(List<WorkOrder> workOrders) {
        return null;
    }

    @Override
    protected Map<String, List<WorkOrder>> groupByStudyCounter(List<WorkOrder> workOrders) {
        return null;
    }

    @Override
    protected Map<String, List<WorkOrder>> groupByRelation(List<WorkOrder> workOrders) {
        return null;
    }

    @Override
    protected WorkOrder selectLeader(List<WorkOrder> workOrders) {
        return workOrders.get(0);
    }

    @Override
    protected void initGroup(Map<String, List<WorkOrder>> groups) {
        groups.forEach((key, groupMembers) -> {
            WorkOrder leader = this.selectLeader(groupMembers);
            SmallClass smallClass = new SmallClass();
            smallClass.setClassDate(leader.getStartTime());
            smallClass.setSlotId(leader.getSlotId());
            smallClass.setCourseType(leader.getCourseType());
            smallClass.setGroupLeader(leader.getStudentId());
            smallClass.setGroupLeaderCard(leader.getId());
            smallClass.setRoleId(leader.getSkuId());
            smallClass.setSmallClassType(SmallClassType.SMALL);
            smallClass.setAllCardIds(this.fetchCardIds(groupMembers));
            smallClass.setAllStudentIds(this.fetchStudents(groupMembers));

            new SmallClassEvent(smallClass, SmallClassCardStatus.CREATE);
        });
    }

    private List<Long> fetchStudents(List<WorkOrder> workOrders) {
        List<Long> studentIds = Lists.newArrayList();
        workOrders.forEach(workOrder -> {
            studentIds.add(workOrder.getStudentId());
        });
        return studentIds;
    }

    public List<Long> fetchCardIds(List<WorkOrder> workOrders) {
        List<Long> cardIds = Lists.newArrayList();
        workOrders.forEach(workOrder -> {
            cardIds.add(workOrder.getId());
        });
        return cardIds;
    }
}
