package com.boxfishedu.workorder.service.fishcardcenter;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by hucl on 16/6/18.
 */
@Component
public class FishCardMakeUpService {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CourseScheduleService courseScheduleService;

    public void processMakeUpParam(WorkOrder oldWorkOrder){
        Short makeUpFlag=oldWorkOrder.getMakeUpFlag();
        if(makeUpFlag!=null&&makeUpFlag!=0){
            logger.info("!FishCardMakeUpService.processMakeUpParam鱼卡[{}]已经安排补课,不做处理,直接返回");
            throw new BusinessException("鱼卡["+oldWorkOrder.getId()+"]已经安排补课,请勿重复操作");
        }
        Integer status=oldWorkOrder.getStatus();
        String msgTips="该鱼卡所处状态["+FishCardStatusEnum.getDesc(oldWorkOrder.getStatus())+"]不允许换课";
        if(status!= FishCardStatusEnum.TEACHER_ABSENT.getCode()){
            if(status!=FishCardStatusEnum.TEACHER_LEAVE_EARLY.getCode()){
                if(status<FishCardStatusEnum.EXCEPTION.getCode()){
                    logger.info("鱼卡[{}]的状态[{}]不允许换课",oldWorkOrder.getId(),FishCardStatusEnum.getDesc(oldWorkOrder.getStatus()));
                    throw new BusinessException(msgTips);
                }
            }
        }
    }

    @Transactional
    public void saveBothOldAndNew(WorkOrder oldWorkOrder,WorkOrder newWorkOrder,CourseSchedule oldCourseSchedule,CourseSchedule newCourseSchedule){
       List<WorkOrder> workOrders= Lists.newArrayList();
        workOrders.add(oldWorkOrder);
        workOrders.add(newWorkOrder);
        workOrderService.save(workOrders);

        newCourseSchedule.setWorkorderId(newWorkOrder.getId());
        List<CourseSchedule> courseSchedules=Lists.newArrayList();
        courseSchedules.add(oldCourseSchedule);
        courseSchedules.add(newCourseSchedule);
        courseScheduleService.save(courseSchedules);
        logger.info("鱼卡[{}]的补课操作生成新鱼卡[{}]成功;生成新的排课表[{}]成功",oldWorkOrder.getId(),newWorkOrder.getId(),newCourseSchedule.getId());
    }
}
