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
        if(isSupportBatchExecute(workOrders)) {
            return batchRecommendCourses(workOrders, timeSlotParam);
        } else {
            Map<Integer, RecommandCourseView> courseViewMap = Maps.newHashMap();
            for (WorkOrder workOrder : workOrders) {
                logger.debug("鱼卡序号{}",workOrder.getSeqNum());
                // 不同类型的套餐对应不同类型的课程推荐
                RecommandCourseView recommandCourseView = recommendCourse(workOrder);
                courseViewMap.put(workOrder.getSeqNum(), recommandCourseView);
            }
            return courseViewMap;
        }
    }


    public boolean isSupportBatchExecute(List<WorkOrder> workOrders) {
        return (workOrders.size() % 8 == 0);
    }


    private RecommandCourseView recommendCourse(WorkOrder workOrder) {
        return recommandCourseRequester.getDreamRecommandCourse(workOrder);
    }


    private Map<Integer, RecommandCourseView> batchRecommendCourses(List<WorkOrder> workOrders, TimeSlotParam timeSlotParam) {
        int recommendIndex = 0;
        Map<Integer, RecommandCourseView> resultMap = Maps.newHashMap();
        for(int i = 0; i < workOrders.size() / 8; i++) {
            List<RecommandCourseView> recommendCourseViews = recommandCourseRequester.getBatch8DreamRecommandCourse(
                    timeSlotParam.getStudentId());
            for(RecommandCourseView recommandCourseView : recommendCourseViews) {
                resultMap.put(++recommendIndex, recommandCourseView);
            }
        }
        return resultMap;
    }
}
