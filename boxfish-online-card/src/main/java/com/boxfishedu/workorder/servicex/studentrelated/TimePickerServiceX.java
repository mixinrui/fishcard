package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.bean.StudentCourseSchedule;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.studentrelated.recommend.RecommendHandlerHelper;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectModeFactory;
import com.boxfishedu.workorder.servicex.studentrelated.validator.StudentTimePickerValidatorSupport;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.form.DateRangeForm;
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

import java.time.LocalDateTime;
import java.util.*;

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
    private ServiceSDK serviceSDK;
    @Autowired
    private TimePickerService timePickerService;
    @Autowired
    RestTemplate restTemplate;

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

    @Autowired
    private RecommendHandlerHelper recommendHandlerHelper;

    @Autowired
    private SelectModeFactory selectModeFactory;

    @Autowired
    private StudentTimePickerValidatorSupport studentTimePickerValidatorSupport;

    @Autowired
    private DataCollectorService dataCollectorService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public JsonResultModel ensureCourseTimes(TimeSlotParam timeSlotParam) throws BoxfishException {
        logger.info("客户端发起选课请求;参数:[{}]", JacksonUtil.toJSon(timeSlotParam));

        //根据订单id,type获取对应的服务
        Service service = ensureConvertOver(timeSlotParam);

        // 获取选课策略,每周选几次,持续几周
        SelectMode selectMode = selectModeFactory.createSelectMode(timeSlotParam);

        // 选时间参数验证
        studentTimePickerValidatorSupport.prepareValidate(timeSlotParam, selectMode, Collections.singletonList(service));

        logger.info("选课开始,service的id为:[{}]", service.getId());

        List<WorkOrder> workOrders = batchInitWorkorders(timeSlotParam, selectMode, service);

        Set<String> classDateTimeslotsSet = courseScheduleService.findByStudentIdAndAfterDate(timeSlotParam.getStudentId());

        // 对初始化的workOrderList进行验证
        studentTimePickerValidatorSupport.postValidate(Collections.singletonList(service),workOrders, classDateTimeslotsSet);

        //TODO:推荐课的接口目前为单词请求
        Map<Integer, RecommandCourseView> recommandCoursesMap = recommendHandlerHelper.recommendCourses(workOrders, timeSlotParam);

        workOrderService.batchSaveCoursesIntoCard(workOrders,recommandCoursesMap);

        //入库,workorder和coursechedule的插入放入一个事务中,保证数据的一致性
        List<CourseSchedule> courseSchedules=workOrderService.persistCardInfos(
                service,workOrders,recommandCoursesMap);

        dataCollectorService.updateBothChnAndFnItemAsync(service.getStudentId());

        //TODO:等测
        workOrderLogService.batchSaveWorkOrderLogs(workOrders);

        //获取订单内所有的教师,key为workorder的starttime的time值
        timePickerService.getRecommandTeachers(service,courseSchedules);

        notifyOtherModules(workOrders, service);

        logger.info("学生[{}]选课结束", service.getStudentId());
        return JsonResultModel.newJsonResultModel();
    }


    private Service ensureConvertOver(TimeSlotParam timeSlotParam) {
        int pivot = 0;
        return ensureConvertOver(timeSlotParam, pivot);
    }


    public void notifyOtherModules(List<WorkOrder> workOrders, Service service) {
        //通知上课中心
//        notifyCourseOnline(workOrders);
        //通知订单中心修改状态为已选课30
        serveService.notifyOrderUpdateStatus(service.getOrderId(), ConstantUtil.WORKORDER_SELECTED);
    }


    private List<WorkOrder> batchInitWorkorders(TimeSlotParam timeSlotParam, SelectMode selectMode,  Service service) throws BoxfishException {
        return selectMode.initWorkOrderList(timeSlotParam, selectMode, Collections.singletonList(service));
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
    public JsonResultModel getByStudentIdAndDateRange(Long studentId, DateRangeForm dateRangeForm, Locale locale) {
        List<CourseSchedule> courseSchedules =
                courseScheduleService.findByStudentIdAndClassDateBetween(studentId, dateRangeForm);
        return JsonResultModel.newJsonResultModel(adapterCourseScheduleList(courseSchedules, locale));
    }

    public JsonResultModel getFinishCourseSchedulePage(Long userId, Pageable pageable, Locale locale) {
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findFinishCourseSchedulePage(userId, pageable);
        return JsonResultModel.newJsonResultModel(wrapCourseSchedulePage(pageable, courseSchedulePage, locale));
    }

    public JsonResultModel getUnFinishCourseSchedulePage(Long userId, Pageable pageable,  Locale locale) {
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findUnfinishCourseSchedulePage(userId, pageable);
        return JsonResultModel.newJsonResultModel(wrapCourseSchedulePage(pageable, courseSchedulePage, locale));
    }

    private Page<StudentCourseSchedule> wrapCourseSchedulePage(
            Pageable pageable, Page<CourseSchedule> courseSchedulePage, Locale locale) {
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
            studentCourseSchedule.setCourseView(serviceSDK.getCourseInfo(courseSchedule.getId(), locale));
            result.add(studentCourseSchedule);
        });
        return new PageImpl<>(result, pageable, courseSchedulePage.getTotalElements());
    }

    private StudentCourseSchedule createStudentCourseSchedule(CourseSchedule courseSchedule, Locale locale) {
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
            studentCourseSchedule.setCourseView(serviceSDK.getCourseInfo(courseSchedule.getId(), locale));
        }
        return studentCourseSchedule;
    }

    public Object getCourseSchedulePage(Long studentId, Pageable pageable, Locale locale) {
        Page<CourseSchedule> page = courseScheduleService.findByStudentId(studentId, pageable);
        List<Map<String, Object>> result = adapterCourseScheduleList(page.getContent(), locale);
        HashMap<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("data", result);
        resultMap.put("returnCode", HttpStatus.SC_OK);
        resultMap.put("totalElements", page.getTotalElements());
        resultMap.put("number", page.getNumber());
        resultMap.put("totalPages", page.getTotalPages());
        resultMap.put("size", page.getSize());
        return resultMap;
    }

    private List<Map<String, Object>> adapterCourseScheduleList(List<CourseSchedule> courseScheduleList, Locale locale) {
        Map<String, List<StudentCourseSchedule>> courseScheduleMap = Maps.newLinkedHashMap();
        courseScheduleList.forEach(courseSchedule -> {
            String date = DateUtil.simpleDate2String(courseSchedule.getClassDate());
            List<StudentCourseSchedule> studentCourseScheduleList = courseScheduleMap.get(date);
            if (studentCourseScheduleList == null) {
                studentCourseScheduleList = Lists.newArrayList();
                courseScheduleMap.put(date, studentCourseScheduleList);
            }
            studentCourseScheduleList.add(createStudentCourseSchedule(courseSchedule, locale));
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
