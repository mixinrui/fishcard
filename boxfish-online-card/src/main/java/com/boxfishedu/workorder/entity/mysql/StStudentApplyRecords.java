package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Date;

/**
 * 指定老师上课申请表
 * Created by jiaozijun on 16/12/13.
 */

@Table
@Entity
@Data
public class StStudentApplyRecords {
    public enum ApplyStatus{
        pending,
        agree
    }
    public enum ReadStatus{
        yes,
        no
    }
    @Id
    @GeneratedValue
    private Long id;
    private Long  studentId;
//    @Transient
//    private String studentImg ; //学生头像url
    private Date applyTime  ; //申请时间
    private Long   workOrderId ;//鱼卡id
    private Long  courseScheleId;// 课程id
    private ApplyStatus applyStatus;// '申请状态 0 待接受  1 已接受
    private Date  createTime ;
    private Date  updateTime;
    private Long  teacherId;     //  指定教师ID
    private ReadStatus isRead;
//    @Transient
//    private Integer courseNum;
//    @Transient
//    private Integer timeSlotId;
//    @Transient
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date startTime;
//    @Transient
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date endTime;



}
