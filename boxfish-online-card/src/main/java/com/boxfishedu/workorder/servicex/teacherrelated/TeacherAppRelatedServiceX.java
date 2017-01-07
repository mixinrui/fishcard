package com.boxfishedu.workorder.servicex.teacherrelated;

import com.boxfishedu.workorder.common.bean.FishCardAuthEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.TimeLimitPolicy;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.MonthTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.form.DateRangeForm;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.boxfishedu.workorder.common.util.DateUtil.parseLocalDate;
import static com.boxfishedu.workorder.common.util.DateUtil.parseLocalTime;


/**
 * Created by hucl on 16/3/31.
 */
@SuppressWarnings("ALL")
@org.springframework.stereotype.Service
public class TeacherAppRelatedServiceX {
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private CourseScheduleService courseScheduleService;
    @Autowired
    private ServiceSDK serviceSDK;
    @Value("${parameter.workorder_valid_time_peroid}")
    private String WORKORDER_VALID_TIME_PEROID;
    @Autowired
    private TimeLimitPolicy timeLimitPolicy;
    @Autowired
    private TeacherStudentRequester teacherStudentRequester;
    @Autowired
    private CacheManager cacheManager;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //判断当前鱼卡的上课时间是否有效,上课时间有效时效为配置文件的parameter.workorder_valid_time_peroid,单位是分钟
    //目前按照开课前后的有效时间计算
    public Map<String, Object> isWorkOrderTimeValidOld(Long workOrderId) throws BusinessException {
        logger.info("老师请求上课,开始校验鱼卡:[{}]的有效性", workOrderId);
        Map<String, Object> map = new HashMap<>();
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        if (null == workOrder) {
            map.put("valid", FishCardAuthEnum.NOT_EXISTS.getCode());
            map.put("desc", FishCardAuthEnum.NOT_EXISTS.getDesc());
            return map;
        }
        int validPeroid = Integer.parseInt(WORKORDER_VALID_TIME_PEROID);
        Calendar startcalendar = Calendar.getInstance();
        startcalendar.setTime(workOrder.getStartTime());
        startcalendar.add(Calendar.MINUTE, 0 - validPeroid);

        Date startDate = workOrder.getStartTime();

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(workOrder.getStartTime());
        endCalendar.add(Calendar.MINUTE, validPeroid);
        Date endDate = endCalendar.getTime();

        Date now = new Date();
        Integer status = workOrder.getStatus();
        if ((status > FishCardStatusEnum.ONCLASS.getCode()) && (status < FishCardStatusEnum.TEACHER_ABSENT.getCode())) {
            logger.info("当前鱼卡[{}]课程的状态[{}]不允许上课", workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
            map = tooLateValidation(map, workOrder);
        }
        if (now.before(startDate)) {
            map.put("valid", FishCardAuthEnum.TOO_EARLY.getCode());
            map.put("desc", FishCardAuthEnum.TOO_EARLY.getDesc());
            logger.info("鱼卡:[{}]还未到上课时间的有效范围:", workOrderId);
        } else if (now.after(endDate)) {
            map = tooLateValidation(map, workOrder);
            dealAbsentWorkOrder(workOrder);
        } else {
            map.put("valid", FishCardAuthEnum.OK.getCode());
            map.put("desc", FishCardAuthEnum.OK.getDesc());
            logger.info("鱼卡:[{}]校验通过:", workOrderId);
        }
        return map;
    }

    public Map<String, Object> isWorkOrderTimeValid(Long workOrderId) throws BusinessException {
        Map<String, Object> map = new HashMap<>();
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        logger.info("@isWorkOrderTimeValid老师请求上课,开始校验鱼卡:[{}]的有效性;鱼卡当前状态#[{}]", workOrderId, FishCardStatusEnum.getDesc(workOrder.getStatus()));
        if (null == workOrder) {
            map.put("valid", FishCardAuthEnum.NOT_EXISTS.getCode());
            map.put("desc", FishCardAuthEnum.NOT_EXISTS.getDesc());
            return map;
        }
        Date endDate = workOrder.getEndTime();
        Date deadEndDate = DateUtil.localDate2Date(
                LocalDateTime.ofInstant(
                        endDate.toInstant(), ZoneId.systemDefault()).plusMinutes(5));
        Date startDate = workOrder.getStartTime();
        Date now = new Date();

        if (now.before(startDate)) {
            map.put("valid", FishCardAuthEnum.TOO_EARLY.getCode());
            map.put("desc", FishCardAuthEnum.TOO_EARLY.getDesc());
            logger.info("鱼卡:[{}]还未到上课时间的有效范围:", workOrderId);
        } else if (workOrder.getStatus() == FishCardStatusEnum.TEACHER_ABSENT.getCode()) {
            logger.info("当前鱼卡[{}]课程的状态[{}]不允许上课", workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
            map.put("valid", FishCardAuthEnum.TOO_LATE.getCode());
            map.put("desc", FishCardAuthEnum.TOO_LATE.getDesc());
        } else if (now.after(deadEndDate)) {
            map.put("valid", FishCardAuthEnum.CLASS_OVER.getCode());
            map.put("desc", FishCardAuthEnum.CLASS_OVER.getDesc());
        } else {
            map.put("valid", FishCardAuthEnum.OK.getCode());
            map.put("desc", FishCardAuthEnum.OK.getDesc());
            logger.info("鱼卡:[{}]校验通过:", workOrderId);
        }
        return map;
    }

    private Map<String, Object> tooLateValidation(Map<String, Object> map, WorkOrder workOrder) {
        map.put("valid", FishCardAuthEnum.TOO_LATE.getCode());
        map.put("desc", FishCardAuthEnum.TOO_LATE.getDesc());
        logger.info("鱼卡:[{}]超出上课时间的有效范围:", workOrder.getId());
        return map;
    }

    public MonthTimeSlots getScheduleByIdAndDateRange(
            Long teacherId, YearMonth yearMonth,
            Integer count, Locale locale) {
        DateRangeForm dateRangeForm;
        // 如果没有指定查询的年月,则默认查询半年的数据
        if (yearMonth == null) {
            // 默认为当月+6个月,传参数默认为单月+1月
            int months = count == null ? 5 : 1;
            dateRangeForm = DateUtil.createHalfYearDateRangeForm(months);
        } else {
            dateRangeForm = new DateRangeForm(
                    DateUtil.convertToDate(yearMonth.atDay(1)),
                    DateUtil.convertToDate(yearMonth.atEndOfMonth()));
        }
        logger.info("用户[{}]发起获取课程表请求", teacherId);
        // 1 获取老师已选时间片列表
        MonthTimeSlots resultMonthTimeSlots = serviceSDK.getMonthTimeSlotsByDateBetween(teacherId, dateRangeForm);
        // 设置是否包含更多的历史记录
        resultMonthTimeSlots.setHasMoreHistory(hasMoreHistory(teacherId, dateRangeForm));
        if (CollectionUtils.isEmpty(resultMonthTimeSlots.getData())) {
            return resultMonthTimeSlots;
        }
        // 2 获取排课表信息
        Map<String, Map<String, CourseSchedule>> courseScheduleMap = courseSchedule(teacherId, dateRangeForm);

        // 3 覆盖处理
        resultMonthTimeSlots.override(courseScheduleMap, serviceSDK, locale);

        return resultMonthTimeSlots.filter(
                // 第一个条件为当前时间之前
                d -> LocalDate.now().isAfter(parseLocalDate(d.getDay())),
                // 第二个条件为老师已分配
                t -> t.getCourseScheduleStatus() != FishCardStatusEnum.UNKNOWN.getCode());
//        return JsonResultModel.newJsonResultModel(resultMonthTimeSlots.getData());
    }


    public JsonResultModel getScheduleByIdAndDate(Long teacherId, Date date, Locale locale) {
        logger.info("用户[{}]发起获取{}课程请求", teacherId, date);

        // 1 获取一天老师选择的时间片
        DateRangeForm dateRangeForm = new DateRangeForm(date, date);
        MonthTimeSlots resultMonthTimeSlots = serviceSDK.getMonthTimeSlotsByDateBetween(teacherId, dateRangeForm);
        List<DayTimeSlots> dayTimeSlotsList = resultMonthTimeSlots.getData();
        if (CollectionUtils.isEmpty(dayTimeSlotsList)) {
            DayTimeSlots dayTimeSlotsTemplate = teacherStudentRequester.dayTimeSlotsTemplate(teacherId, date);
            return JsonResultModel.newJsonResultModel(
                    timeLimitPolicy.limit(dayTimeSlotsTemplate).filter(
                            d -> LocalDate.now().isAfter(parseLocalDate(d.getDay())),
                            t -> t.getCourseScheduleStatus() != FishCardStatusEnum.UNKNOWN.getCode()
                    ));
        }
        DayTimeSlots dayTimeSlots = dayTimeSlotsList.get(0);

        // 2 获取老师遮天选的课
        List<CourseSchedule> courseScheduleList = courseScheduleService
                .findByClassDateAndTeacherId(date, teacherId);

        // 3 覆盖
        dayTimeSlots.override(courseScheduleList, serviceSDK, locale);
        return JsonResultModel.newJsonResultModel(timeLimitPolicy.limit(dayTimeSlots).filter(
                d -> LocalDate.now().isAfter(parseLocalDate(d.getDay())),
                t -> t.getCourseScheduleStatus() != FishCardStatusEnum.UNKNOWN.getCode()
        ));
    }


    /**
     * 获取排课表数据
     *
     * @param teacherId
     * @param dateRangeForm
     * @return
     */
    private Map<String, Map<String, CourseSchedule>> courseSchedule(Long teacherId, DateRangeForm dateRangeForm) {
        List<CourseSchedule> courseSchedules = courseScheduleService.findByTeacherIdAndClassDateBetween(teacherId, dateRangeForm);
        if (CollectionUtils.isEmpty(courseSchedules)) {
            return Collections.EMPTY_MAP;
        }
        // 分组处理
        return courseScheduleService.groupCourseScheduleByDate(courseSchedules);
    }

    public JsonResultModel getScheduleAssignedByIdAndDate(Long teacherId, Date date, Locale locale) {
        List<CourseSchedule> courseScheduleList = courseScheduleService
                .findByClassDateAndTeacherId(date, teacherId);
        if (CollectionUtils.isEmpty(courseScheduleList)) {
            return JsonResultModel.newJsonResultModel(null);
        }

        DayTimeSlots dayTimeSlots = new DayTimeSlots(DateUtil.simpleDate2String(date));
        courseScheduleList.forEach(courseSchedule -> {
            TimeSlots timeSlots = new TimeSlots(courseSchedule);
            dayTimeSlots.addTimeSlots(timeSlots);
            TimeSlots time = teacherStudentRequester.getTimeSlot(courseSchedule.getTimeSlotId());
            timeSlots.setStartTime(time.getStartTime());
            timeSlots.setEndTime(time.getEndTime());
            timeSlots.setSelected(true);
            if (StringUtils.isNotEmpty(courseSchedule.getCourseId())) {
                timeSlots.setCourseView(serviceSDK.getCourseInfoByScheduleId(courseSchedule.getId(), locale));
            }
        });
        return JsonResultModel.newJsonResultModel(dayTimeSlots);
    }

    private void dealAbsentWorkOrder(WorkOrder workOrder) {
        if (workOrder.getStatus() != FishCardStatusEnum.TEACHER_ABSENT.getCode()) {
            workOrder.setStatus(FishCardStatusEnum.TEACHER_ABSENT.getCode());
        }
        workOrderService.save(workOrder);
    }

    /**
     * @param teacherId
     * @param date
     * @return
     */
    public JsonResultModel getInternationalDayTimeSlotsTemplate(Long teacherId, Date date) throws CloneNotSupportedException {

        // 日期范围
        DateRangeForm dateRangeForm = getInternationalDateRange(date);
        // 具体时间范围
        DateRangeForm dateTimeRangeForm = getInternationalDateTimeRange(date);
        List<DayTimeSlots> result = dateRangeForm
                .collect(loopDate -> teacherStudentRequester.dayTimeSlotsTemplate(teacherId, loopDate))
                .stream()
                // 国际化时间
                .map(d -> this.filterInternationalDayTimeSlots(d, dateTimeRangeForm))
                // 过滤历史日期
                .map(dayTimeSlots -> timeLimitPolicy.limit(dayTimeSlots).filter(
                        d -> LocalDate.now().isAfter(parseLocalDate(d.getDay())),
                        t -> t.getCourseScheduleStatus() != FishCardStatusEnum.UNKNOWN.getCode()
                ))
                // 非空
                .filter(this::notEmptyPredicate)
                .collect(Collectors.toList());
        return JsonResultModel.newJsonResultModel(result);
    }

    /**
     * 获取日期范围,不带时分秒
     *
     * @param date
     * @return
     */
    private DateRangeForm getInternationalDateRange(Date date) {
        Date from = DateUtil.getStartTime(date);
        Date to = DateUtil.getStartTime(DateUtil.getTomorrowByDate(date));
        return new DateRangeForm(from, to);
    }

    /**
     * 获取时间范围
     *
     * @param date
     * @return
     */
    private DateRangeForm getInternationalDateTimeRange(Date date) {
        Date from = new Date(date.getTime());
        Date to = new Date(date.getTime() + MonthTimeSlots.DAY_OF_MILLIONS);
        return new DateRangeForm(from, to);
    }

    private DayTimeSlots filterInternationalDayTimeSlots(DayTimeSlots dayTimeSlots, DateRangeForm dateTimeRangeForm) {
        List<TimeSlots> dailyScheduleTime = dayTimeSlots.getDailyScheduleTime();
        Iterator<TimeSlots> iterator = dailyScheduleTime.iterator();
        while (iterator.hasNext()) {
            TimeSlots timeSlots = iterator.next();
            LocalDateTime startTime = DateUtil.merge(
                    parseLocalDate(dayTimeSlots.getDay()),
                    parseLocalTime(timeSlots.getStartTime()));
            if (!dateTimeRangeForm.isWithIn(startTime)) {
                iterator.remove();
            }
        }
        return dayTimeSlots;
    }

    public JsonResultModel getInternationalScheduleByIdAndDate(Long teacherId, Date date, Locale locale) throws CloneNotSupportedException {
        logger.info("用户[{}]发起获取{}课程请求", teacherId, date);

        // 1 获取一天老师选择的时间片
        DateRangeForm dateRangeForm = getInternationalDateRange(date);
        MonthTimeSlots resultMonthTimeSlots = serviceSDK.getMonthTimeSlotsByDateBetween(teacherId, dateRangeForm);
        List<DayTimeSlots> dayTimeSlotsList = transfer(resultMonthTimeSlots, dateRangeForm, teacherId);
        if (CollectionUtils.isEmpty(dayTimeSlotsList)) {
            return getInternationalDayTimeSlotsTemplate(teacherId, date);
        }

        List<CourseSchedule> courseScheduleList = courseScheduleService.findByTeacherIdAndClassDateBetween(
                teacherId, getInternationalDateRange(date));

        List<DayTimeSlots> result = dayTimeSlotsList.parallelStream()
                                                    // 可选时间过滤,即北京时间周一到周五  周六到周日的时间规则
                                                    .map(timeLimitPolicy::limit)
                                                    // 判空
                                                    .filter(this::notEmptyPredicate)
                                                    // 国际化时间转换
                                                    .map(d -> this.filterInternationalDayTimeSlots(d, getInternationalDateTimeRange(date)))
                                                    .filter(this::notEmptyPredicate)
                                                    // 3 覆盖,课程信息覆盖
                                                    .peek(d -> d.override(courseScheduleList, serviceSDK, locale))
                                                    // 历史日期时间片过滤,只显示有课的
                                                    .map(dayTimeSLots -> dayTimeSLots.filter(
                                                            d -> LocalDate.now().isAfter(parseLocalDate(d.getDay())),
                                                            t -> t.getCourseScheduleStatus() != FishCardStatusEnum.UNKNOWN.getCode()))
                                                    // 非空
                                                    .filter(d -> d != null)
                                                    // 排序
                                                    //.sorted((f, s) -> Long.compare(f.getDayStamp(), s.getDayStamp()))
                                                    .collect(Collectors.toList());
        return JsonResultModel.newJsonResultModel(result);
    }


    private boolean notEmptyPredicate(DayTimeSlots d) {
        return d != null && CollectionUtils.isNotEmpty(d.getDailyScheduleTime());
    }


    private List<DayTimeSlots> transfer(MonthTimeSlots resultMonthTimeSlots, DateRangeForm dateRangeForm, Long teacherId)
            throws CloneNotSupportedException {
        List<DayTimeSlots> dayTimeSlotsList = resultMonthTimeSlots.getData();
        if (CollectionUtils.isEmpty(dayTimeSlotsList)) {
            return null;
        }

        Map<String, DayTimeSlots> dayTimeSlotsMap = Maps.newHashMap();
        for (DayTimeSlots dayTimeSlots : dayTimeSlotsList) {
            dayTimeSlotsMap.put(dayTimeSlots.getDay(), dayTimeSlots);
        }

        return dateRangeForm.collect(date -> {
            String key = DateUtil.simpleDate2String(date);
            DayTimeSlots dayTimeSlots = dayTimeSlotsMap.get(key);
            if (dayTimeSlots == null) {
                dayTimeSlots = teacherStudentRequester.dayTimeSlotsTemplate(teacherId, date);
                dayTimeSlotsMap.put(key, dayTimeSlots);
            }
            return dayTimeSlots;
        });
    }

    private boolean hasMoreHistory(Long teacherId, DateRangeForm dateRangeForm) {
        // 先查询缓存
        Long firstDayTimeStamp = cacheManager.getCache(
                CacheKeyConstant.SCHEDULE_HAS_MORE_HISTORY).get(teacherId, Long.class);
        // 如果缓存为空,再查数据库
        if (firstDayTimeStamp == null) {
            Optional<Date> firstDay = courseScheduleService.findMinClassDateByTeacherId(teacherId);
            if (firstDay.isPresent()) {
                firstDayTimeStamp = firstDay.get().getTime();
                // 将数据保存进缓存
                cacheManager.getCache(CacheKeyConstant.SCHEDULE_HAS_MORE_HISTORY).put(teacherId, firstDayTimeStamp);
            }
        }
        return (firstDayTimeStamp != null) && (dateRangeForm.getFrom().getTime() > firstDayTimeStamp);
    }

}
