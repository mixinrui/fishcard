package com.boxfishedu.card.comment.manage.entity.jpa;

import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 16/9/2.
 */
public interface CommentCardJpaRepository extends JpaRepository<CommentCard, Long> {

    @Query(value = "select count(c) from CommentCard c where c.studentAskTime between ?1 and ?2 and c.status<=300")
    Integer findNoAnswerCountByAskTime(Date from, Date to);

    @Query(value = "select count(c) from CommentCard c where c.studentAskTime<=?1 and c.status<=300")
    Integer findNoAnswerCountByAskTime(Date from);

    @Query(value = "select c from CommentCard c where c.status=300 and c.teacherId=?1")
    List<CommentCard> findNoAnswerCommentCardByTeacherId(Long teacherId);
}
