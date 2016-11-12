package com.boxfishedu.workorder.service.studentrelated;

import com.boxfishedu.mall.enums.OrderChannelDesc;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.SlotRuleEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.mongo.TimeLimitRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.TimeLimitRules;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.SimpleTimeLimitPolicy;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/8/23.
 */
@Component
@SuppressWarnings("ALL")
public class RandomSlotFilterService {
    @Autowired
    private TimeLimitRulesMorphiaRepository timeLimitRulesMorphiaRepository;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    public DayTimeSlots removeExculdeSlot(DayTimeSlots dayTimeSlots, AvaliableTimeParam avaliableTimeParam) {
        if (dayTimeSlots == null || CollectionUtils.isEmpty(dayTimeSlots.getDailyScheduleTime())) {
            return dayTimeSlots;
        }
        Date day = DateUtil.String2SimpleDate(dayTimeSlots.getDay());
        Optional<List<SimpleTimeLimitPolicy.TimeRange>> timeRangeOptional = getExcludeDateRange(avaliableTimeParam.getComboType(),avaliableTimeParam.getTutorType(), day);
        if (!timeRangeOptional.isPresent()) {
            return dayTimeSlots;
        }
        dayTimeSlots.setDailyScheduleTime(
                dayTimeSlots.getDailyScheduleTime().stream().filter(
                        timeSlots -> isIncludeSlot(timeSlots, avaliableTimeParam.getComboType(), day, timeRangeOptional.get())
                ).collect(Collectors.toList()));
        return dayTimeSlots;
    }

    //是否为该包含的时间片
    private boolean isIncludeSlot(TimeSlots timeSlots, String comboType, Date day, List<SimpleTimeLimitPolicy.TimeRange> timeRanges) {
        for (SimpleTimeLimitPolicy.TimeRange timeRange : timeRanges) {
            Date startTime = DateUtil.String2Date(String.join(" ", DateUtil.date2SimpleString(timeRange.getFrom()), timeSlots.getStartTime()));
            if (!(startTime.before(timeRange.getFrom()) || startTime.after(timeRange.getTo()))) {
                logger.info("@==============================================isIncludeSlot#false#startTime[{}]#from[{}]#to[{}]",
                        startTime, timeRange.getFrom(), timeRange.getTo());
                return false;
            }
        }
        return true;
    }

    //互斥的时间片
    private Optional<List<SimpleTimeLimitPolicy.TimeRange>> getExcludeDateRange(String comboType,String tutorType, Date day) {
        TutorType tt = TutorType.valueOf(tutorType);
        switch (tt) {
            case CN:
            case MIXED: comboType= OrderChannelDesc.OVERALL.toString(); break;
            case FRN: comboType= OrderChannelDesc.CHINESE.toString(); break;
            default: comboType= OrderChannelDesc.OVERALL.toString(); break;
        }
        logger.info("@getExcludeDateRange#comboType[{}]#day[{}]", comboType, day);
        Optional<List<TimeLimitRules>> timeLimitRuleOptional = timeLimitRulesMorphiaRepository.queryByComboTypeAndRuleAndDay(comboType, SlotRuleEnum.MUTEX, DateUtil.getDayOfWeek(day));
        if (!timeLimitRuleOptional.isPresent()) {
            return Optional.empty();
        }
        List<TimeLimitRules> timeLimitRules = timeLimitRuleOptional.get();
        List<SimpleTimeLimitPolicy.TimeRange> timeRanges = Lists.newArrayList();

        logger.info("@getExcludeDateRange#timeLimitRules[{}]", timeLimitRules);

        Random rand = new Random();
        int pickedNum = rand.nextInt(timeLimitRules.size());
        int pivot = 0;

        for (TimeLimitRules timeLimitRule : timeLimitRules) {
            if (pickedNum == pivot) {
                logger.debug("&&&&&&&&&&&&&&&&&&&&&保留的时间day[{}],from[{}],to[{}]", day, timeLimitRule.getFrom(), timeLimitRule.getTo());
                pivot++;
                continue;
            }
            SimpleTimeLimitPolicy.TimeRange timeRange = new SimpleTimeLimitPolicy.TimeRange(
                    DateUtil.String2Date(String.join(" ", DateUtil.date2SimpleString(day), timeLimitRule.getFrom())),
                    DateUtil.String2Date(String.join(" ", DateUtil.date2SimpleString(day), timeLimitRule.getTo()))
            );
            timeRanges.add(timeRange);
            pivot++;
        }
        return Optional.ofNullable(timeRanges);
    }

    /**
     * 时间片范围
     */
    private Optional<List<SimpleTimeLimitPolicy.TimeRange>> getIncludeDateRange(String comboType,String tutorType, Date day) {
        TutorType tt = TutorType.valueOf(tutorType);
        switch (tt) {
            case CN:
            case MIXED: comboType= OrderChannelDesc.OVERALL.toString(); break;
            case FRN: comboType= OrderChannelDesc.CHINESE.toString(); break;
            default: comboType= OrderChannelDesc.OVERALL.toString(); break;
        }
        Optional<List<TimeLimitRules>> timeLimitRuleOptional = timeLimitRulesMorphiaRepository.queryByComboTypeAndRuleAndDay(comboType, SlotRuleEnum.RANGE, DateUtil.getDayOfWeek(day));
        if (!timeLimitRuleOptional.isPresent()) {
            return Optional.empty();
        }
        List<TimeLimitRules> timeLimitRules = timeLimitRuleOptional.get();
        List<SimpleTimeLimitPolicy.TimeRange> timeRanges = Lists.newArrayList();

        logger.info("@getIncludeDateRange#timeLimitRules[{}]", timeLimitRules);

        for (TimeLimitRules timeLimitRule : timeLimitRules) {
            SimpleTimeLimitPolicy.TimeRange timeRange = new SimpleTimeLimitPolicy.TimeRange(
                    DateUtil.String2Date(String.join(" ", DateUtil.date2SimpleString(day), timeLimitRule.getLimitMini())),
                    DateUtil.String2Date(String.join(" ", DateUtil.date2SimpleString(day), timeLimitRule.getLimitMax()))
            );
            timeRanges.add(timeRange);
        }
        return Optional.ofNullable(timeRanges);
    }

    //删除不在时间范围的slot
    public DayTimeSlots removeSlotsNotInRange(DayTimeSlots dayTimeSlots, AvaliableTimeParam avaliableTimeParam) {
        if (dayTimeSlots == null || CollectionUtils.isEmpty(dayTimeSlots.getDailyScheduleTime())) {
            return dayTimeSlots;
        }
        Date day = DateUtil.String2SimpleDate(dayTimeSlots.getDay());
        Optional<List<SimpleTimeLimitPolicy.TimeRange>> timeRangeOptional = getIncludeDateRange(avaliableTimeParam.getComboType(),avaliableTimeParam.getTutorType(), day);
        if (!timeRangeOptional.isPresent()) {
            logger.debug("@removeSlotsNotInRange无可显示的slot");
            return dayTimeSlots;
        }
        dayTimeSlots.setDailyScheduleTime(
                dayTimeSlots.getDailyScheduleTime().stream().filter(
                        timeSlots -> isInTimeRange(timeSlots, avaliableTimeParam.getComboType(), day, timeRangeOptional.get())
                ).collect(Collectors.toList()));
        return dayTimeSlots;
    }

//    public DayTimeSlots removeSlotsNotInRange(DayTimeSlots dayTimeSlots, Long teacherId) {
//        AvaliableTimeParam avaliableTimeParam = new AvaliableTimeParam();
//        avaliableTimeParam.setComboType(getComboTypeByTeacherId(teacherId));
//        return this.removeSlotsNotInRange(dayTimeSlots, avaliableTimeParam);
//    }

    //是否为该包含的时间片
    private boolean isInTimeRange(TimeSlots timeSlots, String comboType, Date day, List<SimpleTimeLimitPolicy.TimeRange> timeRanges) {
        for (SimpleTimeLimitPolicy.TimeRange timeRange : timeRanges) {
            Date startTime = DateUtil.String2Date(String.join(" ", DateUtil.date2SimpleString(timeRange.getFrom()), timeSlots.getStartTime()));
            if (!(startTime.before(timeRange.getFrom()) || startTime.after(timeRange.getTo()))) {
                logger.debug("@<<<<<<<<<<<isIncludeSlot>>>>>>>>>>>>#true#startTime[{}]#from[{}]#to[{}]",
                        startTime, timeRange.getFrom(), timeRange.getTo());
                return true;
            }
        }
        return false;
    }

//    private String getComboTypeByTeacherId(Long teacherId) {
//        try {
//            int teacherType = teacherStudentRequester.getTeacherType(teacherId);
//            if (teacherType == TeachingType.WAIJIAO.getCode()) {
//                return ComboTypeEnum.FOREIGN.toString();
//            } else {
//                return ComboTypeEnum.OVERALL.toString();
//            }
//        } catch (Exception ex) {
//            logger.error("@getComboTypeByTeacherId#eror", ex);
//            return ComboTypeEnum.OVERALL.toString();
//        }
//    }
}
