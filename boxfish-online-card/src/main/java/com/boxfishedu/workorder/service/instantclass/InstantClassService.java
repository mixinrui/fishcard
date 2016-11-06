package com.boxfishedu.workorder.service.instantclass;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseType2TeachingTypeService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
/**
 * Created by hucl on 16/11/3.
 */
@Component
public class InstantClassService {
    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public InstantClassRequestStatus getMatchResult(){
        Optional<TimeSlots> timeSlotsOptional=getMostSimilarSlot(new Long(CourseType2TeachingTypeService
                .instantCourseType2TeachingType(TutorType.resolve(getInstantRequestParam().getTutorType()))));
        if(!timeSlotsOptional.isPresent()){
            return InstantClassRequestStatus.NOT_IN_RANGE;
        }
        logger.debug("@InstantClassService#user{}#最接近的时间片是{}",getInstantRequestParam().getStudentId(),timeSlotsOptional.get().getSlotId());
        Optional<InstantClassCard> instantClassCardOptional=getClassCardByStudentIdAndTimeParam(timeSlotsOptional.get());
        if(!instantClassCardOptional.isPresent()){
            logger.debug("@InstantClassService#user{}时间片{}在instant_class_card表中无数据"
                    ,getInstantRequestParam().getStudentId(),timeSlotsOptional.get().getSlotId());
            persistInstantCard(initClassCardWithCourse(timeSlotsOptional.get()));
            //发起获取推荐教师

            //TODO:发起教师请求，同时将匹配的结果返回给App
            return InstantClassRequestStatus.WAIT_TO_MATCH;
        }
        else{
            return InstantClassRequestStatus.getEnumByCode(instantClassCardOptional.get().getStatus());
        }
    }

    public Optional<TimeSlots> getMostSimilarSlot(Long roleId){
        return this.getMostSimilarSlot(teacherStudentRequester.dayTimeSlotsTemplate(roleId));
    }

    private Optional<TimeSlots> getMostSimilarSlot(DayTimeSlots dayTimeSlots){
        LocalDateTime nextSlotTime=LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(30);
        return dayTimeSlots.getDailyScheduleTime().stream()
                .filter(timeSlot->nextSlotTime
                        .isAfter(DateUtil.string2LocalDateTime(String.join(" ",DateUtil.date2SimpleString(new Date()),timeSlot.getStartTime()))))
                .max(Comparator.comparing(timeSlots -> timeSlots.getSlotId()));
    }

    public Optional<InstantClassCard> getClassCardByStudentIdAndTimeParam(TimeSlots timeSlots){
        return instantClassJpaRepository
                .findByStudentIdAndClassDateAndSlotId(getInstantRequestParam().getStudentId()
                        ,DateUtil.date2SimpleDate(new Date()),timeSlots.getSlotId().intValue());
    }

    public void persistInstantCard(InstantClassCard instantClassCard){
        instantClassJpaRepository.save(instantClassCard);
    }

    private InstantClassCard initClassCardWithCourse(TimeSlots timeSlots){
        InstantClassCard instantClassCard= this.initClassCard(timeSlots);
        RecommandCourseView recommandCourseView = this.getCourseInfo();
        instantClassCard.setCourseId(recommandCourseView.getCourseId());
        instantClassCard.setCourseType(recommandCourseView.getCourseType());
        return instantClassCard;
    }

    private InstantClassCard initClassCard(TimeSlots timeSlots){
        InstantClassCard instantClassCard=new InstantClassCard();
        instantClassCard.setClassDate(DateUtil.date2SimpleDate(new Date()));
        instantClassCard.setSlotId(timeSlots.getSlotId());
        instantClassCard.setStudentId(getInstantRequestParam().getStudentId());
        instantClassCard.setRequestTeacherTimes(0);
        instantClassCard.setStudentRequestTimes(0);
        instantClassCard.setStatus(InstantClassRequestStatus.WAIT_TO_MATCH.getCode());
        return instantClassCard;
    }

    private RecommandCourseView getCourseInfo(){
        RecommandCourseView recommandCourseView=new RecommandCourseView();
        switch (InstantRequestParam.SelectModeEnum.getSelectMode(getInstantRequestParam().getSelectMode())){
            case COURSE_SCHEDULE_ENTERANCE:
                WorkOrder latestWorkOrder=ThreadLocalUtil.latestWorkOrderThreadLocal.get();
                recommandCourseView.setCourseId(latestWorkOrder.getCourseId());
                recommandCourseView.setCourseType(latestWorkOrder.getCourseType());
                return recommandCourseView;
            case OTHER_ENTERANCE:
                return recommandCourseRequester.getInstantCourseView(TutorType.resolve(getInstantRequestParam().getTutorType()));
            default:
                throw new BusinessException("参数的入口参数错误");
        }
    }

    private InstantRequestParam getInstantRequestParam(){
        return ThreadLocalUtil.instantRequestParamThreadLocal.get();
    }

}
