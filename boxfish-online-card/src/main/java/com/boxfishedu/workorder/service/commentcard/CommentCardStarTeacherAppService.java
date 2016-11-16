package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.workorder.dao.jpa.CommentCardStarJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCardStar;
import com.boxfishedu.workorder.entity.mysql.CommentCardStarForm;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by ansel on 16/11/12.
 */
@Service
public class CommentCardStarTeacherAppService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CommentCardStarJpaRepository commentCardStarJpaRepository;

    @Autowired
    private CommentCardSDK commentCardSDK;

    public CommentCardStar saveTeacher2StudentStar(CommentCardStarForm commentCardStarForm, Long teacherId){
        CommentCardStar commentCardStar = new CommentCardStar(commentCardStarForm.getCommentCardId(),
                commentCardStarForm.getStudentId(),teacherId,commentCardStarForm.getStarLevel());
        logger.info("###saveTeacher2StudentStar 保存学生获得的积分### {}", commentCardStar);
        CommentCardStar newCommentCardStar = commentCardStarJpaRepository.save(commentCardStar);

        //TODO:调用积分接口,给学生增加积分
        commentCardSDK.addScore2Student();
        return newCommentCardStar;
    }
}
