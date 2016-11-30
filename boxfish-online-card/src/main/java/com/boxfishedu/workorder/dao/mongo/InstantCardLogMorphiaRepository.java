package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
import com.boxfishedu.workorder.entity.mongo.InstantCardLog;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.LogManager;

@Component
public class InstantCardLogMorphiaRepository extends BaseMorphiaRepository<InstantCardLog>{
    @Autowired
    private LogPoolManager logPoolManager;

    public void saveInstantLog(InstantClassCard instantClassCard,List<Long> pullTeacherIds,String desc){
        logPoolManager.execute(new Thread(()->{
            InstantCardLog instantCardLog=new InstantCardLog();
            instantCardLog.setStudentId(instantClassCard.getStudentId());
            instantCardLog.setPullTeacherIds(pullTeacherIds);
            instantCardLog.setInstantCardId(instantClassCard.getId());
            instantCardLog.setDesc("获取推荐教师列表");
            instantCardLog.setCreateTime(new Date());
            if(!Objects.isNull(instantClassCard.getWorkorderId())) {
                instantCardLog.setWorkOrderId(instantClassCard.getWorkorderId());
            }
            datastore.save(instantCardLog);
        }));
    }

    public List<InstantCardLog> findByInstantCardId(Long instantCardId){
        Query<InstantCardLog> query = datastore.createQuery(InstantCardLog.class);
        query.and(query.criteria("instantCardId").equal(instantCardId));
        //倒序
//        query.order("-createTime");
        query.order("createTime");
        return query.asList();
    }

    public List<InstantCardLog> findByInstantStudentId(Long studentId){
        Query<InstantCardLog> query = datastore.createQuery(InstantCardLog.class);
        query.and(query.criteria("studentId").equal(studentId));
        //倒序
//        query.order("-createTime");
        query.order("createTime");
        return query.asList();
    }

}
