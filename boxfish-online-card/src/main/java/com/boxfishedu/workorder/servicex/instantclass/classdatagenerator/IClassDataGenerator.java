package com.boxfishedu.workorder.servicex.instantclass.classdatagenerator;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;

/**
 * Created by hucl on 16/11/9.
 */
public interface IClassDataGenerator {
    InstantClassCard initCardAndSchedule(InstantClassCard instantClassCard);

    void initCourses(WorkOrder workOrder);
}
