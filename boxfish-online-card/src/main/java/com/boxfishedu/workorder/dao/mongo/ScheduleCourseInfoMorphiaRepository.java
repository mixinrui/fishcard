package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ScheduleCourseInfoMorphiaRepository extends BaseMorphiaRepository<ScheduleCourseInfo> {

    public Optional<ScheduleCourseInfo> getById(Long id) {
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        query.criteria("id").equal(id);
        query.limit(1);
        return Optional.ofNullable(query.get());
    }

    public ScheduleCourseInfo queryByWorkId(Long workId) {
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        query.and(query.criteria("workOrderId").equal(workId));
        return query.get();
    }

    public ScheduleCourseInfo queryByScheduleId(Long scheduleId) {
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        query.and(query.criteria("scheduleId").equal(scheduleId));
        return query.get();
    }

    public List<ScheduleCourseInfo> queryByCourseId(String courseId){
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        query.and(query.criteria("courseId").equal(courseId));
        return query.asList();
    }

    public ScheduleCourseInfo queryByCourseIdAndScheduleType(String courseId,String scheduleType){
        Query<ScheduleCourseInfo> query = datastore.createQuery(ScheduleCourseInfo.class);
        query.and(
                query.criteria("courseId").equal(courseId),
                query.criteria("scheduleType").equal(scheduleType)
        );
        return query.get();
    }
}
