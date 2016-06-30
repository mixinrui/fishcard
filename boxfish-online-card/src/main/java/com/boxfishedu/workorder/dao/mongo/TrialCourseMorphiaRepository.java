package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.entity.mongo.TrialCourse;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TrialCourseMorphiaRepository {
    @Autowired
    private Datastore datastore;

    public void save(TrialCourse trialCourse) {
        datastore.save(trialCourse);
    }

    public void save(Iterable<TrialCourse> trialCourses){
        datastore.save(trialCourses);
    }

    public Optional<TrialCourse> getById(Long id) {
        Query<TrialCourse> query = datastore.createQuery(TrialCourse.class);
        query.criteria("id").equal(id);
        query.limit(1);
        return Optional.ofNullable(query.get());
    }

    public List<TrialCourse> queryByCourseId(String courseId){
        Query<TrialCourse> query = datastore.createQuery(TrialCourse.class);
        query.and(query.criteria("courseId").equal(courseId));
        return query.asList();
    }

    public TrialCourse queryByCourseIdAndScheduleType(String courseId,String scheduleType){
        Query<TrialCourse> query = datastore.createQuery(TrialCourse.class);
        query.and(
                query.criteria("courseId").equal(courseId),
                query.criteria("scheduleType").equal(scheduleType)
        );
        return query.get();
    }
}
