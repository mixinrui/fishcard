package com.boxfishedu.workorder.servicex.instantclass.classdatagenerator;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;

import java.util.List;

/**
 * Created by hucl on 16/11/9.
 */
public interface IClassDataGenerator {
    List<WorkOrder> initCardAndSchedule(InstantClassCard instantClassCard);



    void initCourses(WorkOrder workOrder);
}
