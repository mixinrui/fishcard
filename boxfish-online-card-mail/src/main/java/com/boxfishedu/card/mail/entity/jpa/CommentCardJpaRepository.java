package com.boxfishedu.card.mail.entity.jpa;

import com.boxfishedu.card.mail.entity.CommentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 16/9/1.
 */
public interface CommentCardJpaRepository extends JpaRepository<CommentCard, Long> {

    @Query(value = "select c from CommentCard c where c.status<=300 and c.studentAskTime<?1 order by c.studentAskTime asc")
    List<CommentCard> findNotAnswerOver12Hours(Date startTime);
}
