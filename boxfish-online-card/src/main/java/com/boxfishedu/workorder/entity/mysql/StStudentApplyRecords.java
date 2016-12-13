package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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
    private Integer applyStatus   ;// '申请状态   10 申请成功  20 申请失败  ',
    private Date  createTime  ;
    private Date  updateTime ;
    private Integer applySchema ; // '0:自由;1:指定',
    private Long  teacherId ;     //  指定教师ID
}
