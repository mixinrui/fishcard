package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mongo.ContinousAbsenceRecord;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mongo.TrialCourse;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ContinousAbsenceMorphiaRepository extends BaseMorphiaRepository<ContinousAbsenceRecord>{

    public List<ContinousAbsenceRecord> queryByStudentId(Long studentId){
        Query<ContinousAbsenceRecord> query = datastore.createQuery(ContinousAbsenceRecord.class);
        query.and(query.criteria("studentId").equal(studentId));
        return query.asList();
    }

    public ContinousAbsenceRecord queryByStudentIdAndComboType(Long studentId,String comboType){
        Query<ContinousAbsenceRecord> query = datastore.createQuery(ContinousAbsenceRecord.class);
        query.and(query.criteria("studentId").equal(studentId));
        query.and(query.criteria("comboType").equal(comboType));
        return query.get();
    }

    public List<ContinousAbsenceRecord> queryByComboTypeAndContinusAbsenceNum(String comboType,Integer continusAbsenceNum){
        Query<ContinousAbsenceRecord> query = datastore.createQuery(ContinousAbsenceRecord.class);
        query.and(query.criteria("comboType").equal(comboType));
        query.and(query.criteria("continusAbsenceNum").equal(continusAbsenceNum));
        return query.asList();
    }

    public void updateCourseAbsenceNum(ContinousAbsenceRecord newContinousAbsenceRecord) {
        Query<ContinousAbsenceRecord> updateQuery = datastore.createQuery(ContinousAbsenceRecord.class);
        updateQuery.and(updateQuery.criteria("studentId").equal(newContinousAbsenceRecord.getStudentId()));
        updateQuery.and(updateQuery.criteria("comboType").equal(newContinousAbsenceRecord.getComboType()));

        UpdateOperations<ContinousAbsenceRecord> updateOperations = datastore.createUpdateOperations(ContinousAbsenceRecord.class);
        updateOperations.set("studentId", newContinousAbsenceRecord.getStudentId());
        updateOperations.set("comboType", newContinousAbsenceRecord.getComboType());
        updateOperations.set("continusAbsenceNum", newContinousAbsenceRecord.getContinusAbsenceNum());

        UpdateResults updateResults = datastore.updateFirst(updateQuery, updateOperations);
        if (updateResults.getUpdatedCount() < 1) {
            throw new BusinessException("@updateCourseAbsenceNum更新课程信息失败");
        }
    }

}
