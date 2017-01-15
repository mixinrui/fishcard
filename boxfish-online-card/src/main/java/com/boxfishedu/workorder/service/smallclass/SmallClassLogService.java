package com.boxfishedu.workorder.service.smallclass;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.bean.RoleEnum;
import com.boxfishedu.workorder.dao.mongo.SmallClassLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.SmallClassLog;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by hucl on 17/1/14.
 */
@Service
public class SmallClassLogService {
    @Autowired
    private SmallClassLogMorphiaRepository smallClassLogMorphiaRepository;

    public void recordLog(
            SmallClass smallClass,
            int status, Long userId,
            RoleEnum roleEnum,
            String desc) {
        SmallClassLog smallClassLog = new SmallClassLog();
        smallClassLog.setRole(roleEnum.name());
        smallClassLog.setStatus(status);
        smallClassLog.setDesc(desc);
        smallClassLog.setReportTime(smallClass.getReportTime());
        switch (roleEnum) {
            case STUDENT:
                smallClassLog.setStudentId(userId);
                break;
            default:
                smallClassLog.setTeacherId(userId);

        }
        smallClassLogMorphiaRepository.save(smallClassLog);
    }

    public void recordStudentLog(SmallClass smallClass) {
        this.recordStudentLog(smallClass, smallClass.getStatusReporter());
    }

    public void recordStudentLog(SmallClass smallClass,String desc){
        this.recordLog(smallClass, smallClass.getStatus(), smallClass.getStatusReporter(), RoleEnum.STUDENT, desc);
    }

    public void recordStudentLog(
            SmallClass smallClass,
            int status, Long userId,
            String desc) {
        this.recordLog(smallClass, status, userId, RoleEnum.STUDENT, desc);
    }

    public void recordStudentLog(
            SmallClass smallClass,
            Long userId) {
        this.recordStudentLog(smallClass
                , smallClass.getStatus()
                , userId
                , PublicClassInfoStatusEnum.getByCode(smallClass.getStatus()).getDesc());
    }

    public void recordTeacherLog(SmallClass smallClass) {
        this.recordStudentLog(smallClass, smallClass.getStatusReporter());
    }

    public void recordTeacherLog(
            SmallClass smallClass,
            int status, Long userId,
            String desc) {
        this.recordLog(smallClass, status, userId, RoleEnum.TEACHER, desc);
    }

    public void recordTeacherLog(
            SmallClass smallClass,
            Long userId) {
        this.recordTeacherLog(smallClass
                , smallClass.getStatus()
                , userId
                , PublicClassInfoStatusEnum.getByCode(smallClass.getStatus()).getDesc());
    }
}
