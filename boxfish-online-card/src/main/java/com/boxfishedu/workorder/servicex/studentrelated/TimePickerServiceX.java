package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
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
import com.boxfishedu.workorder.servicex.bean.StudentCourseSchedule;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.course.CourseView;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.form.DateRangeForm;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static com.boxfishedu.workorder.common.util.DateUtil.*;

/**
 * Created by hucl on 16/3/31.
 */
@Component
public class TimePickerServiceX {
    @Autowired
    private ServeService serveService;
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private WorkOrderLogService workOrderLogService;
    @Autowired
    private CourseScheduleService courseScheduleService;
    @Autowired
    private LogPoolManager logPoolManager;
    @Autowired
    private ServiceSDK serviceSDK;
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

    public JsonResultModel ensureCourseTimes(TimeSlotParam timeSlotParam) throws BoxfishException {
        logger.info("客户端发起选课请求;参数:[{}]", JacksonUtil.toJSon(timeSlotParam));

        //根据订单id,type获取对应的服务
        Service service = ensureConvertOver(timeSlotParam);

        // 操作权限认证,防止非法的访问
        service.authentication(timeSlotParam.getStudentId());

        validateTimeSlotParam(timeSlotParam,service);

        logger.info("选课开始,service的id为:[{}]", service.getId());

        List<WorkOrder> workOrders = batchInitWorkorders(timeSlotParam, service);

        // 返回学生当前选择的课程日期和时间片, unique课程验证
        Set<String> classDateTimeslotsSet = courseScheduleService.findByStudentIdAndAfterDate(timeSlotParam.getStudentId());
        checkUniqueCourseSchedules(classDateTimeslotsSet, workOrders);

        //TODO:推荐课的接口目前为单词请求
        Map<Integer, RecommandCourseView> recommandCoursesMap = getRecommandCourses(workOrders, timeSlotParam);

        workOrderService.batchSaveCoursesIntoCard(workOrders,recommandCoursesMap);

        //入库,workorder和coursechedule的插入放入一个事务中,保证数据的一致性
        List<CourseSchedule> courseSchedules=workOrderService.persistCardInfos(
                service,workOrders,recommandCoursesMap);

        //TODO:等测
        workOrderLogService.batchSaveWorkOrderLogs(workOrders);

        //获取订单内所有的教师,key为workorder的starttime的time值
        timePickerService.getRecommandTeachers(service,courseSchedules);

        notifyOtherModules(workOrders, service);

        logger.info("学生[{}]选课结束", service.getStudentId());
        return JsonResultModel.newJsonResultModel();
    }


    private void checkUniqueCourseSchedules(Set<String> classDateTimeSlotsList, List<WorkOrder> workOrderList) {
        for(WorkOrder workOrder : workOrderList) {
            checkUniqueCourseSchedule(
                    classDateTimeSlotsList,
                    workOrder,
                    () -> String.join(" ", DateUtil.Date2String(workOrder.getStartTime()))
                            + "已经安排了课程,请重新选择!");
        }
    }

    private void checkUniqueCourseSchedule(
            Set<String> classDateTimeSlotsList, WorkOrder workOrder, Supplier<String> exceptionProducer) {
        if(classDateTimeSlotsList.contains(
                String.join(" ", DateUtil.simpleDate2String(workOrder.getStartTime()),
                        workOrder.getSlotId().toString()))) {
            throw new BusinessException(exceptionProducer.get());
        }
    }


    private void validateTimeSlotParam(TimeSlotParam timeSlotParam,Service service) {
        List<SelectedTime> selectedTimes = timeSlotParam.getSelectedTimes();
//        if (service.getComboCycle() != -1) {
//            if (selectedTimes.size() != (service.getAmount() >> 2)) {
//                throw new BusinessException("选择的上课次数不符合规范");
//            }
//        }
        if(serveService.getNumPerWeek(service) != selectedTimes.size()) {
            throw new BusinessException("选择的上课次数不符合规范");
        }
        if(service.getCoursesSelected()==1){
            throw new BusinessException("该订单已经完成选课,请勿重复选课");
        }

        Set<String> selectTimesSet = new HashSet<>();
        for(SelectedTime selectedTime : selectedTimes) {
            if(!selectTimesSet.add(selectedTime.getSelectedDate() + "-" + selectedTime.getTimeSlotId())) {
                TimeSlots timeSlot = teacherStudentRequester.getTimeSlot(selectedTime.getTimeSlotId());
                throw new BusinessException("选择有重复的时间"
                        + selectedTime.getSelectedDate() + " " + timeSlot.getStartTime());
            }
        }
    }

    private Service ensureConvertOver(TimeSlotParam timeSlotParam) {
        int pivot = 0;
        return ensureConvertOver(timeSlotParam, pivot);
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
            String comboType = workOrder.getService().getComboType();
            RecommandCourseView recommandCourseView = null;
            // 不同类型的套餐对应不同类型的课程推荐
            if(Objects.equals(comboType, ComboTypeToRoleId.OVERALL.name())) {
                recommandCourseView=recommandCourseRequester.getRecommandCourse(workOrder,index);
            } else if(Objects.equals(comboType, ComboTypeToRoleId.FOREIGN.name())) {
                recommandCourseView = recommandCourseRequester.getForeignRecomandCourse(workOrder);
            } else if(Objects.equals(comboType, ComboTypeToRoleId.EXCHANGE.name())) {
                // 兑换类型,需要判断对应service的tutorType类型
                recommandCourseView = recommandCourseRequester.getRecomendCourse(
                        workOrder, TutorType.resolve(workOrder.getService().getTutorType()));
            }

            if(!Objects.isNull(recommandCourseView)) {
                courseViewMap.put(workOrder.getSeqNum(), recommandCourseView);
            }
//            courseViewMap.put(workOrder.getSeqNum(), courseView);
        }
        return courseViewMap;
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
            workOrder.setSkuId(CourseType2TeachingTypeService.courseType2TeachingType2(
                    recommandCourseView.getCourseType(),TutorType.resolve(workOrder.getService().getTutorType())));
        }
        return resultMap;
    }

    public void notifyOtherModules(List<WorkOrder> workOrders, Service service) {
        //通知上课中心
//        notifyCourseOnline(workOrders);
        //通知订单中心修改状态为已选课30
        serveService.notifyOrderUpdateStatus(service.getOrderId(), ConstantUtil.WORKORDER_SELECTED);
    }

    public List<WorkOrder> getChineseWorkOrders(List<WorkOrder> workOrders) {
        List chineseWorkorderList = Lists.newArrayList();
        for (WorkOrder workOrder : workOrders) {
            if (ConstantUtil.TEACHER_TYPE_EXTRA_FOREIGNER != workOrder.getSkuIdExtra()) {
                chineseWorkorderList.add(workOrder);
            }
        }
        return chineseWorkorderList;
    }

    public List<WorkOrder> getForeignWorkOrders(List<WorkOrder> workOrders) {
        List foreignWorkorderList = Lists.newArrayList();
        for (WorkOrder workOrder : workOrders) {
            if (ConstantUtil.TEACHER_TYPE_EXTRA_FOREIGNER == workOrder.getSkuIdExtra()) {
                foreignWorkorderList.add(workOrder);
            }
        }
        return foreignWorkorderList;
    }

    private void notifyCourseOnline(List<WorkOrder> workOrders) {
        for (WorkOrder workOrder : workOrders) {
            if (null!=workOrder.getTeacherId()&&(0l!=workOrder.getTeacherId())) {
                logPoolManager.execute(new Thread(() -> {
                    serviceSDK.createGroup(workOrder);
                }));
            }
        }
    }

    private void saveTeachersIntoWorkOrders(List<WorkOrder> workOrders, Map<String, TeacherView> teacherViewMap) {
        for (WorkOrder workOrder : workOrders) {
            TeacherView teacherView = teacherViewMap.get(workOrder.getStartTime().getTime() + "");
            workOrder.setTeacherId(teacherView.getTeacherId());
            workOrder.setTeacherName(teacherView.getTeacherName());
        }
    }

    private Map<WorkOrder, CourseView> saveCoursesIntoWorkorders(List<WorkOrder> workOrders, List<CourseView> courseViews) {
        Map<WorkOrder, CourseView> workOrderCourseViewMap = new HashMap<>();
        for (int i = 0; i < workOrders.size(); i++) {
            workOrders.get(i).setCourseId(courseViews.get(i).getBookSectionId());
            workOrders.get(i).setCourseName(courseViews.get(i).getName());
            workOrders.get(i).setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
            workOrders.get(i).setCourseType(courseViews.get(i).getCourseType().get(0));
            workOrderCourseViewMap.put(workOrders.get(i), courseViews.get(i));
        }
        return workOrderCourseViewMap;
    }

    private void saveCoursesIntoWorkorders(List<WorkOrder> workOrders, Map<Integer, RecommandCourseView> courseViewMap) {
        for (WorkOrder workOrder : workOrders) {
            RecommandCourseView courseView = courseViewMap.get(workOrder.getSeqNum());
            workOrder.setCourseName(courseView.getCourseName());
            workOrder.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
            workOrder.setCourseType(courseView.getCourseType());
            workOrder.setCourseId(courseView.getCourseId());
        }
    }

    private List<WorkOrder> batchInitWorkorders(TimeSlotParam timeSlotParam, Service service) throws BoxfishException {
        List<WorkOrder> workOrders = new ArrayList<>();
        int numPerWeek = serveService.getNumPerWeek(service);
        int loopOfWeek = serveService.getLoopOfWeek(service);

        for (int i = 0; i < loopOfWeek; i++) {
            for (int j = 0; j < numPerWeek; j++) {
                int index = (j + 1) + i * numPerWeek;
                if(index > service.getAmount()) {
                    break;
                }
                List<SelectedTime> selectedTimes = timeSlotParam.getSelectedTimes();
                WorkOrder workOrder = new WorkOrder();
                workOrder.setStatus(FishCardStatusEnum.CREATED.getCode());
                workOrder.setService(service);
                workOrder.setTeacherId(0l);
                workOrder.setOrderId(service.getOrderId());
                workOrder.setStudentId(service.getStudentId());
                workOrder.setStudentName(service.getStudentName());
                workOrder.setIsCourseOver((short) 0);
                workOrder.setSlotId(timeSlotParam.getSelectedTimes().get(j).getTimeSlotId());
                workOrder.setSeqNum(index);
                workOrder.setCreateTime(new Date());
                workOrder.setOrderCode(service.getOrderCode());
                workOrder.setIsFreeze(0);
                // skuIdExtra 字段
                workOrder.setSkuIdExtra(service.getSkuId().intValue());
                workOrder.setOrderChannel(service.getOrderChannel());
                workOrder.setSendflagcc("1");// 师生互评 发送状态 默认1 为 未发送
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

    private Service ensureConvertOver(TimeSlotParam timeSlotParam, int pivot) {
        pivot++;
        Service service = serveService.findTop1ByOrderIdAndComboType(
                timeSlotParam.getOrderId(), timeSlotParam.getComboTypeEnum().name());
        if (null == service) {
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
                service=ensureConvertOver(timeSlotParam, pivot);
                return service;
            }
        }
        return service;
    }

    public TimeSlots getTimeSlotById(Integer id) throws BusinessException {
        return teacherStudentRequester.getTimeSlot(id);
    }

    /**
     * 获取学生所选的课列表
     * @param studentId
     * @param dateRangeForm
     * @return
     */
    public JsonResultModel getByStudentIdAndDateRange(Long studentId, DateRangeForm dateRangeForm) {
        List<CourseSchedule> courseSchedules =
                courseScheduleService.findByStudentIdAndClassDateBetween(studentId, dateRangeForm);
        return JsonResultModel.newJsonResultModel(adapterCourseScheduleList(courseSchedules));
    }

    public JsonResultModel getFinishCourseSchedulePage(Long userId, Pageable pageable) {
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findFinishCourseSchedulePage(userId, pageable);
        return JsonResultModel.newJsonResultModel(wrapCourseSchedulePage(pageable, courseSchedulePage));
    }

    public JsonResultModel getUnFinishCourseSchedulePage(Long userId, Pageable pageable) {
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findUnfinishCourseSchedulePage(userId, pageable);
        return JsonResultModel.newJsonResultModel(wrapCourseSchedulePage(pageable, courseSchedulePage));
    }

    private Page<StudentCourseSchedule> wrapCourseSchedulePage(Pageable pageable, Page<CourseSchedule> courseSchedulePage) {
        List<CourseSchedule> content = courseSchedulePage.getContent();
        List<StudentCourseSchedule> result = Lists.newArrayList();
        content.forEach(courseSchedule -> {
            StudentCourseSchedule studentCourseSchedule = new StudentCourseSchedule();
            studentCourseSchedule.setId(courseSchedule.getId());
            studentCourseSchedule.setCourseType(courseSchedule.getCourseType());
            studentCourseSchedule.setCourseId(courseSchedule.getCourseId());
            studentCourseSchedule.setIsFreeze(courseSchedule.getIsFreeze());
            TimeSlots timeSlot = teacherStudentRequester.getTimeSlot(courseSchedule.getTimeSlotId());
            if (timeSlot != null) {
                // 日期转换
                LocalDateTime time = merge(
                        convertLocalDate(courseSchedule.getClassDate()),
                        parseLocalTime(timeSlot.getStartTime()));
                studentCourseSchedule.setTime(formatLocalDateTime(time));
            }
            studentCourseSchedule.setCourseView(serviceSDK.getCourseInfo(courseSchedule.getId()));
            result.add(studentCourseSchedule);
        });
        return new PageImpl<>(result, pageable, courseSchedulePage.getTotalElements());
    }

    private StudentCourseSchedule createStudentCourseSchedule(CourseSchedule courseSchedule) {
        TimeSlots timeSlots = getTimeSlotById(courseSchedule.getTimeSlotId());
        StudentCourseSchedule studentCourseSchedule = new StudentCourseSchedule();
        studentCourseSchedule.setId(courseSchedule.getId());
        studentCourseSchedule.setCourseId(courseSchedule.getCourseId());
        studentCourseSchedule.setCourseType(courseSchedule.getCourseType());
        studentCourseSchedule.setTime(timeSlots.getStartTime());
        studentCourseSchedule.setWorkOrderId(courseSchedule.getWorkorderId());
        studentCourseSchedule.setStatus(courseSchedule.getStatus());
        studentCourseSchedule.setIsFreeze(courseSchedule.getIsFreeze());
        if (StringUtils.isNotEmpty(courseSchedule.getCourseId())) {
            studentCourseSchedule.setCourseView(serviceSDK.getCourseInfo(courseSchedule.getId()));
        }
        return studentCourseSchedule;
    }

    public Object getCourseSchedulePage(Long studentId, Pageable pageable) {
        Page<CourseSchedule> page = courseScheduleService.findByStudentId(studentId, pageable);
        List<Map<String, Object>> result = adapterCourseScheduleList(page.getContent());
        HashMap<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("data", result);
        resultMap.put("returnCode", HttpStatus.SC_OK);
        resultMap.put("totalElements", page.getTotalElements());
        resultMap.put("number", page.getNumber());
        resultMap.put("totalPages", page.getTotalPages());
        resultMap.put("size", page.getSize());
        return resultMap;
    }

    private List<Map<String, Object>> adapterCourseScheduleList(List<CourseSchedule> courseScheduleList) {
        Map<String, List<StudentCourseSchedule>> courseScheduleMap = Maps.newLinkedHashMap();
        courseScheduleList.forEach(courseSchedule -> {
            String date = DateUtil.simpleDate2String(courseSchedule.getClassDate());
            List<StudentCourseSchedule> studentCourseScheduleList = courseScheduleMap.get(date);
            if (studentCourseScheduleList == null) {
                studentCourseScheduleList = Lists.newArrayList();
                courseScheduleMap.put(date, studentCourseScheduleList);
            }
            studentCourseScheduleList.add(createStudentCourseSchedule(courseSchedule));
        });

        List<Map<String, Object>> result = Lists.newArrayList();
        courseScheduleMap.forEach((key, val) -> {
            Map<String, Object> beanMap = Maps.newHashMap();
            beanMap.put("day", key);
            beanMap.put("dailyScheduleTime", val);
            result.add(beanMap);
        });
        return result;
    }
}
