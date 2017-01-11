package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.CourseDifficultyEnum;
import com.boxfishedu.workorder.common.bean.PublicClassMessageEnum;
import com.boxfishedu.workorder.common.bean.PublicClassTimeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.PublicClassException;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentSchemaJpaRepository;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.StStudentApplyRecordsService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.studentrelated.recommend.RecommendHandlerHelper;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectModeFactory;
import com.boxfishedu.workorder.servicex.studentrelated.validator.StudentTimePickerValidatorSupport;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

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
    private StStudentApplyRecordsService stStudentApplyRecordsService;

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

    @Autowired
    private RecommendHandlerHelper recommendHandlerHelper;

    @Autowired
    private StudentTimePickerValidatorSupport studentTimePickerValidatorSupport;

    @Autowired
    private SelectModeFactory selectModeFactory;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private StStudentSchemaJpaRepository stStudentSchemaJpaRepository;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private PublicClassRoom publicClassRoom;

    private Cache publicClassCacheWithLevelAndDate;

    private Cache publicClassCacheWithId;

    @Autowired
    public void initCache(CacheManager cacheManager) {
        publicClassCacheWithLevelAndDate = cacheManager.getCache(
                CacheKeyConstant.PUBLIC_CLASS_ROOM_WITH_LEVELANDDATE);
        publicClassCacheWithId = cacheManager.getCache(
                CacheKeyConstant.PUBLIC_CLASS_ROOM_WITH_ID);
    }

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

        // 获取选课策略,每周选几次,持续几周
        SelectMode selectMode = selectModeFactory.createSelectMode(timeSlotParam);

        // 选时间参数验证
        studentTimePickerValidatorSupport.prepareValidate(timeSlotParam, selectMode, serviceList);

        // 批量生成工单 TODO 生成工单
        List<WorkOrder> workOrderList = batchInitWorkorders(timeSlotParam, selectMode, serviceList);

        Set<String> classDateTimeslotsSet = courseScheduleService.findByStudentIdAndAfterDate(timeSlotParam.getStudentId());

        studentTimePickerValidatorSupport.postValidate(serviceList, workOrderList, classDateTimeslotsSet);

        // 获取课程推荐
        Map<Integer, RecommandCourseView> recommandCourses = recommendHandlerHelper.recommendCourses(workOrderList, timeSlotParam);

        // 批量保存鱼卡与课表
        List<CourseSchedule> courseSchedules = workOrderService.persistCardInfos(serviceList, workOrderList, recommandCourses);

        dataCollectorService.updateBothChnAndFnItem(serviceList.get(0).getStudentId());

        // 保存日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrderList);

        // 分配老师
        timePickerService.getRecommandTeachers(serviceList.get(0), courseSchedules);

        // 通知其他模块
        notifyOtherModules(workOrderList, serviceList.get(0));

        logger.info("学生[{}]选课结束", serviceList.get(0).getStudentId());
        return JsonResultModel.newJsonResultModel();
    }



    public JsonResultModel ensureCourseTimesv2(TimeSlotParam timeSlotParam) throws BoxfishException {
        logger.info("ensureCourseTimesv2客户端发起选课请求;参数:[{}]", JacksonUtil.toJSon(timeSlotParam));

        //根据订单id,type获取对应的服务
        List<Service> serviceList = ensureConvertOver(timeSlotParam);

        // 获取选课策略,每周选几次,持续几周
        SelectMode selectMode = selectModeFactory.createSelectMode(timeSlotParam);

        // 选时间参数验证
        studentTimePickerValidatorSupport.prepareValidate(timeSlotParam, selectMode, serviceList);

        // 批量生成工单 TODO 生成工单
        List<WorkOrder> workOrderList = batchInitWorkorders(timeSlotParam, selectMode, serviceList);

        Set<String> classDateTimeslotsSet = courseScheduleService.findByStudentIdAndAfterDate(timeSlotParam.getStudentId());

        studentTimePickerValidatorSupport.postValidate(serviceList, workOrderList, classDateTimeslotsSet);

        // 获取课程推荐
        Map<Integer, RecommandCourseView> recommandCourses = recommendHandlerHelper.recommendCourses(workOrderList, timeSlotParam);

        // 批量保存鱼卡与课表
        List<CourseSchedule> courseSchedules = workOrderService.persistCardInfos(serviceList, workOrderList, recommandCourses);

        dataCollectorService.updateBothChnAndFnItem(serviceList.get(0).getStudentId());

        // 保存日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrderList);


        //判断是否指定过老师
        //StStudentApplyRecords stStudentApplyRecords = stStudentApplyRecordsService.findMyLastAssignTeacher(timeSlotParam.getStudentId(),timeSlotParam.getSkuId() );

        logger.info("ensureCourseTimesv2,studentId [{}],orderId [{}]",timeSlotParam.getStudentId(),timeSlotParam.getOrderId());
        StStudentSchema stStudentSchema  =  stStudentSchemaJpaRepository.findTop1ByStudentIdAndStSchemaAndSkuId(timeSlotParam.getStudentId(),StStudentSchema.StSchema.assgin,StStudentSchema.CourseType.getEnum(timeSlotParam.getSkuId()));

        if(null == stStudentSchema){
            // 分配老师
            timePickerService.getRecommandTeachers(serviceList.get(0), courseSchedules);
        }

        // 通知其他模块
        notifyOtherModules(workOrderList, serviceList.get(0));

        logger.info("学生[{}]选课结束", serviceList.get(0).getStudentId());
        return JsonResultModel.newJsonResultModel();
    }

    /**
     * 获取公开课课表
     * @param level
     * @param now
     * @return
     */
    public Map<String, Object> getStudentPublicClassTimeEnum(String level, LocalDate now) {
        String key = createPublicClassCacheWithLevelAndDateKey(level, now);
        Map classRoom = publicClassCacheWithLevelAndDate.get(key, Map.class);
        if(classRoom == null) {
            synchronized (this) {
                classRoom = getClassRoomByLevelAndNowWithDatabase(level, now);
                publicClassCacheWithLevelAndDate.putIfAbsent(key, classRoom);
            }
        }
        return classRoom;

    }


    public void evictPublicClassRoom(String level) {
        publicClassCacheWithLevelAndDate.evict(createPublicClassCacheWithLevelAndDateKey(level, LocalDate.now()));
    }

    private String createPublicClassCacheWithLevelAndDateKey(String level, LocalDate now) {
        return DateUtil.dateFormatter.format(now) + "_" + level;
    }


    /**
     * 学生进入公开课课堂
     * @return
     */
    public Map<String, Object> enterPublicClassRoom(Long studentId, Long smallClassId, String accessToken) {
        try {
            SmallClass smallClass = getClassRoomById(smallClassId);
            publicClassRoom.enter(smallClass, studentId, accessToken);
            return PublicClassMessageEnum.SUCCES.getMessageMap();
        } catch (Exception e) {
            if(e instanceof PublicClassException) {
                return (((PublicClassException) e).publicClassMessage).getMessageMap();
            }
            throw new BusinessException(e.getMessage());
        }
    }

    public List<Service> ensureConvertOver(TimeSlotParam timeSlotParam) {
        return ensureConvertOver(timeSlotParam, 0);
    }

    public void notifyOtherModules(List<WorkOrder> workOrders, Service service) {
        //通知上课中心
//        notifyCourseOnline(workOrders);
        //通知订单中心修改状态为已选课30

        serveService.notifyOrderUpdateStatus(service.getOrderId(), ConstantUtil.WORKORDER_SELECTED);
    }


    public List<Service> ensureConvertOver(TimeSlotParam timeSlotParam, int pivot) {
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

    private SmallClass getClassRoomById(Long smallClassId) {

        SmallClass smallClass = publicClassCacheWithId.get(smallClassId, SmallClass.class);
        if(smallClass == null) {
            synchronized (this) {
                smallClass = smallClassJpaRepository.findOne(smallClassId);
                if (smallClass == null) {
                    throw new PublicClassException(PublicClassMessageEnum.ERROR_PUBLIC_CLASS);
                }
                publicClassCacheWithId.putIfAbsent(smallClassId, smallClass);
            }
        }
        return smallClass;
    }

    /**
     * 从数据库中查询课堂
     * @param level
     * @param now
     * @return
     */
    private Map<String, Object> getClassRoomByLevelAndNowWithDatabase(String level, LocalDate now) {
        // 先判断是否是可用的等级
        CourseDifficultyEnum courseDifficulty;
        try {
            courseDifficulty = CourseDifficultyEnum.valueOf(level);
        } catch (Exception e) {
            throw new BusinessException("错误的level等级");
        }
        PublicClassTimeEnum publicClass = PublicClassTimeEnum.publicClassTime(courseDifficulty);
        List<SmallClass> publicClassList = smallClassJpaRepository.findByClassDateAndSlotIdAndSmallClassType(
                DateUtil.convertToDate(now), publicClass.getTimeRange().getSlotId(), ClassTypeEnum.PUBLIC.name());
        if(CollectionUtils.isEmpty(publicClassList)) {
            throw new BusinessException("今天没有对应的公开课!");
        }
        SmallClass smallClass = publicClassList.get(0);
        HashMap<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("classRoom", smallClass);
        resultMap.put("timeRange", publicClass.getTimeRange().setClassDate(now));
        return resultMap;
    }


    private List<WorkOrder> batchInitWorkorders(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> serviceList) {
        // 中教优先于外教
        serviceList.sort((c1, c2) -> Objects.equals(c1.getTutorType(), TutorType.CN.name()) ? 1 : 0);
        return selectMode.initWorkOrderList(timeSlotParam, selectMode, serviceList);
    }
}
