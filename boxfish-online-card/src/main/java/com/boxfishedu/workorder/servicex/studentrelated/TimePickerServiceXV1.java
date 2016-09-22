package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.*;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/3/31.
 */
@Component
public class TimePickerServiceXV1 {
    @Autowired
    private ServeService serveService;
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private WorkOrderLogService workOrderLogService;
    @Autowired
    private CourseScheduleService courseScheduleService;
    @Autowired
    private TimePickerService timePickerService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private RecommandedCourseService recommandedCourseService;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;
    /**
     * 免费体验的天数
     */
    @Value("${choiceTime.freeExperienceDay:7}")
    private Integer freeExperienceDay;
    /**
     * 选时间生效天数,默认为第二天生效
     */
    @Value("${choiceTime.consumerStartDay:1}")
    private Integer consumerStartDay;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 学生选择时间
     * @param timeSlotParam
     * @return
     * @throws BoxfishException
     */
    public JsonResultModel ensureCourseTimes(TimeSlotParam timeSlotParam) throws BoxfishException {
        logger.info("客户端发起选课请求;参数:[{}]", JacksonUtil.toJSon(timeSlotParam));

        //根据订单id,type获取对应的服务
        List<Service> serviceList = ensureConvertOver(timeSlotParam);

        // 验证service权限
        serviceList.forEach(s -> s.authentication(timeSlotParam.getStudentId()));

        // 获取选课策略,每周选几次,持续几周
        WeekStrategy weekStrategy = getWeekStrategy(timeSlotParam, serviceList);
        // 验证参数
        validateTimeSlotParam(timeSlotParam, weekStrategy, serviceList);
        // unique验证
        for(Service service : serviceList) {
            Set<String> classDateTimeslotsSet = courseScheduleService.findByStudentIdAndAfterDate(service.getStudentId());
            checkUniqueCourseSchedules(classDateTimeslotsSet, timeSlotParam.getSelectedTimes());
        }

        // 批量生成工单 TODO 生成工单
        List<WorkOrder> workOrderList = batchInitWorkorders(timeSlotParam, weekStrategy, serviceList);

        // 获取课程推荐
        Map<Integer, RecommandCourseView> recommandCourses = getRecommandCourses(workOrderList, timeSlotParam);

        // 批量保存鱼卡与课表
        List<CourseSchedule> courseSchedules = workOrderService.persistCardInfos(serviceList, workOrderList, recommandCourses);

        // 保存日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrderList);

        // 分配老师
        timePickerService.getRecommandTeachers(serviceList.get(0), courseSchedules);

        // 通知其他模块
        notifyOtherModules(workOrderList, serviceList.get(0));

        logger.info("学生[{}]选课结束", serviceList.get(0).getStudentId());
        return JsonResultModel.newJsonResultModel();
    }

    private List<WorkOrder> batchInitWorkorders(TimeSlotParam timeSlotParam, WeekStrategy weekStrategy, List<Service> serviceList) {
        // 中教优先于外教
        serviceList.sort((c1, c2) -> Objects.equals(c1.getTutorType(), TutorType.CN.name()) ? 1 : 0);
        if(weekStrategy instanceof TemplateWeekStrategy) {
            return batchInitTemplateWorkOrders(timeSlotParam, weekStrategy, serviceList);
        } else {
            return batchInitUserDefinedWorkOrders(timeSlotParam, serviceList);
        }
    }

    private void checkUniqueCourseSchedules(Set<String> classDateTimeSlotsList, List<SelectedTime> selectedTimes) {
        for(SelectedTime selectedTime : selectedTimes) {
            checkUniqueCourseSchedule(
                    classDateTimeSlotsList,
                    selectedTime,
                    () -> {
                        TimeSlots timeSlot = teacherStudentRequester.getTimeSlot(selectedTime.getTimeSlotId());
                        return String.join(" ", selectedTime.getSelectedDate(), timeSlot.getStartTime())
                                + "已经安排了课程,请重新选择!";
                    });
        }
    }

    private void checkUniqueCourseSchedule(
            Set<String> classDateTimeSlotsList, SelectedTime selectedTime, Supplier<String> exceptionProducer) {
        if(classDateTimeSlotsList.contains(
                String.join(" ", selectedTime.getSelectedDate(),
                        selectedTime.getTimeSlotId().toString()))) {
            throw new BusinessException(exceptionProducer.get());
        }
    }

    private void validateTimeSlotParam(TimeSlotParam timeSlotParam, WeekStrategy weekStrategy, List<Service> services) {
        List<SelectedTime> selectedTimes = timeSlotParam.getSelectedTimes();

        if((weekStrategy instanceof TemplateWeekStrategy) && weekStrategy.getNumPerWeek() != selectedTimes.size()) {
            throw new BusinessException("选择的上课次数不符合规范");
        } else if(weekStrategy instanceof UserDefinedWeekStrategy) {
            Integer count = services.stream().collect(Collectors.summingInt(Service::getAmount));
            if(!Objects.equals(count, selectedTimes.size())) {
                throw new BusinessException("选择的上课次数不符合规范");
            }
        }

        // 重复选择时间
        Set<String> selectTimesSet = new HashSet<>();
        for(SelectedTime selectedTime : selectedTimes) {
            if(!selectTimesSet.add(selectedTime.getSelectedDate() + "-" + selectedTime.getTimeSlotId())) {
                TimeSlots timeSlot = teacherStudentRequester.getTimeSlot(selectedTime.getTimeSlotId());
                throw new BusinessException("选择有重复的时间"
                        + selectedTime.getSelectedDate() + " " + timeSlot.getStartTime());
            }
        }

        for(Service service : services) {
            if (service.getCoursesSelected() == 1) {
                throw new BusinessException("该订单已经完成选课,请勿重复选课");
            }
        }
    }

    private List<Service> ensureConvertOver(TimeSlotParam timeSlotParam) {
        return ensureConvertOver(timeSlotParam, 0);
    }

    //TODO:从师生运营组获取推荐课程
    private Map<Integer, RecommandCourseView> getRecommandCourses(List<WorkOrder> workOrders, TimeSlotParam timeSlotParam) {
        // 如果是overall的8次或者16次课程,直接调用批量推荐
        if(Objects.equals(timeSlotParam.getComboType(), ComboTypeToRoleId.OVERALL.name()) && (workOrders.size() % 8 == 0)) {
            return getOverAllBatchRecommand(workOrders, timeSlotParam.getStudentId());
        }

        Map<Integer, RecommandCourseView> courseViewMap = Maps.newHashMap();
        for (WorkOrder workOrder : workOrders) {
            logger.debug("鱼卡序号{}",workOrder.getSeqNum());
            Integer index=recommandedCourseService.getCourseIndex(workOrder);
            // 判断tutorType 是中教外教还是中外教,对应的推课
            TutorType tutorType = TutorType.resolve(workOrder.getService().getTutorType());
            RecommandCourseView recommandCourseView = null;
            // 不同类型的套餐对应不同类型的课程推荐
            if(Objects.equals(tutorType, TutorType.MIXED)) {
                recommandCourseView=recommandCourseRequester.getRecommandCourse(workOrder,index);
            } else if(Objects.equals(tutorType, TutorType.FRN) || Objects.equals(tutorType, TutorType.CN)) {
                recommandCourseView = recommandCourseRequester.getRecomendCourse(workOrder, tutorType);
            } else {

            }

            if(!Objects.isNull(recommandCourseView)) {
                workOrder.initCourseInfo(recommandCourseView);
                workOrder.setSkuId((long) CourseType2TeachingTypeService.courseType2TeachingType2(
                        recommandCourseView.getCourseType(),TutorType.resolve(workOrder.getService().getTutorType())));
                courseViewMap.put(workOrder.getSeqNum(), recommandCourseView);
            }
//            courseViewMap.put(workOrder.getSeqNum(), courseView);
        }
        return courseViewMap;
    }

    public void notifyOtherModules(List<WorkOrder> workOrders, Service service) {
        //通知上课中心
//        notifyCourseOnline(workOrders);
        //通知订单中心修改状态为已选课30

        serveService.notifyOrderUpdateStatus(service.getOrderId(), ConstantUtil.WORKORDER_SELECTED);
    }

    /**
     * 用户自定义选时间初始化工单
     * @param timeSlotParams
     * @param services
     * @return
     */
    private List<WorkOrder> batchInitUserDefinedWorkOrders(TimeSlotParam timeSlotParams, List<Service> services) {
        Queue<ServiceChoice> serviceQueue = createServiceChoice(services);
        final int[] index = {1};
        return timeSlotParams.getSelectedTimes().stream().map( timeSlotParam -> {
            Service service = services.get(choice(serviceQueue));
            WorkOrder workOrder = initWorkOrder(service, index[0]++, timeSlotParam.getTimeSlotId());
            TimeSlots timeSlots = getTimeSlotById(timeSlotParam.getTimeSlotId());
            String startTimeString = timeSlotParam.getSelectedDate() + " " + timeSlots.getStartTime();
            String endTimeString = timeSlotParam.getSelectedDate() + " " + timeSlots.getEndTime();
            workOrder.setStartTime(DateUtil.String2Date(startTimeString));
            workOrder.setEndTime(DateUtil.String2Date(endTimeString));
            return workOrder;
        }).collect(Collectors.toList());
    }

    /**
     * 模板选时间初始化工单
     * @param timeSlotParam
     * @param weekStrategy
     * @param services
     * @return
     * @throws BoxfishException
     */
    private List<WorkOrder> batchInitTemplateWorkOrders(TimeSlotParam timeSlotParam, WeekStrategy weekStrategy, List<Service> services) throws BoxfishException {
        List<WorkOrder> workOrders = new ArrayList<>();
        int numPerWeek = weekStrategy.getNumPerWeek();
        int loopOfWeek = weekStrategy.getLoopOfWeek();
        Queue<ServiceChoice> serviceQueue = createServiceChoice(services);
        for (int i = 0; i < loopOfWeek; i++) {
            for (int j = 0; j < numPerWeek; j++) {
                int index = (j + 1) + i * numPerWeek;
                if(index > weekStrategy.getCount()) {
                    break;
                }
                Service service = services.get(choice(serviceQueue));
                List<SelectedTime> selectedTimes = timeSlotParam.getSelectedTimes();
                WorkOrder workOrder = initWorkOrder(service, index, timeSlotParam.getSelectedTimes().get(j).getTimeSlotId());
                TimeSlots timeSlots = getTimeSlotById(timeSlotParam.getSelectedTimes().get(j).getTimeSlotId());
                String startTimeString = selectedTimes.get(j).getSelectedDate() + " " + timeSlots.getStartTime();
                String endTimeString = selectedTimes.get(j).getSelectedDate() + " " + timeSlots.getEndTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date startTime = sdf.parse(startTimeString);
                    Date endTime = sdf.parse(endTimeString);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(startTime);
                    //service的validate day来自sku的validate day
                    calendar.add(Calendar.DATE, i * Calendar.DAY_OF_WEEK);
                    workOrder.setStartTime(calendar.getTime());
                    calendar.setTime(endTime);
                    calendar.add(Calendar.DATE, i * Calendar.DAY_OF_WEEK);
                    workOrder.setEndTime(calendar.getTime());
                } catch (Exception ex) {
                    throw new BusinessException("生成鱼卡时日期选择出现异常");
                }
                workOrders.add(workOrder);
            }
        }
        return workOrders;
    }

    private WorkOrder initWorkOrder(Service service, int index, Integer slotId) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setStatus(FishCardStatusEnum.CREATED.getCode());
        workOrder.setService(service);
        workOrder.setTeacherId(0l);
        workOrder.setOrderId(service.getOrderId());
        workOrder.setStudentId(service.getStudentId());
        workOrder.setStudentName(service.getStudentName());
        workOrder.setIsCourseOver((short) 0);
        workOrder.setSlotId(slotId);
        workOrder.setSeqNum(index);
        workOrder.setCreateTime(new Date());
        workOrder.setOrderCode(service.getOrderCode());
        workOrder.setIsFreeze(0);
        // skuIdExtra 字段
        workOrder.setSkuIdExtra(service.getSkuId().intValue());
        workOrder.setOrderChannel(service.getOrderChannel());
        return workOrder;
    }

    private int choice(Queue<ServiceChoice> queue) {
        ServiceChoice serviceChoice;
        while(true) {
            serviceChoice = queue.poll();
            if(Objects.isNull(serviceChoice)) {
                return -1;
            }
            if(serviceChoice.hasNext()) {
                serviceChoice.decrement();
                queue.add(serviceChoice);
                break;
            }
        }
        return serviceChoice.index;
    }

    private Queue<ServiceChoice> createServiceChoice(List<Service> services) {
        Queue<ServiceChoice> serviceQueue = new LinkedList<>();
        for(int i = 0; i < services.size(); i++) {
            serviceQueue.add(new ServiceChoice(i, services.get(i).getAmount()));
        }
        return serviceQueue;
    }

    class ServiceChoice {
        int index;
        int amount;

        public ServiceChoice(int index, int amount) {
            this.index = index;
            this.amount = amount;
        }

        public boolean hasNext() {
            return amount > 0;
        }

        public void decrement() {
            this.amount --;
        }
    }

    private List<Service> ensureConvertOver(TimeSlotParam timeSlotParam, int pivot) {
        pivot++;
        // TODO 需要更改获取服务的方式
        List<Service> services = serveService.findByOrderIdAndProductType(
                timeSlotParam.getOrderId(), timeSlotParam.getProductType());
        if (CollectionUtils.isEmpty(services)) {
            if (pivot > 2) {
                logger.error("重试两次后发现仍然不存在对应的服务,直接返回给前端,当前pivot[{}]",pivot);
                throw new BusinessException("无对应服务,请重试");
            } else {
                //简单粗暴的方式先解决
                try {
                    logger.debug("不存在对应的服务,当前为第[{}]次等待获取service",pivot);
                    Thread.sleep(3000);
                } catch (Exception ex) {
                    logger.error("线程休眠失败");
                    throw new BusinessException("选课失败,请重试");
                }
                return ensureConvertOver(timeSlotParam,  pivot);
            }
        }
        return services;
    }


    public TimeSlots getTimeSlotById(Integer id) throws BusinessException {
        return teacherStudentRequester.getTimeSlot(id);
    }

    private WeekStrategy getWeekStrategy(TimeSlotParam timeSlotParam, List<Service> services) {
        // 默认是template
        if(Objects.isNull(timeSlotParam.getSelectMode()) ||
                Objects.equals(timeSlotParam.getSelectMode(), WeekStrategy.TEMPLATE)) {
            return createTemplateWeekStrategy(timeSlotParam, services);
        } else {
            return UserDefinedWeekStrategy.DEFAULT;
        }

    }


    /**
     * 7+1 套餐批量推荐课程,16次7+1也可以批量进行2次推荐
     * @param workOrders
     * @param studentId
     * @return
     */
    private Map<Integer, RecommandCourseView> getOverAllBatchRecommand(List<WorkOrder> workOrders, Long studentId) {
        Map<Integer, RecommandCourseView> resultMap = new HashMap<>();
        int recommendIndex = 0;
        for(int i = 0; i < workOrders.size() / 8; i++) {
            List<RecommandCourseView> recommendCourseViews = recommandCourseRequester.getBatchRecommandCourse(studentId);
            for(RecommandCourseView recommandCourseView : recommendCourseViews) {
                resultMap.put(++recommendIndex, recommandCourseView);
            }
        }

        for (int i = 0; i < workOrders.size(); i++) {
            WorkOrder workOrder = workOrders.get(i);
            logger.debug("鱼卡序号{}",workOrder.getSeqNum());
//            Integer index=recommandedCourseService.getCourseIndex(workOrder);
            RecommandCourseView recommandCourseView = resultMap.get(workOrder.getSeqNum());
                workOrder.initCourseInfo(recommandCourseView);
                workOrder.setSkuId((long) CourseType2TeachingTypeService.courseType2TeachingType2(
                        recommandCourseView.getCourseType(),TutorType.resolve(workOrder.getService().getTutorType())));
        }
        return resultMap;
    }


    private TemplateWeekStrategy createTemplateWeekStrategy(TimeSlotParam timeSlotParam, List<Service> services) {
        int count = services.stream().collect(Collectors.summingInt(Service::getAmount));
        // 兑换默认为1周两次
        if(Objects.equals(timeSlotParam.getComboTypeEnum(), ComboTypeToRoleId.EXCHANGE)) {
            int loopOfWeek = (count + TemplateWeekStrategy.DEFAULT_EXCHANGE_NUMPERWEEK - 1)
                    / TemplateWeekStrategy.DEFAULT_EXCHANGE_NUMPERWEEK;
            int numPerWeek = count == 1 ? 1: TemplateWeekStrategy.DEFAULT_EXCHANGE_NUMPERWEEK;
            return new TemplateWeekStrategy(loopOfWeek, numPerWeek, count);
        }
        int loopOfWeek = services.stream().collect(Collectors.summingInt(Service::getComboCycle));
        int per = count / loopOfWeek == 0 ? 1 : count / loopOfWeek;
        logger.info("weekStrategy= loopOfWeek:[{}],per:[{}]", loopOfWeek, per);
        return new TemplateWeekStrategy((count + per -1) / per, per, count);
    }


    class TemplateWeekStrategy extends WeekStrategy {
        public final static int DEFAULT_EXCHANGE_NUMPERWEEK = 2;
        int loopOfWeek;
        int numPerWeek;
        int count;

        public TemplateWeekStrategy(int loopOfWeek, int numPerWeek, int count) {
            this.loopOfWeek = loopOfWeek;
            this.numPerWeek = numPerWeek;
            this.count = count;
        }

        @Override
        public int getLoopOfWeek() {
            return loopOfWeek;
        }

        @Override
        public int getNumPerWeek() {
            return numPerWeek;
        }

        @Override
        public int getCount() {
            return count;
        }
    }

    static class UserDefinedWeekStrategy extends WeekStrategy {
        public final static UserDefinedWeekStrategy DEFAULT = new UserDefinedWeekStrategy();
    }

    static class WeekStrategy {
        public final static Integer TEMPLATE = 0;

        public final static Integer USERDEFINED = 1;

        public int getLoopOfWeek() {
            return 0;
        }

        public int getNumPerWeek() {
            return 0;
        }

        public int getCount() {
            return 0;
        }
    }
}
