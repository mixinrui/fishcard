package com.boxfishedu.workorder.servicex.instantclass.schedulestrategy;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/12/20.
 */
@Component
public class ScheduleStrategyContext {
    @Autowired
    private java.util.Map<String, ScheduleStrategy> scheduleStrategyMap;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<WorkOrder> prepareInstantSchedule(InstantClassCard instantClassCard, String strategyType) {
        if (StringUtils.isEmpty(strategyType)) {
            logger.error("@prepareInstantSchedule未获取到策略的类型,使用默认策略");
            return scheduleStrategyMap.get(ScheduleStrategyEnum.SCHEDULE_LAST).prepareInstantSchedule(instantClassCard);
        } else {
            return scheduleStrategyMap.get(strategyType).prepareInstantSchedule(instantClassCard);
        }
    }
}
