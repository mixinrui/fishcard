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
        assgin;
        public static StSchema getEnum(int value) {
            for (StSchema stSchema : StSchema.values()) {
                if (stSchema.ordinal() == value) {
                    return stSchema;
                }
            }
            return null;
        }
    }
    public enum CourseType{
        unknown,
        chinese,
        foreign;
        public static CourseType getEnum(int value) {
            for (CourseType courseType : CourseType.values()) {
                if (courseType.ordinal() == value) {
                    return courseType;
                }
            }
            return null;
        }
    }
    @Id
    @GeneratedValue
    private Long id;

    private Long  studentId;
    private Long  teacherId ;     //  指定教师ID
    private StSchema stSchema;//  '0自由模式1指定模式'
    private CourseType skuId;
    private Date createTime  ;
    private Date  updateTime ;
}
