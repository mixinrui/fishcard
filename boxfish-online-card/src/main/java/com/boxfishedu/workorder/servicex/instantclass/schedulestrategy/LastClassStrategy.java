package com.boxfishedu.workorder.servicex.instantclass.schedulestrategy;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
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
 * Created by hucl on 16/12/20.
 */
@Component(ScheduleStrategyEnum.SCHEDULE_LAST)
public class LastClassStrategy implements ScheduleStrategy {

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private LogPoolManager logPoolManager;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<WorkOrder> prepareInstantSchedule(InstantClassCard instantClassCard) {

        WorkOrder workOrder = workOrderJpaRepository.findOne(instantClassCard.getWorkorderId());
        List<WorkOrder> workOrders = workOrderJpaRepository
                .findByOrderIdAndStartTimeAfterOrderByStartTimeAsc(workOrder.getService().getOrderId(), new Date());
        List<WorkOrder> typeChangedList = Lists.newArrayList();
        List<WorkOrder> typeUnChangedList = Lists.newArrayList();

        Map<WorkOrder, String> logMap = Maps.newHashMap();

        WorkOrder oldCard = workOrders.get(0).clone();

        dealFirstWorkOrder(instantClassCard, workOrders.get(0), logMap);

        ThreadLocalUtil.waitReleasedWorkOrder.set(null);

        for (int i = 1; i < workOrders.size(); i++) {
            WorkOrder tmp = workOrders.get(i).clone();

            //释放最后一个教师的资源
            if (i == workOrders.size() - 1) {
                ThreadLocalUtil.waitReleasedWorkOrder.set(tmp.clone());
            }

            workOrders.get(i).setStartTime(oldCard.getStartTime());
            workOrders.get(i).setEndTime(oldCard.getEndTime());
            workOrders.get(i).setSlotId(oldCard.getSlotId());
            //TODO:更换时间以后,可能需要更换教师;需要师生运营提供接口判断是否可以上那个时间点的课程;可以异步操作
            workOrders.get(i).setTeacherId(oldCard.getTeacherId());
            workOrders.get(i).setTeacherName(oldCard.getTeacherName());

            if (!oldCard.getCourseType().equals(workOrders.get(i).getClassType())) {
                typeChangedList.add(workOrders.get(i));
            } else {
                typeUnChangedList.add(workOrders.get(i));
            }

            logMap.put(workOrders.get(i), "实时上课数据移动,旧时间:[" + DateUtil.Date2String(tmp.getStartTime()) + "],教师:[" + tmp.getTeacherId() + "]");

            BeanUtils.copyProperties(tmp, oldCard);
        }

        courseOnlineRequester.rebuildGroup(typeUnChangedList);

        workOrders.forEach(card -> workOrderService.saveWorkOrderAndSchedule(card, initSchedule(card)));
        printLog(logMap);

        return typeChangedList;
    }

    @Override
    public Optional<WorkOrder> getCardToStart(InstantRequestParam instantRequestParam, int teachingType) {
        return workOrderJpaRepository
                .findTop1ByStudentIdAndSkuIdAndIsFreezeAndStartTimeAfterAndClassTypeNotInOrderByStartTimeAsc(
                        instantRequestParam.getStudentId(), teachingType, new Integer(0), new Date(), Arrays.asList(ClassTypeEnum.SMALL.name()));
    }

    private WorkOrder dealFirstWorkOrder(InstantClassCard instantClassCard, WorkOrder firstWorkOrder, Map<WorkOrder, String> logMap) {
        if (instantClassCard.getWorkorderId() != firstWorkOrder.getId()) {
            WorkOrder instantWorkOrder = workOrderService.findOne(instantClassCard.getWorkorderId());
            logger.error("@initCardAndSchedule#exception数据存在问题,放弃匹配教师,鱼卡:[{}],instantcard:[{}],instantWorkOrder[{}]"
                    , firstWorkOrder, JacksonUtil.toJSon(instantClassCard), JacksonUtil.toJSon(instantWorkOrder));
            throw new BusinessException("内部数据错误");
        }
        String oldTime = DateUtil.Date2String(firstWorkOrder.getStartTime());
        Long oldTeacher = firstWorkOrder.getTeacherId();

        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.systemDefault());
        firstWorkOrder.setStartTime(DateUtil.localDate2Date(localDateTime));
        firstWorkOrder.setEndTime(DateUtil.localDate2Date(localDateTime.plusMinutes(25)));
        firstWorkOrder.setTeacherId(instantClassCard.getTeacherId());
        firstWorkOrder.setTeacherName(instantClassCard.getTeacherName());
        firstWorkOrder.setSlotId(instantClassCard.getSlotId().intValue());
        firstWorkOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        firstWorkOrder.setClassType(ClassTypeEnum.INSTNAT.toString());
        logMap.put(firstWorkOrder, "生成实时上课鱼卡,旧时间[" + oldTime + "],旧教师:[" + oldTeacher + "]");
        return firstWorkOrder;
    }

    private void printLog(Map<WorkOrder, String> logMap) {
        logMap.forEach((workorder, desc) -> {
            logPoolManager.execute(new Thread(() -> {
                workOrderLogService.saveWorkOrderLog(workorder, desc);
            }));
        });
    }

    //TODO:需要优化为一次从db中把schedule全取出
    private CourseSchedule initSchedule(WorkOrder workOrder) {
        CourseSchedule courseSchedule = courseScheduleRepository.findByWorkorderId(workOrder.getId());
        courseSchedule.setClassDate(workOrder.getStartTime());
        courseSchedule.setStartTime(workOrder.getStartTime());
        courseSchedule.setTimeSlotId(workOrder.getSlotId());
        courseSchedule.setTeacherId(workOrder.getTeacherId());
        courseSchedule.setUpdateTime(workOrder.getUpdateTime());
        courseSchedule.setClassType(workOrder.getClassType());
        courseSchedule.setStatus(workOrder.getStatus());
        if (StringUtils.equals(ClassTypeEnum.INSTNAT.toString(), workOrder.getClassType())) {
            courseSchedule.setInstantStartTtime(DateUtil.dateTrimYear(workOrder.getStartTime()));
            courseSchedule.setInstantEndTtime(DateUtil.dateTrimYear(workOrder.getEndTime()));
        }
        return courseSchedule;
    }
}
