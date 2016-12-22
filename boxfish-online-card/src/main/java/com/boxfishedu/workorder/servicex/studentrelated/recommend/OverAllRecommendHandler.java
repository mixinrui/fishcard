package com.boxfishedu.workorder.servicex.studentrelated.recommend;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return super.recommendCourseViews(workOrders, timeSlotParam);
    }


    public boolean isSupportBatchExecute(List<WorkOrder> workOrders) {
        return false;
//        return (workOrders.size() % 8 == 0);
    }


}
