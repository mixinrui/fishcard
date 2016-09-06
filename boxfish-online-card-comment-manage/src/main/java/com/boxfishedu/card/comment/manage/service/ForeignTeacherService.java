package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
import org.springframework.data.domain.Pageable;

/**
 * Created by ansel on 16/9/2.
 */

public interface ForeignTeacherService{
    public void freezeTeacherId(Long teacherId);

    public void unfreezeTeacherId(Long teacherId);

    public JsonResultModel getTeacherOperations(Long teacherId);

    public JsonResultModel getTeacherTimes(Long teacherId);

    public JsonResultModel getTeacherList(Pageable pageable,TeacherForm teacherForm);

    JsonResultModel commentTeacherPage(Pageable pageable, TeacherForm teacherForm);

    public JsonResultModel getUncommentTeacherList(Pageable pageable,TeacherForm teacherForm);
}
