package com.boxfishedu.workorder.servicex.smallclass.status.event;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


/**
 * Created by hucl on 17/1/5.
 */
@Data
public abstract class SmallClassEventCustomer {
    protected final Logger logger = LoggerFactory.getLogger("SmallClassEventCustomer");

    public final String prefix = "INIT_";

    protected PublicClassInfoStatusEnum smallClassCardStatus;

    protected abstract WorkOrderService getWorkOrderService();


    public void exec(SmallClassEvent smallClassEvent) {
        SmallClass smallClass = smallClassEvent.getSource();
        this.execute(smallClass);
    }

    public abstract void execute(SmallClass smallClass);


    public void writeStatusBack2Card(SmallClass smallClass, FishCardStatusEnum fishCardStatusEnum) {
        WorkOrder workOrder =
                this.getWorkOrderService().findBySmallClassIdAndStudentId(
                        smallClass.getId(), smallClass.getStatusReporter());
        workOrder.setStatus(fishCardStatusEnum.getCode());

        if (Objects.isNull(smallClass.getWriteBackDesc())) {
            this.getWorkOrderService().saveStatusForCardAndSchedule(workOrder);
        } else {
            this.getWorkOrderService().saveStatusForCardAndSchedule(
                    workOrder, smallClass.getWriteBackDesc());
        }
    }
}
