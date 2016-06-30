package com.boxfishedu.workorder.service.workorderlog;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
import com.boxfishedu.workorder.dao.mongo.WorkOrderLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by hucl on 16/4/13.
 */
@Component
public class WorkOrderLogService {
    @Autowired
    private LogPoolManager logPoolManager;

    @Autowired
    private WorkOrderLogMorphiaRepository workOrderLogMorphiaRepository;

    public void  save(WorkOrderLog workOrderLog){
        workOrderLogMorphiaRepository.save(workOrderLog);
    }

    public void  save(Iterable<WorkOrderLog> workOrderLogs){
        workOrderLogMorphiaRepository.save(workOrderLogs);
    }

    public Optional<WorkOrderLog> getById(Long id){
        return workOrderLogMorphiaRepository.getById(id);
    }

    public List<WorkOrderLog> queryByWorkId(Long workId) {
        List<WorkOrderLog> workOrderLogs=workOrderLogMorphiaRepository.queryByWorkId(workId);
        return workOrderLogs;
    }

    public void batchSaveWorkOrderLogs(List<WorkOrder> workOrders){
        //批量生成鱼卡流水日志,此处不用关心其是否执行完,将其放入线程池提高效率
        logPoolManager.execute(new Thread(() -> {
            saveWorkorderLogs(workOrders);
        }));
    }

    public void saveWorkOrderLog(WorkOrder workOrder){
        WorkOrderLog workOrderLog = new WorkOrderLog();
        workOrderLog.setWorkOrderId(workOrder.getId());
        workOrderLog.setStatus(workOrder.getStatus());
        workOrderLog.setContent(FishCardStatusEnum.getDesc(workOrder.getStatus()));
        save(workOrderLog);
    }

    private void saveWorkorderLogs(List<WorkOrder> workOrders) {
        List<WorkOrderLog> workOrderLogs = new ArrayList<>();
        for (WorkOrder workOrder : workOrders) {
            WorkOrderLog workOrderLog = new WorkOrderLog();
            workOrderLog.setWorkOrderId(workOrder.getId());
            workOrderLog.setStatus(workOrder.getStatus());
            workOrderLog.setContent(FishCardStatusEnum.getDesc(workOrder.getStatus()));
            workOrderLogs.add(workOrderLog);
        }
        save(workOrderLogs);
    }
}
