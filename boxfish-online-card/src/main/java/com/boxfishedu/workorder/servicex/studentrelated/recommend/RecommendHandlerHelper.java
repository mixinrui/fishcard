package com.boxfishedu.workorder.servicex.studentrelated.recommend;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseType2TeachingTypeService;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/9/22.
 */
@Service
public class RecommendHandlerHelper {

    private Map<Class, RecommendHandler> recommendHandlerMap;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    public RecommendHandlerHelper(List<RecommendHandler> handlerList) {
        if(CollectionUtils.isEmpty(handlerList)) {
            return;
        }
        recommendHandlerMap = Maps.newHashMap();
        for (RecommendHandler aHandlerList : handlerList) {
            recommendHandlerMap.put(aHandlerList.getClass(), aHandlerList);
        }
    }


    public Map<Integer, RecommandCourseView> recommendCourses(List<WorkOrder> workOrders, TimeSlotParam timeSlotParam) {

        String comboTypeStr = timeSlotParam.getComboType();
        ComboTypeToRoleId comboType = ComboTypeToRoleId.resolve(comboTypeStr);
        RecommendHandler recommendHandler = adaptingRecommendHandler(comboType);
        try {
            Map<Integer, RecommandCourseView> resultMap = recommendHandler.recommendCourseViews(workOrders, timeSlotParam);

            for (int i = 0, size = workOrders.size(); i < size; i++) {
                WorkOrder workOrder = workOrders.get(i);
                logger.debug("鱼卡序号{}", workOrder.getSeqNum());
//            Integer index=recommandedCourseService.getCourseIndex(workOrder);
                RecommandCourseView recommandCourseView = resultMap.get(workOrder.getSeqNum());
                workOrder.initCourseInfo(recommandCourseView);
                workOrder.setSkuId(CourseType2TeachingTypeService.courseType2TeachingType2(
                        recommandCourseView.getCourseType(), TutorType.resolve(workOrder.getService().getTutorType())));
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("暂时没有与你的水平匹配的课程，新课即将上线，请过些时候再选课或者调整学习设置");
        }

    }


    private RecommendHandler adaptingRecommendHandler(ComboTypeToRoleId comboType) {
        switch (comboType) {
            case OVERALL: return recommendHandlerMap.get(OverAllRecommendHandler.class);
            case CHINESE: return recommendHandlerMap.get(DreamRecommendHandler.class);
            default: return recommendHandlerMap.get(DefaultRecommendHandler.class);
        }
    }

}
