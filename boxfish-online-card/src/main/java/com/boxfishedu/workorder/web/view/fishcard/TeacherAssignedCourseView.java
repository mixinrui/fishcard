package com.boxfishedu.workorder.web.view.fishcard;

import lombok.Data;

/**
 * Created by hucl on 16/7/7.
 */
@Data
public class TeacherAssignedCourseView {
    //当天分配的课程数量
    private Long count;
    private Long teacherId;

    public TeacherAssignedCourseView(Long count,Long teacherId){
        this.count=count;
        this.teacherId=teacherId;
    }
}
