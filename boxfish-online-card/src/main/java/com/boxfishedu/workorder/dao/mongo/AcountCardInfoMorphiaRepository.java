package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/9/24.
 */
@Component
public class AcountCardInfoMorphiaRepository {
    @Autowired
    protected Datastore datastore;

    public AccountCardInfo queryByStudentId(Long studentId){
        Query<AccountCardInfo> query = datastore.createQuery(AccountCardInfo.class);
        query.and(query.criteria("studentId").equal(studentId));
        return query.get();
    }

    public void save(AccountCardInfo accountCardInfo) {
        datastore.save(accountCardInfo);
    }
}
