package com.boxfishedu.workorder.servicex.instantclass.schedulestrategy;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/12/20.
 */
@Component
public class FirstClassStrategy implements ScheduleStrategy {
    @Override
    public List<WorkOrder> prepareInstantSchedule(InstantClassCard instantClassCard) {
        return null;
    }
}
