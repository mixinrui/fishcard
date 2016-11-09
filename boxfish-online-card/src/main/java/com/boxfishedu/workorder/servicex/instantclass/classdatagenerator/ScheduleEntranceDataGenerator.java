package com.boxfishedu.workorder.servicex.instantclass.classdatagenerator;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.LogManager;

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

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public void initCardAndSchedule(InstantClassCard instantClassCard) {
        WorkOrder workOrder=workOrderJpaRepository.findOne(instantClassCard.getWorkorderId());
        List<WorkOrder> workOrders=workOrderJpaRepository.findByOrderIdAndStartTimeAfterOrderByStartTimeAsc(workOrder.getService().getOrderId(),new Date());
        Queue<WorkOrder> workOrderQueue=new LinkedList<>(workOrders);
        List<WorkOrder> dealedCardList=new ArrayList<>();
        Map<WorkOrder,String> logMap= Maps.newHashMap();

        WorkOrder firstWorkOrder=workOrderQueue.poll();
        WorkOrder oldCard=new WorkOrder();
        BeanUtils.copyProperties(firstWorkOrder,oldCard);

        dealFirstWorkOrder(instantClassCard,firstWorkOrder,logMap);

        //最近一个鱼卡:workOrder
        WorkOrder currentWorkOrder=workOrderQueue.poll();
        while(currentWorkOrder!=null){
            WorkOrder tmp= new WorkOrder();
            BeanUtils.copyProperties(currentWorkOrder,tmp);

            currentWorkOrder.setStartTime(oldCard.getStartTime());
            currentWorkOrder.setEndTime(oldCard.getEndTime());
            currentWorkOrder.setSlotId(oldCard.getSlotId());
            //TODO:更换时间以后,可能需要更换教师;需要师生运营提供接口判断是否可以上那个时间点的课程;可以异步操作
            currentWorkOrder.setTeacherId(oldCard.getTeacherId());
            currentWorkOrder=workOrderQueue.poll();

            BeanUtils.copyProperties(tmp,oldCard);
            dealedCardList.add(currentWorkOrder);
            logMap.put(currentWorkOrder,"实时上课数据移动,旧时间:["+DateUtil.Date2String(oldCard.getStartTime())+"],教师:["+oldCard.getTeacherId()+"]");
        }

        dealedCardList.forEach(card->workOrderService.saveWorkOrderAndSchedule(workOrder,initSchedule(workOrder)));
        printLog(logMap);
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
        firstWorkOrder.setSlotId(instantClassCard.getSlotId().intValue());
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
        return courseSchedule;
    }

}
