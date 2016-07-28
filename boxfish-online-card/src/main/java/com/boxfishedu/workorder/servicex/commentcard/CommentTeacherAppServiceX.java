package com.boxfishedu.workorder.servicex.commentcard;

import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.requester.TeacherStudentCommentCardRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.commentcard.CommentCardLogService;
import com.boxfishedu.workorder.service.commentcard.CommentCardTeacherAppService;
import com.boxfishedu.workorder.web.param.CommentCardSubmitParam;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import com.boxfishedu.workorder.web.param.commentcard.TeacherReadMsgParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;

/**
 * Created by hucl on 16/7/20.
 */
@Service
public class CommentTeacherAppServiceX {

    @Autowired
    private CommentCardTeacherAppService commentCardTeacherAppService;

    @Autowired
    private CommentCardLogService commentCardLogService;

    @Autowired
    private TeacherStudentCommentCardRequester teacherStudentCommentCardRequester;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public Page<CommentCard> findByTeacherIdOrderByAssignTeacherTimeDesc(Long teacherId, Pageable pageable){
        return commentCardTeacherAppService.findByTeacherIdOrderByAssignTeacherTimeDesc(teacherId,pageable);
    }

    public CommentCard findById(Long cardId){
        return commentCardTeacherAppService.findById(cardId);
    }

    public void markTeacherRead(TeacherReadMsgParam teacherReadMsgParam){
        CommentCard commentCard=this.findById(teacherReadMsgParam.getCommentCardId());
        if(null==commentCard){
            logger.error("不存在对应的点评卡,点评卡id[{}]",teacherReadMsgParam.getCommentCardId());
            throw new BusinessException("不存在对应的点评卡");
        }
        commentCard.setTeacherReadFlag(1);
        commentCardTeacherAppService.save(commentCard);
    }

    public void submitComment(@RequestBody CommentCardSubmitParam commentCardSubmitParam){
        CommentCard commentCard=commentCardTeacherAppService.findById(commentCardSubmitParam.getCommentcardId());
        if(null==commentCard){
            throw new BusinessException("不存在对应的点评卡");
        }
        commentCard.setStudentReadFlag(0);
        commentCard.setStatus(CommentCardStatus.ANSWERED.getCode());
        commentCard.setUpdateTime(new Date());
        commentCard.setAnswerVideoPath(commentCardSubmitParam.getVideoPath());
        commentCard.setTeacherAnswerTime(new Date());
        commentCardTeacherAppService.save(commentCard);
        commentCardLogService.saveCommentCardLog(commentCard);
    }
}
