package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.entity.mongo.SmallClassStudentsRelation;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by hucl on 17/1/16.
 */
public class SmallClassTimerServiceX {
    //创建学生小班课的上课关系
    public void buildSmallCLassRelations() {
        List<SmallClass> smallClasses = getSmallClasses();
        if (CollectionUtils.isEmpty(smallClasses)) {
            return;
        }
        smallClasses.forEach(
                smallClass -> {
                    List<WorkOrder> workOrders = getCardsToBuildRelation(smallClass);
                }
        );
        List<WorkOrder> workOrders = getCardsToBuildRelation();

    }

    private List<SmallClass> getSmallClasses() {
        return null;
    }

    private List<WorkOrder> getCardsToBuildRelation(SmallClass smallClass) {
        return null;
    }

    public List<SmallClassStudentsRelation> fromWorkOrder(List<WorkOrder> workOrders) {
        List<Long> students = workOrders.stream()
                                        .map(WorkOrder::getStudentId)
                                        .distinct()
                                        .collect(Collectors.toList());
        return null;

    }
}
