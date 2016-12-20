package com.boxfishedu.workorder.servicex.instantclass.schedulestrategy;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;

import java.util.List;

/**
 * Created by hucl on 16/12/20.
 */
public class ScheduleStrategyContext {
    private ScheduleStrategy strategy = null;

    public ScheduleStrategyContext(ScheduleStrategy _strategy){
        this.strategy = _strategy;
    }

    public List<WorkOrder> prepareInstantSchedule(InstantClassCard instantClassCard){
        return this.strategy.prepareInstantSchedule(instantClassCard);
    }
}
