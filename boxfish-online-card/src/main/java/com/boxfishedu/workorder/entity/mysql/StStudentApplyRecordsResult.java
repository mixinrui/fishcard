package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 指定老师上课申请表
 * Created by jiaozijun on 16/12/13.
 */

@Data
public class StStudentApplyRecordsResult {

//,StStudentApplyRecords.ReadStatus isRead
    public StStudentApplyRecordsResult(Long studentId,Date applyTime,Long teacherId,StStudentApplyRecords.ReadStatus isRead,Long courseNum){
        this.studentId = studentId;
        this.applyTime =applyTime;
        this.teacherId = teacherId;
        this.courseNum =courseNum;
        this.isRead = isRead;
    }

    private Long  studentId;
    private Date applyTime  ; //申请时间
    private Date systemTime; //系统时间
    private String studentName;// 学生姓名
    private Long  teacherId;     //  指定教师ID
    private Long courseNum;

    private String studentImg ; //学生头像url
    private StStudentApplyRecords.ReadStatus isRead;

}
