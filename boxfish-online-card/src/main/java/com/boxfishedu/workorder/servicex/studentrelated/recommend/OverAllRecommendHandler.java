package com.boxfishedu.workorder.servicex.studentrelated.recommend;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/9/22.
 * 7+1课程推荐
 */
@Service
public class OverAllRecommendHandler extends DefaultRecommendHandler {


    @Autowired
    private RecommandCourseRequester recommandCourseRequester;


    @Override
    public Map<Integer, RecommandCourseView> recommendCourseViews(List<WorkOrder> workOrders, TimeSlotParam timeSlotParam) {

        // 如果不支持批量,直接使用默认的推荐方式
        if(!isSupportBatchExecute(workOrders)) {
            return super.recommendCourseViews(workOrders, timeSlotParam);
        }

        Map<Integer, RecommandCourseView> resultMap = new HashMap<>();
        int recommendIndex = 0;
        for(int i = 0; i < workOrders.size() / 8; i++) {
            List<RecommandCourseView> recommendCourseViews = recommandCourseRequester.getBatchRecommandCourse(
                    timeSlotParam.getStudentId());
            for(RecommandCourseView recommandCourseView : recommendCourseViews) {
                resultMap.put(++recommendIndex, recommandCourseView);
            }
        }
        return resultMap;
    }


    public boolean isSupportBatchExecute(List<WorkOrder> workOrders) {
        return (workOrders.size() % 8 == 0);
    }


}
