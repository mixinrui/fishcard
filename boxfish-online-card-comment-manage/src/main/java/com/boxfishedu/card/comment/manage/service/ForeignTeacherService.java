package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.mysql.ToTeacherStudentForm;

/**
 * Created by ansel on 16/9/2.
 */

public interface ForeignTeacherService{
    public JsonResultModel freezeTeacherId(Long teacherId);

    public JsonResultModel unfreezeTeacherId(Long teacherId);

    public JsonResultModel getTeacherList(ToTeacherStudentForm toTeacherStudentForm);
}
