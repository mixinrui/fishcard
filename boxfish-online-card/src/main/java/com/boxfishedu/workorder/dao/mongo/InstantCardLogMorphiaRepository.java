package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
import com.boxfishedu.workorder.entity.mongo.InstantCardLog;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            instantCardLog.setDesc("获取推荐教师列表");
            if(!Objects.isNull(instantClassCard.getWorkorderId())) {
                instantCardLog.setWorkOrderId(instantClassCard.getWorkorderId());
                datastore.save(instantCardLog);
            }
        }));

    }

}
