package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
import com.boxfishedu.workorder.entity.mongo.InstantCardLog;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.requester.InstantTeacherRequester;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class SmallCardMorphiaRepository extends BaseMorphiaRepository<InstantCardLog> {
    @Autowired
    private LogPoolManager logPoolManager;

    public void saveSmallCardLog(SmallClass smallClass) {
    }

    public List<SmallClass> findBySmallClassId(Long smallClassId) {
        Query<SmallClass> query = datastore.createQuery(SmallClass.class);

        query.order("createTime");
        return query.asList();
    }

    public List<InstantCardLog> findByInstantStudentId(Long studentId) {
        Query<InstantCardLog> query = datastore.createQuery(InstantCardLog.class);
        query.and(query.criteria("studentId").equal(studentId));
        //倒序
//        query.order("-createTime");
        query.order("createTime");
        return query.asList();
    }

}
