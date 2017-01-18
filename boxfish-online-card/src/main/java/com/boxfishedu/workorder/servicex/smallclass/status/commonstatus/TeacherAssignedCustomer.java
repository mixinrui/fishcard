package com.boxfishedu.workorder.servicex.smallclass.status.commonstatus;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.smallclass.SmallClassLogService;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassServiceX;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by hucl on 17/1/5.
 */
@Order(PublicClassInfoConstantStatus.TEACHER_ASSIGNED)
@Component
public class TeacherAssignedCustomer extends SmallClassEventCustomer {
    @Autowired
    WorkOrderService workOrderService;

    @Autowired
    SmallClassLogService smallClassLogService;

    @Autowired
    WorkOrderJpaRepository workOrderJpaRepository;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.TEACHER_ASSIGNED);
    }

    @Override
    protected WorkOrderService getWorkOrderService() {
        return workOrderService;
    }

    @Override
    protected void postHandle(SmallClass smallClass) {

    }

    @Override
    public void execute(SmallClass smallClass) {
        smallClassLogService.recordSystemLog(
                smallClass, PublicClassInfoStatusEnum.TEACHER_ASSIGNED.getCode()
                , String.join(smallClass.getCourseName(), "分配教师[", "]"));

        switch (smallClass.getStatusEnum()) {
            case SMALL:
                smallClass.setWriteBackDesc("分配教师[小班课]");
                this.writeStatusBack2Card(smallClass, FishCardStatusEnum.TEACHER_ASSIGNED);
                break;
            default:
                smallClass.setWriteBackDesc("分配教师[公开课]");
                List<WorkOrder> workOrders = workOrderJpaRepository.findBySmallClassId(smallClass.getId());
                smallClass.setAllCards(workOrders);
                this.writeStatusBack2Card(smallClass, FishCardStatusEnum.TEACHER_ASSIGNED);
                break;
        }
    }
}
