package com.boxfishedu.card.comment.manage.entity.jpa;

import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * Created by LuoLiBing on 16/9/2.
 */
public interface CommentCardJpaRepository extends JpaRepository<CommentCard, Long> {

    @Query(value = "select count(c) from CommentCard c where c.studentAskTime between ?1 and ?2 and c.status<=300")
    Integer findNoAnswerCountByAskTime(Date from, Date to);

    @Modifying
    @Query(value = "update CommentCard c set c.teacherStatus = 500 where c.teacherId =?1")
    void freezeTeacherId(Long teacherId);

    @Modifying
    @Query(value = "update CommentCard c set c.teacherStatus = 200 where c.teacherId =?1")
    void unfreezeTeacherId(Long teacherId);
}
