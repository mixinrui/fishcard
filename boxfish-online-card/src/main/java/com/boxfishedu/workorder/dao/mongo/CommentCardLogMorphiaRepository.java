package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.CommentCardLog;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentCardLogMorphiaRepository extends BaseMorphiaRepository<CommentCardLog> {
    public List<CommentCardLog> queryByCommentCardId(Long commentCardId) {
        Query<CommentCardLog> query = datastore.createQuery(CommentCardLog.class);
        query.and(query.criteria("commentCardId").equal(commentCardId));
        query.order("createTime");
        List<CommentCardLog> commentCardLogs = query.asList();
        return commentCardLogs;
    }
}
