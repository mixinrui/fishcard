package com.boxfishedu.workorder.servicex.studentrelated.recommend;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.service.RecommandedCourseService;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/9/22.
 * 默认的课程推荐方式,工单本身来进行判断单独推荐
 */
@Service
public class DefaultRecommendHandler implements RecommendHandler {


    private final static int EXCHANGE_DREAM_SKU_ID = 11;


    @Autowired
    private RecommandCourseRequester recommandCourseRequester;


    @Autowired
    private RecommandedCourseService recommandedCourseService;


    @Override
    public Map<Integer, RecommandCourseView> recommendCourseViews(List<WorkOrder> workOrders, TimeSlotParam timeSlotParam) {
        Map<Integer, RecommandCourseView> courseViewMap = Maps.newHashMap();
        for (WorkOrder workOrder : workOrders) {
            logger.debug("鱼卡序号{}",workOrder.getSeqNum());
            Integer index=recommandedCourseService.getCourseIndex(workOrder);
            // 判断tutorType 是中教外教还是中外教,对应的推课
            TutorType tutorType = TutorType.resolve(workOrder.getService().getTutorType());
            RecommandCourseView recommandCourseView;




            // 不同类型的套餐对应不同类型的课程推荐
            if(Objects.equals(tutorType, TutorType.MIXED)) {
                recommandCourseView=recommandCourseRequester.getRecommandCourse(workOrder,index);
            }
            // 兑换的终极梦想课程
            else if(Objects.equals(workOrder.getSkuIdExtra(), EXCHANGE_DREAM_SKU_ID)) {
                recommandCourseView = recommandCourseRequester.getDreamRecommandCourse(workOrder);
            }
            else {
                //  if(Objects.equals(tutorType, TutorType.FRN) || Objects.equals(tutorType, TutorType.CN))
                recommandCourseView = recommandCourseRequester.getRecomendCourse(workOrder, tutorType);
            }
            courseViewMap.put(workOrder.getSeqNum(), recommandCourseView);
        }
        return courseViewMap;
    }

    private boolean within48Hourse(WorkOrder workOrder) {
        Duration duration = Duration.between(
                LocalDateTime.now(), DateUtil.convertLocalDateTime(workOrder.getStartTime()));
        // 课程推荐, 否则推课程类型
        return duration.toDays() <=2;
    }
}
