package com.boxfishedu.workorder.service.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.absenteeism.sdk.AbsenteeismSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by ansel on 16/9/14.
 */

@Service
public class AbsenteeismServiceImpl implements AbsenteeismService{

    private static final long DEDUCT_SCORE = 30000l;

    private Logger logger = LoggerFactory.getLogger(AbsenteeismServiceImpl.class);

    @Autowired
    AbsenteeismSDK absenteeismSDK;

    @Autowired
    WorkOrderJpaRepository workOrderJpaRepository;

    @Override
    public JsonResultModel absenteeismDeductScore(Long studentId){
        logger.info("@AbsenteeismServiceImpl Student played truant. Deducting score ...");
        return absenteeismSDK.absenteeismDeductScore(studentId,DEDUCT_SCORE);
    }

    @Override
    public void queryAbsentStudent() {
        LocalDateTime now = LocalDateTime.now();
        List<WorkOrder> workOrderList = workOrderJpaRepository.queryAbsentStudent(DateUtil.localDate2Date(now.minusDays(1)),
                DateUtil.localDate2Date(now.minusDays(0)));
        for (WorkOrder workOrder: workOrderList
             ) {
            absenteeismDeductScore(workOrder.getStudentId());
            logger.info("@queryAbsentStudent Deducting {} score " + workOrder.getId());
        }
    }
}
