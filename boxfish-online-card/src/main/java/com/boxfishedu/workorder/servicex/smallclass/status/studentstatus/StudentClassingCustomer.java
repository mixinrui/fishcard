package com.boxfishedu.workorder.servicex.smallclass.status.studentstatus;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.smallclass.SmallClassLogService;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.StatusDealer;
import com.boxfishedu.workorder.servicex.smallclass.initstrategy.GroupInitStrategy;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

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

    @Autowired
    SmallClassLogService smallClassLogService;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.STUDENT_CLASSING);
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
        smallClassLogService.recordStudentLog(smallClass);
        switch (smallClass.getStatusEnum()) {
            case SMALL:
                //模拟师生已连通
                smallClass.setWriteBackDesc("师生已连通[小班学生上报正在上课]");
                this.writeStatusBack2Card(smallClass, FishCardStatusEnum.CONNECTED);

                //模拟学生正在上课
                smallClass.setWriteBackDesc("正在上课[小班学生上报正在上课]");
                this.writeStatusBack2Card(smallClass, FishCardStatusEnum.ONCLASS);
                break;
            default:
                break;
        }
    }
}
