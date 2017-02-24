package com.boxfishedu.workorder.servicex.studentrelated;

import com.alibaba.fastjson.JSON;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.*;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.*;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.instantclass.SmallClassQueryService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.studentrelated.recommend.RecommendHandlerHelper;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectModeFactory;
import com.boxfishedu.workorder.servicex.studentrelated.validator.StudentTimePickerValidatorSupport;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 系统自动生成鱼卡 及其 鱼卡的小班课id
 */
@Component
public class AutoTimePickerService {

    @Value("${parameter.small_class_size}")
    private Integer smallClassSize;

    @Autowired
    private TimePickerServiceXV1 timePickerServiceXV1;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private SmallClassRequester smallClassRequester;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;


    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ScheduleCourseInfoService courseInfoService;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 小班课一节课  自动操作 调用异步消息
    public void syncServiceToWorkOrder(Service service,Long smallClassId){

        threadPoolManager.execute(new Thread(() -> {
            this.auToMakeClassesForSmallClass(service,smallClassId);
        }));
    }



    public void auToMakeClassesForSmallClass(Service service,Long smallClassId){
        logger.info("auToMakeClassesForSmallClass->serviceInfo:[{}]" , JSON.toJSON(service));

        List<Service> servicedb = null;
        // 确保service 生成  循环六次 每次1秒
        for(int i=0;i<6;i++){
            servicedb = serviceJpaRepository.findByOrderId(service.getOrderId());
            if(CollectionUtils.isEmpty(servicedb)){ try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();} }else { break; }
        }

        if(CollectionUtils.isEmpty(servicedb)){
            logger.error("auToMakeClassesForSmallClassERROR,订单id:[{}]没有生成service",service.getOrderId());
            return;
        }

        SmallClass smallClass  = getSmallClass(service,smallClassId);

        if(Objects.isNull(smallClass)){
            logger.info("auToMakeClassesForSmallClassERROR,订单id:[{}]该学生不满足生成小班课 小班课id:[{}]",service.getOrderId(),smallClassId);
            return;
        }

        TimeSlotParam timeSlotParam = makeTimeSlotParam(service,smallClass);
        //组装TimeSlotParam 学生id
         timePickerServiceXV1.ensureCourseTimesv2(timeSlotParam);


        WorkOrder workOrder = null;
        //确定生成鱼卡数据
        for(int i=0;i<6;i++){
            workOrder = workOrderJpaRepository.findByOrderIdAndServiceId(servicedb.get(0).getOrderId(),servicedb.get(0).getId());
            if(null==workOrder){ try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();} }else { break; }
        }

        if(null == workOrder){
            logger.error("auToMakeClassesForSmallClassERROR,订单id:[{}]没有生成workOrder",service.getOrderId());
            return;
        }

        CourseSchedule courseSchedule = courseScheduleRepository.findTop1ByWorkorderId(workOrder.getId());

        // 根据smallClass 更新WorkOrder  CourseSchele
        workOrder.setCourseType(smallClass.getCourseType());
        workOrder.setCourseId(smallClass.getCourseId());
        workOrder.setCourseName(smallClass.getCourseName());
        workOrder.setTeacherId(smallClass.getTeacherId());
        workOrder.setTeacherName(smallClass.getTeacherName());
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        workOrder.setSmallClassId(smallClass.getId());
        workOrder.setUpdateTime(new Date());


        courseSchedule.setCourseType(smallClass.getCourseType());
        courseSchedule.setCourseId(smallClass.getCourseId());
        courseSchedule.setCourseName(smallClass.getCourseName());
        courseSchedule.setTeacherId(smallClass.getTeacherId());
        courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        courseSchedule.setSmallClassId(smallClass.getId());
        courseSchedule.setUpdateTime(new Date());

        restoreClassForSmallClass(workOrder,courseSchedule,smallClass);

        //课程保存到mongo中
        saveCourseScheToMongo(workOrder,courseSchedule);

    }

    private TimeSlotParam  makeTimeSlotParam(Service service,SmallClass smallClass){
        TimeSlotParam timeSlotParam = new TimeSlotParam();

        timeSlotParam.setStudentId(service.getStudentId());
        timeSlotParam.setComboType(service.getComboType());
        timeSlotParam.setOrderId(service.getOrderId());
        timeSlotParam.setProductType(service.getProductType());
        timeSlotParam.setSelectMode(0);
        timeSlotParam.setTutorType(service.getTutorType());
        if(TutorTypeEnum.CN.name().equals(service.getTutorType())){
            timeSlotParam.setSkuId(1);
        }
        if(TutorTypeEnum.FRN.name().equals(service.getTutorType())){
            timeSlotParam.setSkuId(2);
        }


        SelectedTime selectedTime = new SelectedTime();
        selectedTime.setSelectedDate(DateUtil.date2SimpleString(smallClass.getStartTime()));
        selectedTime.setTimeSlotId(smallClass.getSlotId());
        List<SelectedTime> selectedTimes = Lists.newArrayList(selectedTime);

        timeSlotParam.setSelectedTimes(selectedTimes);
        logger.info("auToMakeClassesForSmallClass->timeSlotParam:[{}]" , JSON.toJSON(timeSlotParam));
        return timeSlotParam;
    }


    public SmallClass getSmallClass(Service service,Long smallClassId){
        String level = smallClassRequester.fetchUserDifficultyInfo(service.getStudentId());
        SmallClass smallClass =  smallClassJpaRepository.findOne(smallClassId);

        List<WorkOrder> workOrders = workOrderJpaRepository.findBySmallClassId(smallClassId);

        if(Objects.equals(level,smallClass.getDifficultyLevel()) && workOrders.size()<smallClassSize){
            return smallClass;
        }

        return null;

    }



    @Transactional
    public void restoreClassForSmallClass(WorkOrder workOrder, CourseSchedule  courseSchedule,SmallClass smallClass){
        // 更新workOrder 和 courseSchedule
        workOrderService.updateWorkOrderAndSchedule(workOrder,courseSchedule);

        changeWorkOrderLog(workOrder,"分配课程");
        changeWorkOrderLog(workOrder,"分配老师");

        // 添加小班课群主关系
        smallClass.setAllStudentIds(Lists.newArrayList(workOrder.getStudentId()));
        FishCardGroupsInfo fishCardGroupsInfo =  courseOnlineRequester.buildsmallClassChatRoom(smallClass);

        String  groupId = fishCardGroupsInfo==null?"":fishCardGroupsInfo.getGroupId();  //群主Id
        Long  chatRoomId= fishCardGroupsInfo==null?null:fishCardGroupsInfo.getChatRoomId();//房间号
        logger.info( String.format( "auToMakeClassesForSmallClassFishCardGroupsInfo:%s__ %s",groupId,chatRoomId));

        //调用首页接口
        dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());

    }


    private void changeWorkOrderLog(WorkOrder workOrder,String content) {
        WorkOrderLog workOrderLog = new WorkOrderLog();
        workOrderLog.setCreateTime(new Date());
        workOrderLog.setWorkOrderId(workOrder.getId());
        workOrderLog.setStatus(workOrder.getStatus());
        workOrderLog.setContent(String.format("%s[小班课][后台添加学生]",content));
        workOrderLogService.save(workOrderLog);
    }

    //课程保存到mongo中
    public void saveCourseScheToMongo(WorkOrder workOrder, CourseSchedule courseSchedule){
        RecommandCourseView courseView = recommandCourseRequester.getCourseViewDetail(courseSchedule.getCourseId());
        courseInfoService.saveSingleCourseInfo(workOrder,courseSchedule,courseView);
    }
}
