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
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by hucl on 16/11/3.
 */
@Component
public class InstantClassService {
    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    @Autowired
    private InstantClassTeacherService instantClassTeacherService;

    @Autowired
    private TeacherPhotoRequester teacherPhotoRequester;

    @Autowired
    private InstantClassUpdatorService instantClassUpdatorService;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取学生的匹配情况
     *
     * @return
     */
    public InstantClassResult getMatchResult() {
        Optional<TimeSlots> timeSlotsOptional = getMostSimilarSlot(this.getRoleId());
        if (!timeSlotsOptional.isPresent()) {
            return this.matchResultWrapper(InstantClassRequestStatus.NOT_IN_RANGE, teacherPhotoRequester);
        }

        logger.debug("@InstantClassService#参数[{}]#最接近时间片是[{}]"
                , getInstantRequestParam(), timeSlotsOptional.get().getSlotId());

        Optional<InstantClassCard> instantClassCardOptional
                = getClassCardByStudentIdAndTimeParam(timeSlotsOptional.get());

        if (!instantClassCardOptional.isPresent()) {
            return getFirstInstantClassResult(timeSlotsOptional);
        }

        //入口变化
        if (getInstantRequestParam().getSelectMode() != instantClassCardOptional.get().getEntrance()) {
            this.entranceChanged(timeSlotsOptional, instantClassCardOptional);
        }

        //请求的教师类型发生变化
        if (!this.isTutorTypeSame(instantClassCardOptional)) {
            this.tutorTypeChanged(timeSlotsOptional, instantClassCardOptional);
        }

        if (instantClassCardOptional.get().getResultReadFlag() == 1
                && instantClassCardOptional.get().getStatus() == InstantClassRequestStatus.NO_MATCH.getCode()) {

            InstantClassCard instantClassCard = instantClassUpdatorService
                    .resetInstantCard(instantClassCardOptional.get()
                            .getId(), 0, InstantClassRequestStatus.WAIT_TO_MATCH);

            instantClassTeacherService.dealFetchedTeachersAsync(instantClassCard, false);

            return this.matchResultWrapper(InstantClassRequestStatus.WAIT_TO_MATCH, teacherPhotoRequester);
        } else {
            //直接返回结果,由定时器负责触发获取教师,推送消息给教师的任务
            return matchResultWrapper(instantClassCardOptional.get());
        }
    }

    private void tutorTypeChanged(Optional<TimeSlots> timeSlotsOptional
            , Optional<InstantClassCard> instantClassCardOptional) {

        this.reInitInstantCard(timeSlotsOptional, instantClassCardOptional);
    }

    private void reInitInstantCard(Optional<TimeSlots> timeSlotsOptional
            , Optional<InstantClassCard> instantClassCardOptional) {

        dealDifferentEntrance(instantClassCardOptional);
        instantClassJpaRepository.delete(instantClassCardOptional.get());
        dealFirstRequest(timeSlotsOptional);
    }

    private boolean isTutorTypeSame(Optional<InstantClassCard> instantClassCardOptional) {
        return StringUtils.equals(getInstantRequestParam().getTutorType()
                , instantClassCardOptional.get().getTutorType());
    }

    private void entranceChanged(Optional<TimeSlots> timeSlotsOptional
            , Optional<InstantClassCard> instantClassCardOptional) {

        this.reInitInstantCard(timeSlotsOptional, instantClassCardOptional);
    }

    /**
     * 获取传给师生运营的教师类型参数
     *
     * @return
     */
    private Long getRoleId() {
        return new Long(CourseType2TeachingTypeService
                .instantCourseType2TeachingType(TutorType.resolve(getInstantRequestParam().getTutorType())));
    }

    //当无对应的鱼卡数据时候,获取第一条数据
    private InstantClassResult getFirstInstantClassResult(Optional<TimeSlots> timeSlotsOptional) {
        //处理学生跨时间片的请求,如果当前请求在29:55这时候下一个请求在30:01;则返回上一个请求卡的匹配情况
        Optional<InstantClassCard> latestInstantCardOptional = instantClassJpaRepository
                .findTop1ByStudentIdAndCreateTimeAfterOrderByCreateTimeDesc(getInstantRequestParam().getStudentId()
                        , DateUtil.localDate2Date(LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(70)));
        if (latestInstantCardOptional.isPresent()) {
            return matchResultWrapper(latestInstantCardOptional.get());
        }

        Optional<InstantClassCard> latestInstantCardOptional30Minutes = instantClassJpaRepository
                .findTop1ByStudentIdAndRequestMatchTeacherTimeAfterOrderByCreateTimeDesc(
                        getInstantRequestParam().getStudentId()
                        , DateUtil.localDate2Date(LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(30)));

        if (latestInstantCardOptional30Minutes.isPresent()) {
            //如果30分钟内有已经匹配的数据,同一个接口则返回相同的数据;不同的入口返回另外的数据
            if (latestInstantCardOptional30Minutes.get().getStatus() == InstantClassRequestStatus.MATCHED.getCode()) {
                if (getInstantRequestParam().getSelectMode() != latestInstantCardOptional30Minutes.get().getEntrance()) {
                    throw new BusinessException("您当前还有未完成的课程，请稍后再试");
                } else {
                    return matchResultWrapper(latestInstantCardOptional30Minutes.get());
                }
            }
        }

        return dealFirstRequest(timeSlotsOptional);
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

        instantClassCard = instantClassUpdatorService.incrementrequestTeacherTimes(instantClassCard.getId());

        instantClassTeacherService.dealFetchedTeachersAsync(instantClassCard, false);

        //TODO:发起教师请求，同时将匹配的结果返回给App
        return this.matchResultWrapper(InstantClassRequestStatus.WAIT_TO_MATCH, teacherPhotoRequester);
    }

    public Optional<TimeSlots> getMostSimilarSlot(Long roleId) {
        return this.getMostSimilarSlot(teacherStudentRequester.dayTimeSlotsTemplate(roleId));
    }

    private Optional<TimeSlots> getMostSimilarSlot(DayTimeSlots dayTimeSlots) {
        LocalDateTime nextSlotTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(30);
        return dayTimeSlots.getDailyScheduleTime().stream()
                .filter(timeSlot -> nextSlotTime.isAfter(
                        DateUtil.string2LocalDateTime(String.join(
                                " ", DateUtil.date2SimpleString(new Date())
                                , timeSlot.getStartTime()))))
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

    private InstantClassResult matchResultWrapper(InstantClassRequestStatus instantClassRequestStatus
            , TeacherPhotoRequester teacherPhotoRequester) {
        return InstantClassResult.newInstantClassResult(instantClassRequestStatus);
    }

    private InstantClassResult matchResultWrapper(InstantClassCard instantClassCard) {
        if (instantClassCard.getStatus() == InstantClassRequestStatus.NO_MATCH.getCode()) {
            if (instantClassCard.getResultReadFlag() == 0) {
                //将结果的读取值设置为1;表示已经读取了
                instantClassJpaRepository.updateReadFlag(instantClassCard.getId(), 1);
            }
        }
        if (instantClassCard.getStatus() == InstantClassRequestStatus.MATCHED.getCode()) {
            if (instantClassCard.getMatchResultReadFlag() == 0) {
                instantClassJpaRepository.updateMatchedReadFlag(instantClassCard.getId(), 1);
            } else {
                return InstantClassResult.newInstantClassResult(instantClassCard, teacherPhotoRequester);
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

        instantClassCard.initDefault(timeSlots);

        instantClassCard.setStudentId(getInstantRequestParam().getStudentId());
        instantClassCard.setEntrance(getInstantRequestParam().getSelectMode());
        instantClassCard.setTutorType(getInstantRequestParam().getTutorType());
        //课程表入口
        if (getInstantRequestParam().getSelectMode() == 0) {
            instantClassCard.setWorkorderId(getAvaliableWorkOrder().getId());
            instantClassCard.setRoleId(getAvaliableWorkOrder().getSkuId());
        } else {
            instantClassCard.setOrderId(getInstantRequestParam().getOrderId());
            instantClassCard.setProductType(getInstantRequestParam().getProductType());
            instantClassCard.setComboType(getInstantRequestParam().getComboType());
            instantClassCard.setRoleId(TeachingType.WAIJIAO.getCode());
        }
        instantClassCard.setStatus(InstantClassRequestStatus.WAIT_TO_MATCH.getCode());
        return instantClassCard;
    }

    private RecommandCourseView getCourseInfo() {
        RecommandCourseView recommandCourseView = new RecommandCourseView();
        switch (this.getSelectMode()) {
            case COURSE_SCHEDULE_ENTERANCE:
                WorkOrder latestWorkOrder = ThreadLocalUtil.latestWorkOrderThreadLocal.get();
                recommandCourseView.setCourseId(latestWorkOrder.getCourseId());
                recommandCourseView.setCourseType(latestWorkOrder.getCourseType());
                return recommandCourseView;

            case OTHER_ENTERANCE:
                return recommandCourseRequester.getInstantCourseView(
                        getInstantRequestParam().getStudentId(), 1, TutorType.resolve(getInstantRequestParam().getTutorType()));

            default:
                throw new BusinessException("入口参数错误");
        }
    }

    private InstantRequestParam.SelectModeEnum getSelectMode() {
        return InstantRequestParam.SelectModeEnum
                .getSelectMode(getInstantRequestParam().getSelectMode());
    }

    private InstantRequestParam getInstantRequestParam() {
        return ThreadLocalUtil.instantRequestParamThreadLocal.get();
    }

    private WorkOrder getAvaliableWorkOrder() {
        return ThreadLocalUtil.latestWorkOrderThreadLocal.get();
    }

    public Map<String, Object> getScheduleTypeMap(Long studentId) {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.systemDefault());

        List<Integer> skuIds = workOrderJpaRepository.findDistinctSkuIds(
                studentId, DateUtil.localDate2Date(localDateTime.minusMinutes(30)));

        java.util.Map<String, Object> map = new HashMap<>();
        map.put("status", 0);
        map.put("statusDesc", "既无中教也无外教");
        if (!CollectionUtils.isEmpty(skuIds)) {
            if (1 == skuIds.size()) {
                if (skuIds.get(0) == TeachingType.ZHONGJIAO.getCode()) {
                    map.put("status", TeachingType.ZHONGJIAO.getCode());
                    map.put("statusDesc", "只有中教");
                }
                if (skuIds.get(0) == TeachingType.WAIJIAO.getCode()) {
                    map.put("status", TeachingType.WAIJIAO.getCode());
                    map.put("statusDesc", "只有外教");
                }
            } else {
                map.put("status", 3);
                map.put("statusDesc", "既有中教也有外教");
            }
        }
        return map;
    }

}
