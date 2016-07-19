package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.entity.mysql.CommentCard;

import java.util.List;

/**
 * Created by ansel on 16/7/18.
 */
public interface ForeignTeacherCommentCardService {
    public JsonResultModel foreignTeacherCommentCardAdd(CommentCard commentCardForm);
    public JsonResultModel foreignTeacherCommentCardUpdate(CommentCard commentCardForm);
    public List<CommentCard> foreignTeacherCommentQuery(Long studentId);
}
