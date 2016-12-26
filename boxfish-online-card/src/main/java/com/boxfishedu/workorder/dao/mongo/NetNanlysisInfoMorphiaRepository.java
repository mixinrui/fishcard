package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.AccountCourseEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.entity.mongo.NetPingAnalysisInfo;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by hucl on 16/9/24.
 */
@Component
public class NetNanlysisInfoMorphiaRepository {
    @Autowired
    protected Datastore datastore;

    private final org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());


    public NetPingAnalysisInfo queryByCardIdAndUserId(Long cardId,Long userId){
        Query<NetPingAnalysisInfo> query = datastore.createQuery(NetPingAnalysisInfo.class);
        query.and(query.criteria("userId").equal(userId));
        query.and(query.criteria("cardId").equal(cardId));
        return query.get();
    }

    public void save(NetPingAnalysisInfo netPingAnalysisInfo) {
        datastore.save(netPingAnalysisInfo);
    }
}
