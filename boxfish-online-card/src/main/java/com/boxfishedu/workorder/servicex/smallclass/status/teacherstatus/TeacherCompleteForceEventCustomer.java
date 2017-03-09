package com.boxfishedu.workorder.servicex.smallclass.status.teacherstatus;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
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
import java.util.Date;
import java.util.Objects;

/**
 * Created by hucl on 17/1/5.
 */
@Order(PublicClassInfoConstantStatus.TEACHER_COMPLETED_FORCE)
@Component
public class TeacherCompleteForceEventCustomer extends SmallClassEventCustomer {
    @Autowired
    WorkOrderService workOrderService;

    @Autowired
    SmallClassLogService smallClassLogService;

    @Autowired
    SmallClassService smallClassService;

    @Autowired
    SmallClassJpaRepository smallClassJpaRepository;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.TEACHER_COMPLETED_FORCE);
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
        smallClassService.persistIntoDb(smallClass,PublicClassInfoStatusEnum.TEACHER_COMPLETED_FORCE);

        switch (smallClass.getStatusEnum()) {
            case SMALL:
//                smallClass.setWriteBackDesc("强制完成[教师小班课]");
//                this.writeStatusBack2Card(smallClass, FishCardStatusEnum.COMPLETED_FORCE,true);
                break;
            default:
                smallClass.setWriteBackDesc("强制完成[教师公开课]");
                this.writeStatusBack2Card(smallClass, FishCardStatusEnum.COMPLETED_FORCE);
                break;
        }
    }
}
