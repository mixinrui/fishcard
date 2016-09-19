package com.boxfishedu.workorder.service.commentcard;

import lombok.Data;

/**
 * Created by LuoLiBing on 16/9/10.
 */
@Data
public class InnerTeacher {
    private Long teacherId;
    private String teacherFirstName;
    private String teacherLastName;
    private String teacherName;

    public void setTeacherName(String teacherName) {
        this.teacherName = (teacherFirstName == null ? "" : teacherFirstName.trim())
                + " "+ (teacherLastName == null ? "" :teacherLastName.trim());
    }
}
