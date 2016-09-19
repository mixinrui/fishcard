package com.boxfishedu.workorder.service.absencendeal;

import com.boxfishedu.workorder.dao.mongo.ContinousAbsenceMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ContinousAbsenceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/9/17.
 */
@Component
public class AbsenceDealService {
    @Autowired
    private ContinousAbsenceMorphiaRepository continousAbsenceMorphiaRepository;

    public List<ContinousAbsenceRecord> queryByStudentId(Long studentId) {
        return continousAbsenceMorphiaRepository.queryByStudentId(studentId);
    }

    public ContinousAbsenceRecord queryByStudentIdAndComboType(Long studentId, String comboType) {
        return continousAbsenceMorphiaRepository.queryByStudentIdAndComboType(studentId, comboType);
    }

    public List<ContinousAbsenceRecord> queryByComboTypeAndContinusAbsenceNum(String comboType, Integer continusAbsenceNum) {
        return continousAbsenceMorphiaRepository.queryByComboTypeAndContinusAbsenceNum(comboType, continusAbsenceNum);
    }

    public void updateCourseAbsenceNum(ContinousAbsenceRecord newContinousAbsenceRecord) {
        continousAbsenceMorphiaRepository.updateCourseAbsenceNum(newContinousAbsenceRecord);
    }

    public void save(ContinousAbsenceRecord newContinousAbsenceRecord) {
        continousAbsenceMorphiaRepository.save(newContinousAbsenceRecord);
    }
}
