package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.dto.CommentCountSetLog;
import com.boxfishedu.card.comment.manage.entity.dto.CommentTeacherInfo;
import com.boxfishedu.card.comment.manage.entity.dto.NoCommentTeacherInfoDto;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by ansel on 16/9/2.
 */

public interface ForeignTeacherService{
    void freezeTeacherId(Long teacherId);

    void unfreezeTeacherId(Long teacherId);

    JsonResultModel getTeacherOperations(Long teacherId);

    Page<CommentTeacherInfo> commentTeacherPage(Pageable pageable, TeacherForm teacherForm);

    Page<NoCommentTeacherInfoDto> uncommentTeacherPage(Pageable pageable, TeacherForm teacherForm);

    Page<CommentCountSetLog> commentCountSetLogPage(Pageable pageable, Long teacherId);
}
