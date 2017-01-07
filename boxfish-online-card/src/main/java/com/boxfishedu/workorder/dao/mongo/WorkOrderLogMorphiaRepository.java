package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Component
public class WorkOrderLogMorphiaRepository extends BaseMorphiaRepository<WorkOrderLog>{

    public Optional<WorkOrderLog> getById(Long id) {
        Query<WorkOrderLog> query = datastore.createQuery(WorkOrderLog.class);
        query.criteria("id").equal(id);
        query.limit(1);
        return Optional.ofNullable(query.get());
    }

    public List<WorkOrderLog> queryByWorkId(Long workId, boolean ascFlag) {
        Query<WorkOrderLog> query = datastore.createQuery(WorkOrderLog.class);
        query.and(query.criteria("workOrderId").equal(workId));

        if(ascFlag){
            query.order("createTime");
        }
        else{
            query.order("-createTime");
        }

        List<WorkOrderLog> workOrderLogs= query.asList();
        int limit=0;
        for (WorkOrderLog workOrderLog:workOrderLogs){
            limit++;
            workOrderLog.setSeqNum(limit);
        }
        return workOrderLogs;
    }

    /**
     * 查看老师是否处于呼叫 或者 联通情况
     * @param workId
     * @param status
     * @return
     */
    public List<WorkOrderLog> queryByWorkIdAndStatus(Long workId, List<Integer> status) {
        Query<WorkOrderLog> query = datastore.createQuery(WorkOrderLog.class);
        query.and(query.criteria("workOrderId").equal(workId));

        query.and(query.criteria("status").in(status));

        List<WorkOrderLog> workOrderLogs= query.asList();

        return workOrderLogs;
    }

}
