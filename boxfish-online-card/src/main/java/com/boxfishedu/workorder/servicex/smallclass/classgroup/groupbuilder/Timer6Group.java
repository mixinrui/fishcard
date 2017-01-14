package com.boxfishedu.workorder.servicex.smallclass.classgroup.groupbuilder;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventDispatch;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by hucl on 17/1/6.
 * 6人小班组
 */
@Component
public class Timer6Group extends GroupBuilder {
    @Autowired
    private SmallClassRequester smallClassRequester;

    @Autowired
    private SmallClassEventDispatch smallClassEventDispatch;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Override
    protected List<WorkOrder> cardsToGroup() {
        LocalDateTime deadLocalDateTime = LocalDateTime.now().plusDays(10);
        Date deadDate = DateUtil.localDate2Date(deadLocalDateTime);
        return workOrderJpaRepository
                .findByClassTypeAndSmallClassIdIsNullAndStartTimeBetween(ClassTypeEnum.SMALL.name(), new Date(), deadDate);
    }

    @Override
    protected Map<String, List<WorkOrder>> groupByTime(List<WorkOrder> workOrders) {
        return workOrders.parallelStream()
                         .collect(groupingBy(workOrder -> DateUtil.Date2String(workOrder.getStartTime())));
    }

    @Override
    protected Map<String, List<WorkOrder>> groupbyLevel(List<WorkOrder> workOrders) {
        return workOrders.parallelStream()
                         .collect(groupingBy(workOrder -> smallClassRequester.fetchUserDifficultyInfo(workOrder.getStudentId())));
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
    protected Integer smallClassMemeberNum() {
        return 6;
    }

    @Override
    protected void initGroup(Map<Integer, List<WorkOrder>> groups) {
        groups.forEach((key, groupMembers) -> {
            WorkOrder leader = this.selectLeader(groupMembers);
            SmallClass smallClass = new SmallClass();
            smallClass.setClassDate(DateUtil.date2SimpleDate(leader.getStartTime()));
            smallClass.setSlotId(leader.getSlotId());
            smallClass.setCourseType(leader.getCourseType());
            smallClass.setGroupLeader(leader.getStudentId());
            smallClass.setGroupLeaderCard(leader.getId());
            smallClass.setRoleId(leader.getSkuId());
            smallClass.setCreateTime(new Date());
            smallClass.setStartTime(leader.getStartTime());
            smallClass.setEndTime(leader.getEndTime());
            smallClass.setCourseType(leader.getCourseType());
            smallClass.setDifficultyLevel(
                    smallClassRequester.fetchUserDifficultyInfo(smallClass.getGroupLeader()));
            smallClass.setClassType(ClassTypeEnum.SMALL.name());
            smallClass.setAllCards(groupMembers);
            smallClass.setAllStudentIds(this.fetchStudents(groupMembers));

            new SmallClassEvent(smallClass, smallClassEventDispatch, PublicClassInfoStatusEnum.CREATE);
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
