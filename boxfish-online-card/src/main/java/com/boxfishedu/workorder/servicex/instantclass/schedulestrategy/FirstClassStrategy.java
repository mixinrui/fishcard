package com.boxfishedu.workorder.servicex.instantclass.schedulestrategy;

import com.boxfishedu.workorder.common.bean.ScheduleTypeEnum;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by hucl on 16/12/20.
 */
@Component(ScheduleStrategyEnum.SCHEDULE_FIRST)
public class FirstClassStrategy implements ScheduleStrategy {

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    //TODO:扣除首次的情况和最后一次的情况不同
    @Override
    public List<WorkOrder> prepareInstantSchedule(InstantClassCard instantClassCard) {
        return null;
    }

    @Override
    public Optional<WorkOrder> getCardToStart(InstantRequestParam instantRequestParam, int teachingType) {
        return workOrderJpaRepository
                .findTop1ByStudentIdAndSkuIdAndIsFreezeAndStartTimeAfterOrderByStartTimeAsc(instantRequestParam
                        .getStudentId(), teachingType, new Integer(0), new Date());
    }
}
