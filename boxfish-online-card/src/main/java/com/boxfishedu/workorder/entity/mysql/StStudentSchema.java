package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 学生上课模式
 * Created by jiaozijun on 16/12/13.
 */

@Table
@Entity
@Data
public class StStudentSchema {

    public enum StSchema{
        un_assgin,
        assgin
    }
    @Id
    @GeneratedValue
    private Long id;

    private Long  studentId;
    private Long  teacherId ;     //  指定教师ID
    private StSchema stSchema;//  '0自由模式1指定模式'
    private Date createTime  ;
    private Date  updateTime ;
}
