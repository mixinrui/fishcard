package com.boxfishedu.workorder.servicex.studentrelated;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.mall.enums.ProductType;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.BaseTimeSlotJpaRepository;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.BaseTimeSlots;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.RedisMapService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.MonthTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.boxfishedu.workorder.web.view.base.DateRange;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/5/17.
 * 新版本
 */
@Component
public class AvaliableTimeServiceXV1 {

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private BaseTimeSlotJpaRepository baseTimeSlotJpaRepository;

    @Autowired
    private RedisMapService redisMapService;

    @Autowired
    private ServeService serveService;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    /**
     * 免费体验的天数
     */
    @Value("${choiceTime.freeExperienceDay:7}")
    private Integer freeExperienceDay;
    /**
     * 选时间生效天数,默认为第二天生效
     */
    @Value("${choiceTime.consumerStartDay:2}")
    private Integer consumerStartDay;
    private final static int daysOfWeek = 7;
    private final static int daysOfMonth = 30;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 选时间
     *
     * @param avaliableTimeParam
     * @return
     * @throws CloneNotSupportedException
     */
    public JsonResultModel getTimeAvailable(AvaliableTimeParam avaliableTimeParam) throws CloneNotSupportedException {
        List places = new ArrayList<Integer>(Arrays.asList(4, 6, 7, 1));
        // 获取时间区间
        DateRange dateRange = getEnableDateRange(avaliableTimeParam, getOptionalDays(avaliableTimeParam));

        Set<String> classDateTimeSlotsSet = courseScheduleService.findByStudentIdAndAfterDate(avaliableTimeParam.getStudentId());
        // 获取时间片模板,并且复制
        for (int i = 0; i < 4; i++) {
            List<DayTimeSlots> result = getTimeAvailable(avaliableTimeParam, dateRange, classDateTimeSlotsSet);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(result)) {
                return JsonResultModel.newJsonResultModel(new MonthTimeSlots(result).getData());
            } else {
                // 如果为空, 则推后一周
                dateRange.incrementAWeek();
            }
        }
        // 如果推后了一个月都没有可选时间则, 则返回空
        return JsonResultModel.newJsonResultModel();
    }


    private List<DayTimeSlots> getTimeAvailable(AvaliableTimeParam avaliableTimeParam, DateRange dateRange, Set<String> classDateTimeSlotsSet) throws CloneNotSupportedException {
        // 获取时间片模板,并且复制
        final List<Integer> list = Lists.newArrayList(4, 6, 7, 1);
        return dateRange.forEach(
                // 过滤掉时间片
                (localDateTime, dayTimeSlot) -> {

                    //小班课
                    if (ClassTypeEnum.SMALL.name().equals(avaliableTimeParam.getClassType())) {
                        dayTimeSlot.setDailyScheduleTime(dayTimeSlot.getDailyScheduleTime().stream()
                                .filter(t -> !classDateTimeSlotsSet.contains(String.join(" ", dayTimeSlot.getDay(), t.getSlotId().toString()))
                                        &&
                                        DateUtil.getWeekInByDate(DateUtil.String2Date(String.join(" ", dayTimeSlot.getDay(), "00:00:00")), list)
                                        && t.getSlotId() == 27
                                )// 27 晚8点  小班课
                                .collect(Collectors.toList()));
                        //1对1课程
                    } else {
                        dayTimeSlot.setDailyScheduleTime(dayTimeSlot.getDailyScheduleTime().stream()
                                .filter(t -> !classDateTimeSlotsSet.contains(String.join(" ", dayTimeSlot.getDay(), t.getSlotId().toString())))
                                .collect(Collectors.toList()));
                    }


                    return CollectionUtils.isEmpty(dayTimeSlot.getDailyScheduleTime()) ? null : dayTimeSlot;
                },
                // 根据日期获取到对应的DayTimeSlots
                (d) -> {
                    int teachingType = BaseTimeSlots.TEACHING_TYPE_CN;
                    if (StringUtils.equals(avaliableTimeParam.getTutorType(), TutorType.FRN.name())) {
                        teachingType = BaseTimeSlots.TEACHING_TYPE_FRN;
                    }
                    List<BaseTimeSlots> timeSlotsList = redisMapService.getMap(teachingType + "" + BaseTimeSlots.CLIENT_TYPE_STU, DateUtil.localDate2SimpleString(d));
                    if (CollectionUtils.isEmpty(timeSlotsList)) {
                        timeSlotsList = baseTimeSlotJpaRepository.findByClassDateAndTeachingTypeAndClientType(DateUtil.convertToDate(d.toLocalDate()), teachingType, BaseTimeSlots.CLIENT_TYPE_STU);
                        redisMapService.setMap(teachingType + "" + BaseTimeSlots.CLIENT_TYPE_STU, DateUtil.localDate2SimpleString(d), timeSlotsList);
                        ;
                    }

                    //小班课单独处理 只分0% 和 100% 对待
                    if (ClassTypeEnum.SMALL.name().equals(avaliableTimeParam.getClassType())) {
                        return createDayTimeSlotsSmallClass(d, timeSlotsList);
                    } else {
                        return createDayTimeSlots(d, timeSlotsList);
                    }

                });
    }


    public DayTimeSlots createDayTimeSlots(LocalDateTime date, List<BaseTimeSlots> timeSlotsList) {
        DayTimeSlots result = new DayTimeSlots();
        result.setDay(DateUtil.formatLocalDate(date));
        List<TimeSlots> list = timeSlotsList.stream()
                .filter(BaseTimeSlots::roll)
                .map(baseTimeSlots -> {
                    TimeSlots timeSlots = new TimeSlots();
                    timeSlots.setSlotId(baseTimeSlots.getSlotId().longValue());
                    timeSlots.setStartTime(DateUtil.timeShortString(baseTimeSlots.getStartTime()));
                    timeSlots.setEndTime(DateUtil.timeShortString(baseTimeSlots.getEndTime()));
                    return timeSlots;
                }).collect(Collectors.toList());
        result.setDailyScheduleTime(list);
        return result;
    }

    public DayTimeSlots createDayTimeSlotsSmallClass(LocalDateTime date, List<BaseTimeSlots> timeSlotsList) {
        DayTimeSlots result = new DayTimeSlots();
        result.setDay(DateUtil.formatLocalDate(date));
        List<TimeSlots> list = timeSlotsList.stream()
                .filter(BaseTimeSlots::rollForSmallClass)
                .map(baseTimeSlots -> {
                    TimeSlots timeSlots = new TimeSlots();
                    timeSlots.setSlotId(baseTimeSlots.getSlotId().longValue());
                    timeSlots.setStartTime(DateUtil.timeShortString(baseTimeSlots.getStartTime()));
                    timeSlots.setEndTime(DateUtil.timeShortString(baseTimeSlots.getEndTime()));
                    return timeSlots;
                }).collect(Collectors.toList());
        result.setDailyScheduleTime(list);
        return result;
    }


    /**
     * 获取可选的时间区间
     *
     * @return
     */
    private DateRange getEnableDateRange(AvaliableTimeParam avaliableTimeParam, Integer days) {
        LocalDateTime startDate = null;
        if (null == avaliableTimeParam.getDelayWeek()) {


            // 如果没有未消费的订单,则取得当前时间;否则换成订单的最后结束时间
            WorkOrder workOrder = null;
            try {
                // 如果是overall,设置为MIXED
                if (Objects.equals(avaliableTimeParam.getComboType(), ComboTypeToRoleId.OVERALL.name())) {
                    avaliableTimeParam.setTutorType(TutorType.MIXED.name());
                }
                workOrder = workOrderService.getLatestWorkOrderByStudentIdAndProductTypeAndTutorType(
                        avaliableTimeParam.getStudentId(), ProductType.TEACHING.value(), avaliableTimeParam.getTutorType());
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error("获取可用时间片时获取鱼卡失败,此次选课为该学生的首单选课");
            }
            Date date = new Date();
            int afterDays = consumerStartDay;

            // 推迟上课周数
            if (null != avaliableTimeParam.getDelayWeek()) {
                afterDays += (daysOfWeek * avaliableTimeParam.getDelayWeek());
            }


            // 同类型工单的最后一个工单
            if (null != workOrder && workOrder.getEndTime().after(date)) {
                date = workOrder.getEndTime();
                afterDays = 1;
            }
            startDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            if (afterDays > 0) {
                startDate = startDate.plusDays(afterDays);
            }

        } else {
            // 延迟上课
            startDate = LocalDateTime.ofInstant(DateUtil.String2Date(avaliableTimeParam.getRangeStartTime()).toInstant(), ZoneId.systemDefault());
            days = daysOfWeek;
        }
        return new DateRange(startDate, days);
    }

    /**
     * 获取返回的一次选时间的日期总数,默认为一周,如果自由选择,则为一个月
     *
     * @param avaliableTimeParam
     * @return
     */
    private int getOptionalDays(AvaliableTimeParam avaliableTimeParam) {
        // 判断选择模式,如果是模板模式,则为一周. 默认为模板方式
        if (Objects.isNull(avaliableTimeParam.getSelectMode())
                || Objects.equals(avaliableTimeParam.getSelectMode(), SelectMode.TEMPLATE)) {
            return avaliableTimeParam.getIsFree() ? freeExperienceDay : daysOfWeek;
        } else {
            return avaliableTimeParam.getIsFree() ? freeExperienceDay : daysOfMonth;
        }
    }


    /**
     * 获取延迟时间列表
     */
    public JsonResultModel getDelayWeekDays() throws Exception {
        List delayRange = Lists.newArrayList();
        Date currentDate = new Date();
        boolean weekFlag = DateUtil.getWeekDay();

        for (int i = 1; i < 9; i++) {
            JSONObject jb = new JSONObject();

            String firstWeek = weekFlag ? "下周开始" : "本周开始";
            String text = i == 1 ? firstWeek + " (" + DateUtil.formatMonthDay2String(DateUtil.getAfterTomoDate(currentDate)) + ")" :
                    "第" + String.valueOf(i) + "周开始" + " (" + DateUtil.formatMonthDay2String(DateUtil.getMonday(DateUtil.getAfter7Days(currentDate, weekFlag ? (i + 1) : (i)))) + ")";
            Date date = i == 1 ? DateUtil.getAfterTomoDate(currentDate) : DateUtil.getMonday(DateUtil.getAfter7Days(currentDate, weekFlag ? (i + 1) : (i)));
            jb.put("id", i);
            jb.put("text", text);
            jb.put("date", DateUtil.Date2String24(date));
            delayRange.add(jb);
        }

        return JsonResultModel.newJsonResultModel(delayRange);
    }

    public JsonResultModel getDelayWeekDaysForSmallClass(Long orderId, Long userId) throws Exception {
        List delayRange = Lists.newArrayList();
        Date currentDate = new Date();

        List<Service> services = serveService.findByOrderId(orderId);
        if (CollectionUtils.isEmpty(services)) {
            throw new BusinessException("未生成服务数据");
        }

        // 获取服务信息   返回获取一周几次课  service 的 original_amount 次数 除  combo_cycle
        Service service = services.get(0);
        int countByWeek = service.getOriginalAmount() / service.getComboCycle();
        int yushuByWeek = service.getOriginalAmount() % service.getComboCycle();
        if (0 != yushuByWeek) {
            //countByWeek=countByWeek+1;
        }

        logger.info("getDelayWeekDaysForSmallClass userId:[{}] ,countByWeek:[{}],yushuByWeek:[{}]", userId, countByWeek, yushuByWeek);


        for (int i = 1; i < 9; i++) {
            JSONObject jb = new JSONObject();
            String firstWeek = "下周开始";
            String text = i == 1 ? firstWeek + " (" + DateUtil.formatMonthDay2String(DateUtil.getAfter7Days(currentDate, 2)) + ")" : "第" + String.valueOf(i) + "周开始" + " (" + DateUtil.formatMonthDay2String(DateUtil.getMonday(DateUtil.getAfter7Days(DateUtil.getAfter7Days(currentDate, 2), i))) + ")";
            Date date = i == 1 ? DateUtil.getAfter7Days(currentDate, 2) : DateUtil.getMonday(DateUtil.getAfter7Days(DateUtil.getAfter7Days(currentDate, 2), i));
            jb.put("id", i);
            jb.put("text", text);
            jb.put("date", DateUtil.Date2String24(date));
            jb.put("show", true);
            jb.put("realDate", date);
            jb.put("endDate", DateUtil.getAfter7Days(date, (service.getComboCycle() + 1)));
            delayRange.add(jb);
        }

        //  1 计算时间总跨度

        Date beginDate = DateUtil.String2SimpleDate(DateUtil.Date2String24(((JSONObject) delayRange.get(0)).getDate("realDate")));

        Date endDate = DateUtil.String2SimpleDate(DateUtil.Date2String24(((JSONObject) delayRange.get(7)).getDate("endDate")));

        logger.info("date begin:[{}],endDate:[{}]", DateUtil.Date2String24(beginDate), DateUtil.Date2String24(endDate));

        // 获取课程信息 每天晚8点时间片 为27
        List<CourseSchedule> listCourses = courseScheduleRepository.findByMyClasses(userId, beginDate, endDate, Lists.newArrayList(27));

        if (CollectionUtils.isEmpty(listCourses)) {
            return JsonResultModel.newJsonResultModel(delayRange);
        }

        Map<String, Integer> courseMap = extractToMap(listCourses, "classDate", "timeSlotId");

        // 循环 判断每周 是否满足条件  countByWeek每周
        for (int i = 0; i < 8; i++) {
            JSONObject jsonObject = (JSONObject) delayRange.get(i);

            Date realDate = jsonObject.getDate("realDate");

            for (int j = 1; j <= service.getComboCycle(); j++) {

                List<String> compareDateList = getAvaliableDateRange(realDate, 1); //每次向后叠加一周

                //如果每周的时间片 小于每次几次课的数量 ,本周不可用
                if (compareDateList.size() < countByWeek) {
                    jsonObject.put("show", false);
                    break;
                }

                //每周的课程数量
                int countClassesWeek = 0;

                for (String o : compareDateList) {
                    if (courseMap.get(o) != null) countClassesWeek++;
                }

                // 可用的时间片数量 减去 每周已有课程的数量 小于
                if ((compareDateList.size() - countClassesWeek) < countByWeek) {
                    jsonObject.put("show", false);
                    break;
                }


                realDate = DateUtil.getAfter7Days(realDate, 2); //每次向后加7天
            }

        }

        return JsonResultModel.newJsonResultModel(delayRange);
    }

    //获取可用的日期用于过滤
    private List<String> getAvaliableDateRange(Date beginDate, int comboCycle) {
        List<String> listDate = Lists.newArrayList();
        for (int i = 1; i <= comboCycle; i++) {
            beginDate = i == 1 ? beginDate : DateUtil.getAfter7Days(beginDate, 2);
            Date baseDate = beginDate;
            for (int j = 0; j < 7; j++) {
                if (DateUtil.getWeekDay3567(DateUtil.getAfterOneDay(baseDate, j))) {
                    listDate.add(DateUtil.date2SimpleString(DateUtil.getAfterOneDay(baseDate, j)));
                }
            }

        }

        System.out.println(JSONObject.toJSONString(listDate));
        return listDate;

    }


    public static Map extractToMap(final Collection collection, final String keyPropertyName, final String valuePropertyName) {
        Map map = new HashMap(collection.size());

        try {
            for (Object obj : collection) {
                map.put(DateUtil.date2SimpleString((Date) PropertyUtils.getProperty(obj, keyPropertyName)), PropertyUtils.getProperty(obj, valuePropertyName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public static void main(String[] args) {
//        AvaliableTimeServiceXV1 avaliableTimeServiceXV1 = new AvaliableTimeServiceXV1();
//        avaliableTimeServiceXV1.getAvaliableDateRange(new Date(), 4);
        List<String> list = Arrays.asList("123", "45634", "7892", "abch", "sdfhrthj", "mvkd");
        for (int i = 0; i < 5; i++) {
            list.stream().forEach(e -> {
                if (e.length() >= 5) {
                    return;
                }
                System.out.println(e);
            });
        }
    }


}
