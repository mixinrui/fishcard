package com.boxfishedu.workorder.service.instantclass;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.TeacherPhotoRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseType2TeachingTypeService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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

    @Autowired
    private InstantClassTeacherService instantClassTeacherService;

    @Autowired
    private TeacherPhotoRequester teacherPhotoRequester;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public InstantClassResult getMatchResult() {
        Optional<TimeSlots> timeSlotsOptional = getMostSimilarSlot(new Long(CourseType2TeachingTypeService
                .instantCourseType2TeachingType(TutorType.resolve(getInstantRequestParam().getTutorType()))));
        if (!timeSlotsOptional.isPresent()) {
            return this.matchResultWrapper(InstantClassRequestStatus.NOT_IN_RANGE, teacherPhotoRequester);
        }
        logger.debug("@InstantClassService#user{}#最接近的时间片是{}", getInstantRequestParam().getStudentId(), timeSlotsOptional.get().getSlotId());
        Optional<InstantClassCard> instantClassCardOptional = getClassCardByStudentIdAndTimeParam(timeSlotsOptional.get());
        if (!instantClassCardOptional.isPresent()) {
            return dealFirstRequest(timeSlotsOptional);
        }
        //入口变化
        if (getInstantRequestParam().getSelectMode() != instantClassCardOptional.get().getEntrance()) {
            dealDifferentEntrance(instantClassCardOptional);
            instantClassJpaRepository.delete(instantClassCardOptional.get());
            dealFirstRequest(timeSlotsOptional);
        }
        //处理学生跨时间片的请求,如果当前请求在29:55这时候下一个请求在30:01;则返回上一个请求卡的匹配情况
        Optional<InstantClassCard> latestInstantCardOptional=instantClassJpaRepository
                .findTop1ByStudentIdAndCreateTimeAfterOrderByCreateTimeDesc(getInstantRequestParam().getStudentId()
                        , DateUtil.localDate2Date(LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(60)));
        if(latestInstantCardOptional.isPresent()){
            return matchResultWrapper(latestInstantCardOptional.get());
        }
        if (instantClassCardOptional.get().getResultReadFlag() == 1
                && instantClassCardOptional.get().getStatus() == InstantClassRequestStatus.NO_MATCH.getCode()) {
            instantClassJpaRepository.updateReadFlagAnsStatus(instantClassCardOptional.get().getId()
                    , 0, InstantClassRequestStatus.WAIT_TO_MATCH.getCode());
            instantClassTeacherService.dealFetchedTeachersAsync(instantClassCardOptional.get());
            return this.matchResultWrapper(InstantClassRequestStatus.WAIT_TO_MATCH, teacherPhotoRequester);
        } else {
            //直接返回结果,由定时器负责触发获取教师,推送消息给教师的任务
            return matchResultWrapper(instantClassCardOptional.get());
        }
    }

    private void dealDifferentEntrance(Optional<InstantClassCard> instantClassCardOptional) {
        if (instantClassCardOptional.get().getStatus() == InstantClassRequestStatus.WAIT_TO_MATCH.getCode()) {
            throw new BusinessException("您有别的课程正在等待匹配，请稍后再试");
        }
        if (instantClassCardOptional.get().getStatus() == InstantClassRequestStatus.MATCHED.getCode()) {
            throw new BusinessException("您当前还有未完成的课程，请稍后再试");
        }
    }

    private InstantClassResult dealFirstRequest(Optional<TimeSlots> timeSlotsOptional) {
        logger.debug("@InstantClassService#user{}时间片{}在instant_class_card表中无数据"
                , getInstantRequestParam().getStudentId(), timeSlotsOptional.get().getSlotId());
        InstantClassCard instantClassCard = persistInstantCard(initClassCardWithCourse(timeSlotsOptional.get()));
        instantClassTeacherService.dealFetchedTeachersAsync(instantClassCard);
        //TODO:发起教师请求，同时将匹配的结果返回给App
        return this.matchResultWrapper(InstantClassRequestStatus.WAIT_TO_MATCH, teacherPhotoRequester);
    }

    public Optional<TimeSlots> getMostSimilarSlot(Long roleId) {
        return this.getMostSimilarSlot(teacherStudentRequester.dayTimeSlotsTemplate(roleId));
    }

    private Optional<TimeSlots> getMostSimilarSlot(DayTimeSlots dayTimeSlots) {
        LocalDateTime nextSlotTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(30);
        return dayTimeSlots.getDailyScheduleTime().stream()
                .filter(timeSlot -> nextSlotTime
                        .isAfter(DateUtil.string2LocalDateTime(String.join(" ", DateUtil.date2SimpleString(new Date()), timeSlot.getStartTime()))))
                .max(Comparator.comparing(timeSlots -> timeSlots.getSlotId()));
    }

    public Optional<InstantClassCard> getClassCardByStudentIdAndTimeParam(TimeSlots timeSlots) {
        return instantClassJpaRepository
                .findByStudentIdAndClassDateAndSlotId(getInstantRequestParam().getStudentId()
                        , DateUtil.date2SimpleDate(new Date()), timeSlots.getSlotId());
    }

    public InstantClassCard persistInstantCard(InstantClassCard instantClassCard) {
        return instantClassJpaRepository.save(instantClassCard);
    }

    private InstantClassResult matchResultWrapper(InstantClassRequestStatus instantClassRequestStatus, TeacherPhotoRequester teacherPhotoRequester) {
        return InstantClassResult.newInstantClassResult(instantClassRequestStatus);
    }

    private InstantClassResult matchResultWrapper(InstantClassCard instantClassCard) {
        if (instantClassCard.getStatus() == InstantClassRequestStatus.NO_MATCH.getCode()) {
            if (instantClassCard.getResultReadFlag() == 0) {
                //将结果的读取值设置为1;表示已经读取了
                instantClassJpaRepository.updateReadFlag(instantClassCard.getId(), 1);
            }
        }
        return InstantClassResult.newInstantClassResult(instantClassCard, teacherPhotoRequester);
    }

    private InstantClassCard initClassCardWithCourse(TimeSlots timeSlots) {
        InstantClassCard instantClassCard = this.initClassCard(timeSlots);
        RecommandCourseView recommandCourseView = this.getCourseInfo();
        instantClassCard.setCourseId(recommandCourseView.getCourseId());
        instantClassCard.setCourseType(recommandCourseView.getCourseType());
        return instantClassCard;
    }

    private InstantClassCard initClassCard(TimeSlots timeSlots) {
        InstantClassCard instantClassCard = new InstantClassCard();
        instantClassCard.setClassDate(DateUtil.date2SimpleDate(new Date()));
        instantClassCard.setSlotId(timeSlots.getSlotId());
        instantClassCard.setStudentId(getInstantRequestParam().getStudentId());
        instantClassCard.setRequestTeacherTimes(0);
        instantClassCard.setStudentRequestTimes(0);
        instantClassCard.setResultReadFlag(0);
        instantClassCard.setTeacherId(0l);
        instantClassCard.setChatRoomId(0l);
        instantClassCard.setCreateTime(new Date());
        instantClassCard.setRequestMatchTeacherTime(DateTime.now().toDate());
        instantClassCard.setEntrance(getInstantRequestParam().getSelectMode());
        //课程表入口
        if (getInstantRequestParam().getSelectMode() == 0) {
            instantClassCard.setWorkorderId(getAvaliableWorkOrder().getId());
            instantClassCard.setRoleId(getAvaliableWorkOrder().getSkuId());
        } else {
            instantClassCard.setOrderId(getInstantRequestParam().getOrderId());
            instantClassCard.setProductType(getInstantRequestParam().getProductType());
            instantClassCard.setTutorType(getInstantRequestParam().getTutorType());
            instantClassCard.setComboType(getInstantRequestParam().getComboType());
            instantClassCard.setRoleId(TeachingType.WAIJIAO.getCode());
        }
        instantClassCard.setStatus(InstantClassRequestStatus.WAIT_TO_MATCH.getCode());
        return instantClassCard;
    }

    private RecommandCourseView getCourseInfo() {
        RecommandCourseView recommandCourseView = new RecommandCourseView();
        switch (InstantRequestParam.SelectModeEnum.getSelectMode(getInstantRequestParam().getSelectMode())) {
            case COURSE_SCHEDULE_ENTERANCE:
                WorkOrder latestWorkOrder = ThreadLocalUtil.latestWorkOrderThreadLocal.get();
                recommandCourseView.setCourseId(latestWorkOrder.getCourseId());
                recommandCourseView.setCourseType(latestWorkOrder.getCourseType());
                return recommandCourseView;
            case OTHER_ENTERANCE:
                return recommandCourseRequester.getInstantCourseView(getInstantRequestParam().getStudentId()
                        , 1, TutorType.resolve(getInstantRequestParam().getTutorType()));
            default:
                throw new BusinessException("入口参数错误");
        }
    }

    private InstantRequestParam getInstantRequestParam() {
        return ThreadLocalUtil.instantRequestParamThreadLocal.get();
    }

    private WorkOrder getAvaliableWorkOrder() {
        return ThreadLocalUtil.latestWorkOrderThreadLocal.get();
    }

}
