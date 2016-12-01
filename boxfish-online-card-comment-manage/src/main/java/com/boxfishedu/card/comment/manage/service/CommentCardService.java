package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.card.comment.manage.entity.dto.CommentCardDto;
import com.boxfishedu.card.comment.manage.entity.dto.CommentCardExcelDto;
import com.boxfishedu.card.comment.manage.entity.dto.CommentCardLogDto;
import com.boxfishedu.card.comment.manage.entity.form.ChangeTeacherForm;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardForm;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by LuoLiBing on 16/9/2.
 */
public interface CommentCardService {

    Page<CommentCardDto> findCommentCardByOptions(CommentCardForm commentCardForm, Pageable pageable);

    CommentCardDto findCommentCardById(Long id);

    void changeTeacher(Long id, Long teacherId);

    void changeTeacherBatch(Long[] ids, Long teacherId);

    void changeTeacher(CommentCard commentCard, Long teacherId);

    Integer[] findNoAnswerCountsByAskTime();

    CommentCardLogDto findCommentCardLog(Long id);

    ChangeTeacherForm changeCommentCardToInnerTeacher(Long teacherId);

    CommentCardExcelDto exportExcel(CommentCardForm commentCardForm, Pageable pageable);
}
