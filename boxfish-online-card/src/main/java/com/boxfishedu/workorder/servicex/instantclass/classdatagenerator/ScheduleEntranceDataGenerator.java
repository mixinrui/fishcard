package com.boxfishedu.workorder.servicex.instantclass.classdatagenerator;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by hucl on 16/11/9.
 */
@Component
public class ScheduleEntranceDataGenerator implements IClassDataGenerator {
    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private LogPoolManager logPoolManager;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public List<WorkOrder> initCardAndSchedule(InstantClassCard instantClassCard) {
        WorkOrder workOrder=workOrderJpaRepository.findOne(instantClassCard.getWorkorderId());
        List<WorkOrder> workOrders=workOrderJpaRepository
                .findByOrderIdAndStartTimeAfterOrderByStartTimeAsc(workOrder.getService().getOrderId(),new Date());
        List<WorkOrder> typeChangedList= new ArrayList<>();

        Map<WorkOrder,String> logMap= Maps.newHashMap();

        WorkOrder oldCard=workOrders.get(0).clone();

        dealFirstWorkOrder(instantClassCard,workOrders.get(0),logMap);

        for(int i=1;i<workOrders.size();i++){
            WorkOrder tmp= workOrders.get(i).clone();

            workOrders.get(i).setStartTime(oldCard.getStartTime());
            workOrders.get(i).setEndTime(oldCard.getEndTime());
            workOrders.get(i).setSlotId(oldCard.getSlotId());
            //TODO:更换时间以后,可能需要更换教师;需要师生运营提供接口判断是否可以上那个时间点的课程;可以异步操作
            workOrders.get(i).setTeacherId(oldCard.getTeacherId());

            if(!oldCard.getCourseType().equals(workOrders.get(i).getClassType())){
                typeChangedList.add(workOrders.get(i));
            }

            logMap.put(workOrders.get(i),"实时上课数据移动,旧时间:["+DateUtil.Date2String(tmp.getStartTime())+"],教师:["+tmp.getTeacherId()+"]");

            BeanUtils.copyProperties(tmp,oldCard);
        }

        workOrders.forEach(card->workOrderService.saveWorkOrderAndSchedule(workOrder,initSchedule(workOrder)));
        printLog(logMap);

        return typeChangedList;
    }

    private WorkOrder dealFirstWorkOrder(InstantClassCard instantClassCard, WorkOrder firstWorkOrder,Map<WorkOrder,String> logMap) {
        if(instantClassCard.getWorkorderId()!=firstWorkOrder.getId()){
            logger.error("@initCardAndSchedule#exception数据存在问题,放弃匹配教师,鱼卡:{}",firstWorkOrder);
            throw new BusinessException("内部数据错误");
        }
        String oldTime=DateUtil.Date2String(firstWorkOrder.getStartTime());
        Long oldTeacher=firstWorkOrder.getTeacherId();

        LocalDateTime localDateTime=LocalDateTime.now(ZoneId.systemDefault());
        firstWorkOrder.setStartTime(DateUtil.localDate2Date(localDateTime));
        firstWorkOrder.setEndTime(DateUtil.localDate2Date(localDateTime.plusMinutes(25)));
        firstWorkOrder.setTeacherId(instantClassCard.getTeacherId());
        firstWorkOrder.setTeacherName(instantClassCard.getTeacherName());
        firstWorkOrder.setSlotId(instantClassCard.getSlotId().intValue());
        firstWorkOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        firstWorkOrder.setClassType(ClassTypeEnum.INSTNAT.toString());
        logMap.put(firstWorkOrder,"生成实时上课鱼卡,旧时间["+oldTime+"],旧教师:["+oldTeacher+"]");
        return firstWorkOrder;
    }

    @Override
    public void initCourses(WorkOrder workOrder) {

    }

    private void printLog(Map<WorkOrder,String> logMap){
        logMap.forEach((workorder,desc)->{
            logPoolManager.execute(new Thread(() -> {
                workOrderLogService.saveWorkOrderLog(workorder,desc);
            }));
        });
    }

    //TODO:需要优化为一次从db中把schedule全取出
    private CourseSchedule initSchedule(WorkOrder workOrder){
        CourseSchedule courseSchedule=courseScheduleRepository.findByWorkorderId(workOrder.getId());
        courseSchedule.setClassDate(workOrder.getStartTime());
        courseSchedule.setTimeSlotId(workOrder.getSlotId());
        courseSchedule.setTeacherId(workOrder.getTeacherId());
        courseSchedule.setUpdateTime(workOrder.getUpdateTime());
        courseSchedule.setClassType(workOrder.getClassType());
        if(StringUtils.equals(ClassTypeEnum.INSTNAT.toString(),workOrder.getClassType())){
           courseSchedule.setInstantStartTtime(DateUtil.dateTrimYear(workOrder.getStartTime()));
            courseSchedule.setInstantEndTtime(DateUtil.dateTrimYear(workOrder.getEndTime()));
        }
        return courseSchedule;
    }

}
