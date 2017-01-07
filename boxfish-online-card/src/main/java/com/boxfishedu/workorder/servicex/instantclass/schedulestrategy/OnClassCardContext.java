package com.boxfishedu.workorder.servicex.instantclass.schedulestrategy;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by hucl on 16/12/21.
 */
@Component
public class OnClassCardContext {
    @Autowired
    private java.util.Map<String, ScheduleStrategy> scheduleStrategyMap;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public Optional<WorkOrder> getCardToStart(InstantRequestParam instantRequestParam, int teachingType) {
        //目前暂时一样
        return scheduleStrategyMap.get(ScheduleStrategyEnum.SCHEDULE_LAST)
                .getCardToStart(instantRequestParam, teachingType);
    }
}
