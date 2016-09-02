package com.boxfishedu.card.comment.manage.entity.jpa;

import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by LuoLiBing on 16/9/2.
 */
public interface CommentCardJpaRepository extends JpaRepository<CommentCard, Long> {
}
