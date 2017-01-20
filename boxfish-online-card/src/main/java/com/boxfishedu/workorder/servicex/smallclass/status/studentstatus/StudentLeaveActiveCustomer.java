package com.boxfishedu.workorder.servicex.smallclass.status.studentstatus;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.smallclass.SmallClassLogService;
import com.boxfishedu.workorder.servicex.smallclass.initstrategy.GroupInitStrategy;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by hucl on 17/1/5.
 */
@Order(PublicClassInfoConstantStatus.STUDENT_LEAVE_ACTIVE)
@Component
public class StudentLeaveActiveCustomer extends SmallClassEventCustomer {

    @Autowired
    Map<String, GroupInitStrategy> groupInitStrategyMap;

    @Autowired
    WorkOrderService workOrderService;

    @Autowired
    SmallClassLogService smallClassLogService;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.STUDENT_LEAVE_ACTIVE);
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
        smallClassLogService.recordStudentLog(smallClass);
        switch (smallClass.getStatusEnum()) {
            case SMALL:
                if (smallClass.reachOverTime()) {
                    smallClass.setWriteBackDesc("完成[学生(学生主动退出)]");
                    this.writeStatusBack2Card(smallClass, FishCardStatusEnum.COMPLETED);
                } else {
                    smallClass.setWriteBackDesc("学生早退[学生(学生主动退出)]");
                    this.writeStatusBack2Card(smallClass, FishCardStatusEnum.STUDENT_LEAVE_EARLY);
                }
                break;
            default:
                break;
        }
    }
}
