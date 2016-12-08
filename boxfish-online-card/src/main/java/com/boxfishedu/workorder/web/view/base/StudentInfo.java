package com.boxfishedu.workorder.web.view.base;

import lombok.Data;

/**
 * Created by jiaozijun on 16/12/7.
 */
@Data
public class StudentInfo {

    private String mobile;

    private String username;

    private String schoolName;

    private String startTime;

    private Long studentId;

    private Long fishcardId;

    @Override
    public StudentInfo clone() {
        StudentInfo prototypeClass = null;
        try {
            prototypeClass = (StudentInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("克隆对象失败");
        }
        return prototypeClass;
    }
}