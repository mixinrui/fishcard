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
    @Id
    @GeneratedValue
    private Long id;


    private Long  studentId;
    private String studentImg ; //学生头像url
    private Date applyTime  ; //申请时间
    private Long   workOrderId ;//鱼卡id
    private Long  courseScheleId ;// 课程id
    private Integer applyStatus   ;// '申请状态  0 不匹配  1 匹配  2 无时间片待匹配
    private Date  createTime  ;
    private Date  updateTime ;
    private Integer applySchema ; // '0:自由;1:指定',
    private Long  teacherId ;     //  指定教师ID


    @Transient
    private Integer courseNum;

    @Transient
    private Integer timeSlotId;


    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;



}
