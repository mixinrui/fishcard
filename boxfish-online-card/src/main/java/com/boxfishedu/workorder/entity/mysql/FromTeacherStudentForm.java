package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * Created by ansel on 16/7/22.
 */
@Data
public class FromTeacherStudentForm {
    Long fishCardId;
    Long teacherId;
    String teacherName;
}
