package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.FromTeacherStudentForm;
import com.boxfishedu.workorder.entity.mysql.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by ansel on 16/7/18.
 */
public interface ForeignTeacherCommentCardService {

    public CommentCard foreignTeacherCommentCardAdd(CommentCard commentCard);

    public void foreignTeacherCommentUpdateQuestion(CommentCard commentCard);

    public void foreignTeacherCommentUpdateAnswer(FromTeacherStudentForm fromTeacherStudentForm);

    public void foreignTeacherCommentUpdateStatusRead(CommentCard commentCard);

    public Page<CommentCard> foreignTeacherCommentQuery(Pageable pageable,Long studentId);

    public CommentCard foreignTeacherCommentDetailQuery(Long id, Long studentId);

    public void foreignTeacherCommentUnAnswer();

    public void foreignTeacherCommentUnAnswer2();

    public JsonResultModel updateCommentAmount(Service service);

    public CommentCard getAssignTeacherCount(Long id);
}
