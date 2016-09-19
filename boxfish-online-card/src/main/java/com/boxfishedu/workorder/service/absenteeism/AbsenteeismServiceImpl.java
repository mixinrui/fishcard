package com.boxfishedu.workorder.service.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.absenteeism.sdk.AbsenteeismSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by ansel on 16/9/14.
 */

@Service
public class AbsenteeismServiceImpl implements AbsenteeismService{

    private Logger logger = LoggerFactory.getLogger(AbsenteeismServiceImpl.class);

    @Autowired
    AbsenteeismSDK absenteeismSDK;

    @Autowired
    WorkOrderJpaRepository workOrderJpaRepository;

    @Override
    public JsonResultModel absenteeismDeductScore(WorkOrder workOrder){
        logger.info("@AbsenteeismServiceImpl Student played truant. Deducting score ...");
        return absenteeismSDK.absenteeismDeductScore(workOrder);
    }

    @Override
    public void queryAbsentStudent() {
        LocalDateTime now = LocalDateTime.now();
        List<WorkOrder> workOrderList = workOrderJpaRepository.queryAbsentStudent(DateUtil.localDate2Date(now.minusDays(30)),DateUtil.localDate2Date(now.minusDays(0)));
        for (WorkOrder workOrder: workOrderList) {
            logger.info("@queryAbsentStudent Deducting {} score " + workOrder.getId());
            JsonResultModel jsonResultModel = absenteeismDeductScore(workOrder);
            logger.info("@queryAbsentStudent Deducted result:" + jsonResultModel.getData());
            if(!ObjectUtils.isEmpty(jsonResultModel.getData())){
                workOrder.setDeduct_score_status(FishCardStatusEnum.DEDUCT_SCORE_STATUS.getCode());
                workOrderJpaRepository.save(workOrder);
            }
        }
    }
}
