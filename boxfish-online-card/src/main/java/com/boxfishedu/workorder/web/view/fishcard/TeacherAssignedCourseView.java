package com.boxfishedu.workorder.web.view.fishcard;

import lombok.Data;

/**
 * Created by hucl on 16/7/7.
 */
@Data
public class TeacherAssignedCourseView {
    private Long teacherId;
    //当天分配的课程数量
    private Long count;
}
