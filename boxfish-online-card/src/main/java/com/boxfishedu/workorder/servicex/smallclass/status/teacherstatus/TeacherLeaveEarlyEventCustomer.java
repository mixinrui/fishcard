package com.boxfishedu.workorder.servicex.smallclass.status.teacherstatus;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.smallclass.SmallClassLogService;
import com.boxfishedu.workorder.service.smallclass.SmallClassService;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/8.
 */
@Order(PublicClassInfoConstantStatus.TEACHER_LEAVE_EARLY)
@Component
public class TeacherLeaveEarlyEventCustomer extends SmallClassEventCustomer {
    @Autowired
    WorkOrderService workOrderService;

    @Autowired
    SmallClassLogService smallClassLogService;

    @Autowired
    SmallClassService smallClassService;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.TEACHER_LEAVE_EARLY);
    }

    @Override
    protected WorkOrderService getWorkOrderService() {
        return workOrderService;
    }

    @Override
    protected void postHandle(SmallClass smallClass) {

    }

    @Override
    protected SmallClassLogService getSmallClassLogService() {
        return smallClassLogService;
    }

    @Override
    public void execute(SmallClass smallClass) {
        smallClassLogService.recordTeacherLog(smallClass);
        smallClassService.persistIntoDb(smallClass, PublicClassInfoStatusEnum.TEACHER_LEAVE_EARLY);

        switch (smallClass.getStatusEnum()) {
            case SMALL:
//                smallClass.setWriteBackDesc("教师早退[教师小班课]");
//                this.writeStatusBack2Card(smallClass, FishCardStatusEnum.TEACHER_LEAVE_EARLY, true);
                break;
            default:
                smallClass.setWriteBackDesc("教师早退[教师公开课]");
                this.writeStatusBack2Card(smallClass, FishCardStatusEnum.TEACHER_LEAVE_EARLY);
                break;
        }
    }
}
