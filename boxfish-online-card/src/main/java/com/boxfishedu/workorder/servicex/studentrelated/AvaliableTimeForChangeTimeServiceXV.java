package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.BaseTimeSlotJpaRepository;
import com.boxfishedu.workorder.entity.mysql.BaseTimeSlots;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.*;
import com.boxfishedu.workorder.service.baseTime.BaseTimeSlotService;
import com.boxfishedu.workorder.service.studentrelated.RandomSlotFilterService;
import com.boxfishedu.workorder.servicex.bean.CourseView;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.MonthTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.boxfishedu.workorder.web.view.base.DateRange;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.MyCourseView;
import com.google.common.collect.Lists;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
public class AvaliableTimeForChangeTimeServiceXV {

    @Autowired
    private TimeLimitPolicy timeLimitPolicy;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private RandomSlotFilterService randomSlotFilterService;

    @Autowired
    private ServeService serveService;


    @Autowired
    private RedisMapService redisMapService;


    @Autowired
    private BaseTimeSlotJpaRepository baseTimeSlotJpaRepository;

    /**
     * 选时间生效天数,默认为第二天生效
     */
    @Value("${choiceTime.consumerStartDay:2}")
    private Integer consumerStartDay;
    private final static Integer daysOfWeek = 7;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *获取可以修改鱼卡的时间片列表
     */
    public JsonResultModel getTimeAvailable(Long workOrderId) throws CloneNotSupportedException {

        //获取鱼卡信息
        WorkOrder workOrder = validateAndGetWorkOrder(workOrderId);


        boolean afterTomo =  afterTomoDate(workOrder);
        if(!afterTomo){
            throw  new BusinessException("请提前48小时修改上课时间");
        }

        //首次鱼卡时间
        Date beginDate = serveService.getFirstTimeOfService(workOrder);

        AvaliableTimeParam avaliableTimeParam = new  AvaliableTimeParam();
        avaliableTimeParam.setComboType(workOrder.getService().getComboType());
        avaliableTimeParam.setTutorType(workOrder.getService().getTutorType());

        /**
         * 获取该鱼卡所在订单有效周期
         */
        Integer comboCycle = workOrder.getService().getComboCycle();

        if(null==comboCycle  || comboCycle<1 ){
            throw new BusinessException("该鱼卡不允许更改时间");
        }

        List<MyCourseView>  myCourseViews =   courseScheduleService.findMyClasses(workOrder.getStudentId());// 该学生以后的课程

        // 判断是免费还是正常购买
        Integer days =  comboCycle* daysOfWeek;

        //获取截至日期 (T+2原则  下单之后选时间最早后台)
        Date endDate  = DateUtil.addMinutes( DateUtil.date2SimpleDate(beginDate),60*24*(days-1)  );

        // 获取时间区间
        DateRange dateRange = getEnableDateRange(endDate);

        // TODO
        Set<String> classDateTimeSlotsSet = courseScheduleService.findByStudentIdAndAfterDate(workOrder.getStudentId());


        List<DayTimeSlots> dayTimeSlotsList = dateRange.forEach(

                // 过滤掉时间片
                (localDateTime, dayTimeSlot) -> {
                    dayTimeSlot.setDailyScheduleTime(dayTimeSlot.getDailyScheduleTime().stream()
                            .filter(t -> !classDateTimeSlotsSet.contains(String.join(" ", dayTimeSlot.getDay(), t.getSlotId().toString())))
                            .collect(Collectors.toList()));
                    return CollectionUtils.isEmpty(dayTimeSlot.getDailyScheduleTime()) ? null : dayTimeSlot;
                },
                // 根据日期获取到对应的DayTimeSlots
                (d) -> {
                    int teachingType = BaseTimeSlots.TEACHING_TYPE_CN;
                    if(StringUtils.equals(avaliableTimeParam.getTutorType(), TutorType.FRN.name())) {
                        teachingType = BaseTimeSlots.TEACHING_TYPE_FRN;
                    }


                    List<BaseTimeSlots> timeSlotsList = redisMapService.getMap( teachingType+""+BaseTimeSlots.CLIENT_TYPE_STU, DateUtil.localDate2SimpleString(d)) ;
                    if(CollectionUtils.isEmpty(timeSlotsList)){
                        timeSlotsList = baseTimeSlotJpaRepository.findByClassDateAndTeachingTypeAndClientType( DateUtil.convertToDate(d.toLocalDate()), teachingType, BaseTimeSlots.CLIENT_TYPE_STU);
                        redisMapService.setMap ( teachingType+""+BaseTimeSlots.CLIENT_TYPE_STU, DateUtil.localDate2SimpleString(d),timeSlotsList); ;
                    }
                    return createDayTimeSlots(d, timeSlotsList);
                });


//        List<DayTimeSlots> dayTimeSlotsList = dateRange.forEach(
//                dayTimeSlots, (localDateTime, d) -> {
//                                                        DayTimeSlots clone = (DayTimeSlots) d.clone();
//                                                        clone.setDay(DateUtil.formatLocalDate(localDateTime));
//                                                        //获取时间片范围内的数据
//                                                        DayTimeSlots result = randomSlotFilterService.removeSlotsNotInRange(clone,avaliableTimeParam);
//                                                        if(null == result){
//                                                            clone.setDailyScheduleTime(clone.getDailyScheduleTime().stream()
//                                                                    .filter(t -> !classDateTimeSlotsSet.contains(String.join(" ", clone.getDay(), t.getSlotId().toString())))
//                                                                    .collect(Collectors.toList()));
//                                                            return clone;
//                                                        }
//
//                                                        result.setDailyScheduleTime(result.getDailyScheduleTime().stream()
//                                                                .filter(t -> !classDateTimeSlotsSet.contains(String.join(" ", clone.getDay(), t.getSlotId().toString())))
//                                                                .collect(Collectors.toList()));
//                                                        if(result.getDay().equals(DateUtil.date2SimpleString(DateUtil.localDate2Date(dateRange.getFrom())  ))){
//
//                                                            result.setDailyScheduleTime(result.getDailyScheduleTime().stream().filter(
//                                                                    t ->   validateFirstDate(clone.getDay(),t.getStartTime(),DateUtil.localDate2Date(dateRange.getFrom())
//
//                                                            )).collect(Collectors.toList()));
//                                                        }
//
//                                                            return result;
//                                                     }
//        );

        List<DayTimeSlots> lastDayTimeSlots = Lists.newArrayList();

        if(!CollectionUtils.isEmpty(dayTimeSlotsList)){
            for(DayTimeSlots dts :dayTimeSlotsList){
                if(null!=dts){
                    if(CollectionUtils.isEmpty(myCourseViews)){
                        if(! CollectionUtils.isEmpty( dts.getDailyScheduleTime() )){
                            lastDayTimeSlots.add(dts);
                        }

                    }else {
                        boolean flag =true;
                        for(MyCourseView  mcv: myCourseViews){

                            if(dts.getDay().equals( mcv.getClassDate() )){
                                flag = false;
                                break;
                            }
                        }
                        if(flag){
                            if(! CollectionUtils.isEmpty( dts.getDailyScheduleTime() )){
                                lastDayTimeSlots.add(dts);
                            }
                        }
                    }
                }

            }
        }

        //return   JsonResultModel.newJsonResultModel(new MonthTimeSlots(dayTimeSlotsList).getData());
        return   JsonResultModel.newJsonResultModel(new MonthTimeSlots(lastDayTimeSlots).getData());
    }


    private DayTimeSlots createDayTimeSlots(LocalDateTime date, List<BaseTimeSlots> timeSlotsList) {
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
    private  boolean validateFirstDate(String day ,String startTime,Date from){
        Date startDate  = DateUtil.String2Date(day+" "+startTime);
        return startDate.after(from);
    }

    /**
     * 开始时间从后天开始
     * @param workOrder
     * @return
     */
    private boolean afterTomoDate(WorkOrder workOrder){
        Date end  = DateUtil.addMinutes(new Date(),60*24*2);
        if(workOrder.getStartTime()  .after(end)){
            return true;
        }
        return false;
    }

    /**
     * 获取可选的时间区间
     *
     * @return
     */
    private DateRange getEnableDateRange(Date endDate) {
        Date date  = new Date();// DateUtil.date2SimpleDate(new Date());
        int days = DateUtil.getBetweenDays(date,endDate);
        if(days<2){
            throw new BusinessException("该鱼卡更改时间超出修改范围");
        }
        int afterDays = consumerStartDay;
        LocalDateTime startDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        if(afterDays > 0) {
            startDate = startDate.plusDays(afterDays);

        }
        return new DateRange(startDate, days);
    }

    /**
     * 获取鱼卡信息
     * @param workOrderId
     * @return
     */
    private WorkOrder validateAndGetWorkOrder(Long workOrderId) {
        WorkOrder workOrder;
        try {
            workOrder = workOrderService.findOne(workOrderId);
            if (null == workOrder) {
                throw new BusinessException("鱼卡在服务端无对应的服务");
            }
        } catch (Exception ex) {
            throw new BusinessException("传入鱼卡的id在服务端无对应服务");
        }
        return workOrder;
    }








}
