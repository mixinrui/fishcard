package com.boxfishedu.workorder.servicex.smallclass.status.teacherstatus;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.smallclass.SmallClassLogService;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(PublicClassInfoConstantStatus.TEACHER_CLASSING)
@Component
public class TeacherClassingEventCustomer extends SmallClassEventCustomer {

    @Autowired
    WorkOrderService workOrderService;

    @Autowired
    SmallClassLogService smallClassLogService;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.TEACHER_CLASSING);
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

        switch (smallClass.getStatusEnum()) {
            case SMALL:
                smallClass.setWriteBackDesc("正在上课[教师]");
                this.writeStatusBack2Card(smallClass, FishCardStatusEnum.ONCLASS);
                break;
            default:
                break;
        }
    }
}
