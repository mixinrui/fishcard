package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/10/19.
 */
@Component
public class RecommendCourseTask implements Runnable {

    private static SingleRecommendHandler handler;

    @Autowired
    public void autowired(SingleRecommendHandler _handler) {
        handler = _handler;
    }

    private List<WorkOrder> workOrders;

    private Map<Long, CourseSchedule> courseScheduleMap;

    public RecommendCourseTask() {
    }

    public RecommendCourseTask(List<WorkOrder> workOrders, Map<Long, CourseSchedule> courseScheduleMap) {
        this.workOrders = workOrders;
        this.courseScheduleMap = courseScheduleMap;
    }

    @Override
    public void run() {
        for (int i = 0, size = workOrders.size(); i < size; i++) {
            if (workOrders.get(i).isGroupCard()) {
                continue;
            }
            handler.singleRecommend(
                    workOrders.get(i), courseScheduleMap.get(workOrders.get(i).getId()),
                    (w -> DateUtil.within72Hours(w.getStartTime())));
        }
    }


}