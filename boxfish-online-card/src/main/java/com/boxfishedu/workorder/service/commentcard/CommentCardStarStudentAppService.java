package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.workorder.dao.jpa.CommentCardStarJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCardStar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by ansel on 16/11/12.
 */
@Service
public class CommentCardStarStudentAppService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private CommentCardStarJpaRepository commentCardStarJpaRepository;

    public CommentCardStar getCommentCardStar(Long commentCardId){
        logger.info("###getCommentCardStar### 学生端获取点评星级, cardId = {}", commentCardId);
        return commentCardStarJpaRepository.findByCommentCardId(commentCardId);
    }
}
