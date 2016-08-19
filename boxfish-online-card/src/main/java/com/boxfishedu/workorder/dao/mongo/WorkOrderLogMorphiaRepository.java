package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WorkOrderLogMorphiaRepository extends BaseMorphiaRepository<WorkOrderLog>{

    public Optional<WorkOrderLog> getById(Long id) {
        Query<WorkOrderLog> query = datastore.createQuery(WorkOrderLog.class);
        query.criteria("id").equal(id);
        query.limit(1);
        return Optional.ofNullable(query.get());
    }

    public List<WorkOrderLog> queryByWorkId(Long workId) {
        Query<WorkOrderLog> query = datastore.createQuery(WorkOrderLog.class);
        query.and(query.criteria("workOrderId").equal(workId));
        //倒序
//        query.order("-createTime");
        query.order("createTime");
        List<WorkOrderLog> workOrderLogs= query.asList();
        int limit=0;
        for (WorkOrderLog workOrderLog:workOrderLogs){
            limit++;
            workOrderLog.setSeqNum(limit);
        }
        return workOrderLogs;
    }
}
