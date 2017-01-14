package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.SmallClassLog;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.web.param.SmallClassParam;
import org.jboss.netty.util.internal.StringUtil;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Created by jiaozijun on 17/1/12.
 */

@Component
public class SmallClassLogMorphiaRepository extends BaseMorphiaRepository<SmallClassLog> {

    public Long queryCount(SmallClassParam smallClassParam){
        Query<SmallClassLog>  query = this.queryCondition(smallClassParam);
        return query.countAll();
    }

    public Query<SmallClassLog> queryCondition(SmallClassParam smallClassParam) {
        Query<SmallClassLog> query = datastore.createQuery(SmallClassLog.class);

        //公开课id
        if(null != smallClassParam.getId()){
            query.and(query.criteria("smallClassId").equal(smallClassParam.getId()));
        }
        if(null != smallClassParam.getTeacherId()){
            query.and(query.criteria("teacherId").equal(smallClassParam.getTeacherId()));
        }

        if(null != smallClassParam.getStudentId()){
            query.and(query.criteria("studentId").equal(smallClassParam.getStudentId()));
        }

        //创建时间排序
        if(!StringUtils.isEmpty(smallClassParam.getCreateTimeSort()) ){
            if("asc".equals(smallClassParam.getCreateTimeSort())){
                query.order("createTime");
            }else {
                query.order("-createTime");
            }

        }


        query.offset(smallClassParam.getOffSet( )).limit(smallClassParam.getLimit( ));
        return query;
    }
}
