package com.boxfishedu.workorder.servicex.commentcard;

import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.requester.TeacherStudentCommentCardRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.commentcard.CommentCardTeacherAppService;
import com.boxfishedu.workorder.web.param.CommentCardSubmitParam;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;

/**
 * Created by hucl on 16/7/20.
 */
@Service
public class StudentComment2TeacherServiceX {

    @Autowired
    private CommentCardTeacherAppService commentCardTeacherAppService;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private TeacherStudentCommentCardRequester teacherStudentCommentCardRequester;

    private Logger logger=LoggerFactory.getLogger(this.getClass());

    public CommentCard studentComment2Teacher(Student2TeacherCommentParam student2TeacherCommentParam){
        CommentCard commentCard=commentCardTeacherAppService.findById(student2TeacherCommentParam.getCommentCardId());
        if(null==commentCard){
            throw new BusinessException("不存在对应的点评卡");
        }
        if(!CollectionUtils.isEmpty(student2TeacherCommentParam.getForGoodReviews())){
            commentCard.setStudentCommentGoodTagCode(student2TeacherCommentParam.getForGoodReviews());
            teacherStudentCommentCardRequester.sendStudentComment2Teacher(student2TeacherCommentParam);
        }
        else if(!CollectionUtils.isEmpty(student2TeacherCommentParam.getForBadReviews())){
            commentCard.setStudentCommentBadTagCode(student2TeacherCommentParam.getForBadReviews());
            teacherStudentCommentCardRequester.sendStudentComment2Teacher(student2TeacherCommentParam);
        }
        else{
            logger.info("@studentComment2Teacher获取到的评价为空,不做处理");
        }
        Date dateNow = new Date();
        commentCard.setStudentCommentTeacherTime(dateNow);
        commentCard.setStatus(CommentCardStatus.STUDENT_COMMENT_TO_TEACHER.getCode());
        commentCard.setTeacherReadFlag(0);
        return commentCardTeacherAppService.save(commentCard);
    }
}
