package com.boxfishedu.workorder.servicex.smallclass.status.studentstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.StatusDealer;
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
@Order(PublicClassInfoConstantStatus.STUDENT_CLASSING)
@Component
public class StudentClassingCustomer extends SmallClassEventCustomer implements StatusDealer {

    @Autowired
    Map<String, GroupInitStrategy> groupInitStrategyMap;

    @Autowired
    WorkOrderService workOrderService;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.STUDENT_CLASSING);
    }

    @Override
    protected WorkOrderService getWorkOrderService() {
        return workOrderService;
    }

    @Override
    public void execute(SmallClass smallClass) {

    }
}
