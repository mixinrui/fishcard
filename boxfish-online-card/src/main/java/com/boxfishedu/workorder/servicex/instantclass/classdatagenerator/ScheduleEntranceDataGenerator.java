package com.boxfishedu.workorder.servicex.instantclass.classdatagenerator;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.instantclass.schedulestrategy.ScheduleStrategyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/11/9.
 */
@Component
public class ScheduleEntranceDataGenerator implements IClassDataGenerator {

    @Autowired
    private ScheduleStrategyContext scheduleStrategyContext;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<WorkOrder> initCardAndSchedule(InstantClassCard instantClassCard) {
        //目前暂时只会使用到第一节课的策略,以后会根据需求变更
        return scheduleStrategyContext.prepareInstantSchedule(instantClassCard, null);
    }

    @Override
    public void initCourses(WorkOrder workOrder) {

    }

}
