package com.boxfishedu.workorder.servicex.studentrelated.recommend;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/9/22.
 */
public interface RecommendHandler {

    Logger logger = LoggerFactory.getLogger(RecommendHandler.class);

    Map<Integer, RecommandCourseView> recommendCourseViews(List<WorkOrder> workOrders, TimeSlotParam timeSlotParam);
}
