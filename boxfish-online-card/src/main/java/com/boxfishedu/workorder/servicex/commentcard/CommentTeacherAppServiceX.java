package com.boxfishedu.workorder.servicex.commentcard;

import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.service.commentcard.CommentCardTeacherAppService;
import com.boxfishedu.workorder.web.param.CommentCardSubmitParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by hucl on 16/7/20.
 */
@Service
public class CommentTeacherAppServiceX {

    @Autowired
    private CommentCardTeacherAppService commentCardTeacherAppService;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public Page<CommentCard> findByTeacherIdOrderByAssignTeacherTimeDesc(Long teacherId, Pageable pageable){
        return commentCardTeacherAppService.findByTeacherIdOrderByAssignTeacherTimeDesc(teacherId,pageable);
    }

    public CommentCard findById(Long cardId){
        return commentCardTeacherAppService.findById(cardId);
    }

    public void submitComment(CommentCardSubmitParam commentCardSubmitParam){
        CommentCard commentCard=commentCardTeacherAppService.findById(commentCardSubmitParam.getCommentcardId());
        if(null==commentCard){
            throw new BusinessException("不存在对应的点评卡");
        }
        commentCard.setStatus(CommentCardStatus.ANSWERED.getCode());
        commentCard.setUpdateTime(new Date());
        commentCard.setAnswerVideoPath(commentCardSubmitParam.getVideoPath());
        commentCard.setTeacherAnswerTime(new Date());
        commentCardTeacherAppService.save(commentCard);
    }
}
