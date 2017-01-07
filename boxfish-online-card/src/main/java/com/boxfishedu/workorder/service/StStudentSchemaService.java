package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentSchemaJpaRepository;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.boxfishedu.workorder.entity.mysql.StStudentSchema;
import com.boxfishedu.workorder.service.base.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/3/31.
 */
@Component
public class StStudentSchemaService extends BaseService<StStudentSchema, StStudentSchemaJpaRepository, Long> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

//    public Page<WorkOrder> findByServiceIdOrderByStartTime(Long serviceId, Pageable pageable) {
//        return jpa.findByServiceIdOrderByStartTime(serviceId, pageable);
//    }



//    private List<WorkOrder> getSortOrders(List<WorkOrder> workOrders){
//        workOrders.sort(new Comparator<WorkOrder>() {
//            @Override
//            public int compare(WorkOrder o1, WorkOrder o2) {
//                if(o1.getStartTime().after(o2.getStartTime())){
//                    return 0;
//                }
//                return -1;
//            }
//        });
//
//        return workOrders;
//    }

}
