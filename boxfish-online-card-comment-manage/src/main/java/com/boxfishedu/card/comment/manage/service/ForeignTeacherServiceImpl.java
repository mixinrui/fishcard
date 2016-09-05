package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.form.ToTeacherStudentForm;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Created by ansel on 16/9/2.
 */
@Service
public class ForeignTeacherServiceImpl implements ForeignTeacherService{
    @Override
    public JsonResultModel freezeTeacherId(Long teacherId) {
        return null;
    }

    @Override
    public JsonResultModel unfreezeTeacherId(Long teacherId) {
        return null;
    }

    @Override
    public JsonResultModel getTeacherList(ToTeacherStudentForm toTeacherStudentForm, Pageable pageable) {
        return null;
    }
}
