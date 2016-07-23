package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * Created by ansel on 16/7/22.
 */
@Data
public class ToTeacherStudentForm {
    Long fishCardId;
    Long studentId;
    String courseId;

    public static ToTeacherStudentForm getToTeacherStudentForm(CommentCard commentCard){
        ToTeacherStudentForm toTeacherStudentForm = new ToTeacherStudentForm();
        toTeacherStudentForm.setFishCardId(commentCard.getId());
        toTeacherStudentForm.setStudentId(commentCard.getStudentId());
        toTeacherStudentForm.setCourseId(commentCard.getCourseId());
        return toTeacherStudentForm;
    }
}
