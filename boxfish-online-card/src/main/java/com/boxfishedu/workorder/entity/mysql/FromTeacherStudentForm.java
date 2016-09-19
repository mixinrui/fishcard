package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * Created by ansel on 16/7/22.
 */
@Data
public class FromTeacherStudentForm {
    Long fishCardId;
    Long teacherId;
    String teacherFirstName;
    String teacherLastName;
    String teacherName;

    private void setTeahcerName(String teacherName){
        this.teacherName = (teacherFirstName !=null ? teacherFirstName.trim() : "")+ " "+
                (teacherLastName !=null ? teacherLastName.trim() : "");
    }
}
