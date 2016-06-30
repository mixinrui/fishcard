package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by hucl on 16/5/5.
 */
@Component
public class CourseOnlineService {
    @Autowired
    private UrlConf urlConf;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CourseScheduleService courseScheduleService;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //通知上课中心,添加学生和答疑老师做好友
    public void notifyMakeFriends(WorkOrder workOrder){
//        String url=String.format("%s/teaching/add_friends/{source_id}/{target_id}")
    }

    public void notAllowUpdateStatus(WorkOrder workOrder){
        Integer savedStatus=workOrder.getStatus();
        boolean flag=(savedStatus== FishCardStatusEnum.COMPLETED.getCode())
                ||(savedStatus==FishCardStatusEnum.COMPLETED_FORCE.getCode())
                ||(savedStatus==FishCardStatusEnum.COMPLETED_FORCE_SERVER.getCode())
                ||(savedStatus==FishCardStatusEnum.TEACHER_ABSENT.getCode())
                ||(savedStatus==FishCardStatusEnum.STUDENT_ABSENT.getCode());
        if(flag){
            throw new BusinessException("当前鱼卡状态已经处于冻结状态,不允许再做修改!");
        }
    }

    public void handleException(WorkOrder workOrder, CourseSchedule courseSchedule, Integer status){
        workOrder.setStatus(status);
        saveStatus4WorkOrderAndSchedule(workOrder,courseSchedule);
    }

    public void saveStatus4WorkOrderAndSchedule(WorkOrder workOrder,CourseSchedule courseSchedule){
        courseSchedule.setStatus(workOrder.getStatus());
        courseSchedule.setUpdateTime(workOrder.getUpdateTime());
        workOrderService.save(workOrder);
        courseScheduleService.save(courseSchedule);
    }
}
