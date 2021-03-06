package com.boxfishedu.workorder.servicex.studentrelated.recommend;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/9/22.
 * 终极梦想课程推荐
 */
@Service
public class DreamRecommendHandler extends DefaultRecommendHandler {


    @Autowired
    private RecommandCourseRequester recommandCourseRequester;


    @Override
    public Map<Integer, RecommandCourseView> recommendCourseViews(List<WorkOrder> workOrders, TimeSlotParam timeSlotParam) {
        Map<Integer, RecommandCourseView> courseViewMap = Maps.newHashMap();
        for (WorkOrder workOrder : workOrders) {
            logger.debug("鱼卡序号{}",workOrder.getSeqNum());
            // 不同类型的套餐对应不同类型的课程推荐
            RecommandCourseView recommandCourseView = recommendCourse(workOrder);
            courseViewMap.put(workOrder.getSeqNum(), recommandCourseView);
        }
        return courseViewMap;
    }


    public boolean isSupportBatchExecute(List<WorkOrder> workOrders) {
        return false;
//        return (workOrders.size() % 8 == 0);
    }


    private RecommandCourseView recommendCourse(WorkOrder workOrder) {
        return recommandCourseRequester.getUltimateRecommend(workOrder);
    }

}
