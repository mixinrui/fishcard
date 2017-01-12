package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.CommentCardForm;
import com.boxfishedu.workorder.entity.mysql.FromTeacherStudentForm;
import com.boxfishedu.workorder.entity.mysql.UpdatePicturesForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Created by ansel on 16/7/18.
 */
public interface ForeignTeacherCommentCardService {

    public CommentCard foreignTeacherCommentCardAdd(CommentCardForm commentCardForm, Long userId, String access_token);

    public void foreignTeacherCommentUpdateAnswer(FromTeacherStudentForm fromTeacherStudentForm);

    public Map foreignTeacherCommentQuery(Pageable pageable, Long studentId);

    public CommentCard foreignTeacherCommentDetailQuery(Long id,Long userId);

    public void foreignTeacherCommentUnAnswer();

    public void foreignTeacherCommentUnAnswer2();

    public void foreignUndistributedTeacherCommentCards();

    public CommentCard testTeacherComment(CommentCardForm commentCardForm,Long userId,String access_token);

    public Page<CommentCard> testQueryAll(Pageable pageable);

    public String getUserPicture(String access_token);

    public JsonResultModel pushInfoToStudentAndTeacher(Long userId, String title, String type);

    public JsonResultModel countStudentUnreadCommentCards(Long userId);

    public JsonResultModel countTeacherUnreadCommentCards(Long userId);

    public void updateCommentCardsPictures(UpdatePicturesForm updatePicturesForm);

    public void forceToChangeTeacher(Long fromTeacherId,Long toTeacherId);

    void notifyExpireCommentCards();
}
