package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by ansel on 16/7/18.
 */
public interface ForeignTeacherCommentCardService {

    public JsonResultModel foreignTeacherCommentCardAdd(CommentCard commentCardForm);

    public JsonResultModel foreignTeacherCommentCardUpdate(CommentCard commentCardForm);

    public Page<CommentCard> foreignTeacherCommentQuery(Pageable pageable,Long studentId);

    public CommentCard foreignTeacherCommentDetailQuery(Long id);

    public List<CommentCard> foreignTeacherCommentUnAnswer();
}
