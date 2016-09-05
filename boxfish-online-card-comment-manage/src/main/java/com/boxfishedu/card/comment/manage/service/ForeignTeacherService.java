package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.form.ToTeacherStudentForm;
import org.springframework.data.domain.Pageable;

/**
 * Created by ansel on 16/9/2.
 */

public interface ForeignTeacherService{
    public JsonResultModel freezeTeacherId(Long teacherId);

    public JsonResultModel unfreezeTeacherId(Long teacherId);

    public JsonResultModel getTeacherList(ToTeacherStudentForm toTeacherStudentForm, Pageable pageable);
}
