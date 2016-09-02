package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.card.comment.manage.entity.dto.CommentCardDto;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by LuoLiBing on 16/9/2.
 */
public interface CommentCardService {

    Page<CommentCardDto> findCommentCardByOptions(CommentCardForm commentCardForm, Pageable pageable);

    CommentCardDto findCommentCardById(Long id);

    void changeTeacher(Long id, Long teacherId);
}
