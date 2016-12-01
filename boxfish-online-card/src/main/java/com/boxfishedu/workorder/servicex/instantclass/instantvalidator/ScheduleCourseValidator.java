package com.boxfishedu.workorder.servicex.instantclass.instantvalidator;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseType2TeachingTypeService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by hucl on 16/11/4.
 */
@Order(3)
@Component
public class ScheduleCourseValidator implements InstantClassValidator {
    @Autowired
    private OnlineAccountService onlineAccountService;

    private CourseType2TeachingTypeService courseType2TeachingTypeService;

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Override
    public int preValidate() {
        InstantRequestParam instantRequestParam=ThreadLocalUtil.instantRequestParamThreadLocal.get();
        switch (InstantRequestParam.SelectModeEnum.getSelectMode(instantRequestParam.getSelectMode())){
            case COURSE_SCHEDULE_ENTERANCE:
                Optional<WorkOrder> haveClass=workOrderJpaRepository
                        .findTop1ByStudentIdAndSkuIdAndIsFreezeAndStartTimeAfterOrderByStartTimeAsc(instantRequestParam
                                .getStudentId(),TeachingType.WAIJIAO.getCode(),new Integer(0),new Date());
                if(!haveClass.isPresent()){
                    //判断当前是否有刚匹配上的课程,如果有,则跳过这个验证
                    Optional<InstantClassCard> latestMatchedDateOptional=instantClassJpaRepository
                            .findLatestMatchedInstantCard(instantRequestParam.getStudentId(),InstantClassRequestStatus.MATCHED.getCode());
                    if(latestMatchedDateOptional.isPresent()){
                        //TODO:25分钟之内匹配上的,则放行;需要做配置
                        LocalDateTime localDateTimeBegin=LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(30);
                        if(latestMatchedDateOptional.get().getRequestMatchTeacherTime().after(DateUtil.localDate2Date(localDateTimeBegin))){
                            ThreadLocalUtil.instantCardMatched30Minutes.set(latestMatchedDateOptional.get());
                            return InstantClassRequestStatus.MATCHED_LESS_THAN_30MINUTES.getCode();
                        }
                    }
                    return InstantClassRequestStatus.OUT_OF_NUM.getCode();
                }
                if(StringUtils.isEmpty(haveClass.get().getCourseId())){
                    logger.debug("@ScheduleCourseValidator#user#{}的最新课程表无课程，推荐课程",instantRequestParam.getStudentId());
                    dataCollectorService.getLatestRecommandCourse(haveClass.get());
                    ThreadLocalUtil.latestWorkOrderThreadLocal.set(workOrderJpaRepository.findOne(haveClass.get().getId()));
                }
                else{
                    //课程表入口的最近一节外教课
                    ThreadLocalUtil.latestWorkOrderThreadLocal.set(haveClass.get());
                }
                return InstantClassRequestStatus.UNKNOWN.getCode();
            case OTHER_ENTERANCE:
                return InstantClassRequestStatus.UNKNOWN.getCode();
            default:
                throw new BusinessException("未知的入口参数");
        }
    }
}
