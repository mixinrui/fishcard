package com.boxfishedu.workorder.service.smallclass;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.bean.RoleEnum;
import com.boxfishedu.workorder.dao.mongo.SmallClassLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.SmallClassLog;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.web.param.SmallClassParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
        smallClassLog.setSmallClassId(smallClass.getId());
        smallClassLog.setReportTime(smallClass.getReportTime());
        smallClassLog.setCreateTime(new Date());

        switch (roleEnum) {
            case STUDENT:
                smallClassLog.setStudentId(userId);
                break;
            case TEACHER:
                smallClassLog.setTeacherId(userId);
                break;
            default:
                break;

        }
        smallClassLogMorphiaRepository.save(smallClassLog);
    }

    public void recordStudentLog(SmallClass smallClass) {
        this.recordStudentLog(smallClass, smallClass.getStatusReporter());
    }

    public void recordStudentLog(SmallClass smallClass, String desc) {
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
        this.recordTeacherLog(smallClass, smallClass.getStatusReporter());
    }

    public void recordTeacherLog(
            SmallClass smallClass,
            int status, Long userId,
            String desc) {
        this.recordLog(smallClass, status, userId, RoleEnum.TEACHER, desc);
    }

    public void recordSystemLog(SmallClass smallClass, int status, String desc) {
        this.recordLog(smallClass, status, 0l, RoleEnum.SYSTEM, desc);
    }

    public void recordTeacherLog(
            SmallClass smallClass,
            Long userId) {
        this.recordTeacherLog(smallClass
                , smallClass.getStatus()
                , userId
                , PublicClassInfoStatusEnum.getByCode(smallClass.getStatus()).getDesc());
    }

    //学生动作
    public boolean studentActed(Long studentId, Long smallClassId) {
        List<SmallClassLog> smallClassLogs
                = smallClassLogMorphiaRepository.queryByStudentAndSmallClass(studentId, smallClassId);

        for (SmallClassLog smallClassLog : smallClassLogs) {

            if (smallClassLog.getStatus() > PublicClassInfoStatusEnum.STUDENT_ENTER.getCode()
                    && smallClassLog.getStatus() <= PublicClassInfoStatusEnum.STUDENT_QUIT.getCode()) {
                return true;
            }
        }
        return false;

    }

    //教师动作
    public boolean teacherActed(Long teacherId, Long smallClassId) {
        List<SmallClassLog> smallClassLogs
                = smallClassLogMorphiaRepository.queryByTeacherAndSmallClass(teacherId, smallClassId);

        for (SmallClassLog smallClassLog : smallClassLogs) {

            if (smallClassLog.getStatus() > PublicClassInfoStatusEnum.STUDENT_QUIT.getCode()) {
                return true;
            }
        }
        return false;
    }
}
