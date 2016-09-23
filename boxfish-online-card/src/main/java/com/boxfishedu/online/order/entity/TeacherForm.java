package com.boxfishedu.online.order.entity;

import lombok.Data;

/**
 * 教师信息
 * Created by jiaozijun on 16/7/11.
 */
@Data
public class TeacherForm {

    private  Long teacherId;

    /** 1 中教  2 外教 **/
    private int teacherType;
    /** 课程类型   "courseIds" : [ "EXAMINATION", "FUNCTION", "READING", "CONVERSATION", "PHONICS" ]   **/
    private String [] courseIds;

}
