package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.entity.mongo.OnlineAccountSet;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/10/25.
 */
@Component
public class OnlineAccountSetMorphiaRepository {
    @Autowired
    protected Datastore datastore;

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    public OnlineAccountSet queryByStudentId(Long studentId){
        Query<OnlineAccountSet> query = datastore.createQuery(OnlineAccountSet.class);
        query.and(query.criteria("studentId").equal(studentId));
        return query.get();
    }

    public void save(OnlineAccountSet onlineAccountSet) {
        datastore.save(onlineAccountSet);
    }

    public Long count(){
        return datastore.getCount(OnlineAccountSet.class);
    }

    public List<OnlineAccountSet> getAll(){
        Query<OnlineAccountSet> query = datastore.createQuery(OnlineAccountSet.class);
        query.and(query.criteria("studentId").notEqual(1));
        return query.asList();
    }

    public void add(Long studentId) {
        if(null==this.queryByStudentId(studentId)) {
            OnlineAccountSet onlineAccountSet=new OnlineAccountSet();
            onlineAccountSet.setStudentId(studentId);
            onlineAccountSet.setUpdateTime(new Date());
            logger.debug("@OnlineAccountSetMorphiaRepository#add#newuser[{}]",studentId);
            datastore.save(onlineAccountSet);
            return;
        }
        logger.debug("@OnlineAccountSetMorphiaRepository#add#olduser[{}]",studentId);
    }

}
