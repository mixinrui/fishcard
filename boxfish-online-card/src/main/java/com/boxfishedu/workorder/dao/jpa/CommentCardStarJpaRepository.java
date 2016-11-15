package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CommentCardStar;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by ansel on 16/11/12.
 */
public interface CommentCardStarJpaRepository extends JpaRepository<CommentCardStar,Long>{

    public CommentCardStar findByCommentCardId(Long commentCardId);

}
