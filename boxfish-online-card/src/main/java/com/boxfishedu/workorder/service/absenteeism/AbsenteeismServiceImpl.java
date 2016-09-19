package com.boxfishedu.workorder.service.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.config.ServiceGateWayType;
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
import java.util.Map;

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

    @Autowired
    ServiceGateWayType serviceGateWayType;

    @Override
    public Map absenteeismDeductScore(WorkOrder workOrder){
        logger.info("@AbsenteeismServiceImpl Student played truant. Deducting score ...");
        return absenteeismSDK.absenteeismDeductScore(workOrder);
    }

    @Override
    public void queryAbsentStudent() {
        LocalDateTime now = LocalDateTime.now();
        List<WorkOrder> workOrderList = null;
        if (serviceGateWayType.getType().equals("test") || serviceGateWayType.getType().equals("development_new")){
            workOrderList = workOrderJpaRepository.queryAbsentStudent(DateUtil.localDate2Date(now.minusMinutes(5)),DateUtil.localDate2Date(now.minusMinutes(0)), ComboTypeEnum.EXCHANGE.toString());

        }else{
            workOrderList = workOrderJpaRepository.queryAbsentStudent(DateUtil.localDate2Date(now.minusDays(1)),DateUtil.localDate2Date(now.minusDays(0)), ComboTypeEnum.EXCHANGE.toString());
        }
        for (WorkOrder workOrder: workOrderList) {
            logger.info("@queryAbsentStudent Deducting score " + workOrder.getId());
            Map map = absenteeismDeductScore(workOrder);
            logger.info("@queryAbsentStudent Deducted result:" + map);
            if(!ObjectUtils.isEmpty(map)){
                if ( map.get("success").equals("true")) {
                    workOrder.setDeductScoreStatus(FishCardStatusEnum.DEDUCT_SCORE_STATUS.getCode());
                    workOrderJpaRepository.save(workOrder);
                }
            }
        }
    }
}
