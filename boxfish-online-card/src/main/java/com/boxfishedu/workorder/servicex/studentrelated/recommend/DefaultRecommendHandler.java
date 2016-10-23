package com.boxfishedu.workorder.servicex.studentrelated.recommend;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by LuoLiBing on 16/9/22.
 * 默认的课程推荐方式,工单本身来进行判断单独推荐
 */
@SuppressWarnings("ALL")
@Service
public class DefaultRecommendHandler implements RecommendHandler {

//    private final static int EXCHANGE_DREAM_SKU_ID = 11;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    @Override
    public Map<Integer, RecommandCourseView> recommendCourseViews(List<WorkOrder> workOrders, TimeSlotParam timeSlotParam) {
        return recommendCourse(workOrders, (workOrder -> DateUtil.within48Hours(workOrder.getStartTime())));
    }

    public Map<Integer, RecommandCourseView> recommendCourse(List<WorkOrder> workOrders, Predicate<WorkOrder> predicate) {
        Map<Integer, RecommandCourseView> courseViewMap = Maps.newHashMap();
        for (int i = 0, size = workOrders.size(); i < size; i++) {
            WorkOrder workOrder = workOrders.get(i);
            logger.debug("鱼卡序号{}",workOrder.getSeqNum());
            // 判断tutorType 是中教外教还是中外教,对应的推课
            TutorType tutorType = TutorType.resolve(workOrder.getService().getTutorType());
            RecommandCourseView recommandCourseView;

            switch (tutorType) {
                case CN: recommandCourseView = recommandCourseRequester.getPromoteRecommend(workOrder, predicate); break;
                case FRN: recommandCourseView = recommandCourseRequester.getUltimateRecommend(workOrder, predicate); break;
                case MIXED: recommandCourseView = recommandCourseRequester.getPromoteRecommend(workOrder, predicate); break;
                default: throw new BusinessException("未知类型的课程");
            }
            courseViewMap.put(i, recommandCourseView);
        }
        return courseViewMap;
    }

    public RecommandCourseView recommandCourseView(WorkOrder workOrder){
        RecommandCourseView recommandCourseView;
        TutorType tutorType = TutorType.resolve(workOrder.getService().getTutorType());
        switch (tutorType) {
            case CN: recommandCourseView = recommandCourseRequester.getPromoteRecommend(workOrder); break;
            case FRN: recommandCourseView = recommandCourseRequester.getUltimateRecommend(workOrder); break;
            case MIXED: recommandCourseView = recommandCourseRequester.getPromoteRecommend(workOrder); break;
            default: throw new BusinessException("未知类型的课程");
        }
        return recommandCourseView;
    }
}
