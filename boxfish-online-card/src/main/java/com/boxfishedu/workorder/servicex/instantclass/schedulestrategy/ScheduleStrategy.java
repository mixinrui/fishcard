package com.boxfishedu.workorder.servicex.instantclass.schedulestrategy;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.param.InstantRequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Created by hucl on 16/12/20.
 */
public interface ScheduleStrategy {
    List<WorkOrder> prepareInstantSchedule(InstantClassCard instantClassCard);

    Optional<WorkOrder> getCardToStart(InstantRequestParam instantRequestParam, int teachingType);
}
