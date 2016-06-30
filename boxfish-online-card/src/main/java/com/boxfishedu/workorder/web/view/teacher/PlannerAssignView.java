/*
* Copyright (c) 2015 boxfish.cn. All Rights Reserved.
*/
package com.boxfishedu.workorder.web.view.teacher;


import lombok.Data;
import org.jdto.annotation.Source;

/**
 * Created with Intellij IDEA
 * Author: boxfish
 * Date: 16/3/17
 * Time: 19:26
 */
@Data
public class PlannerAssignView {

    @Source(value = "teacherId")
    private Long plannerId;
    private String name;
    private Long studentId;

    public PlannerAssignView() {
    }

    public PlannerAssignView(Long plannerId, String name, Long studentId) {
        this.plannerId = plannerId;
        this.name = name;
        this.studentId = studentId;
    }

    @Override
    public String toString() {
        return "PlannerAssignView{" +
                "plannerId=" + plannerId +
                ", name='" + name + '\'' +
                ", studentId=" + studentId +
                '}';
    }


}
