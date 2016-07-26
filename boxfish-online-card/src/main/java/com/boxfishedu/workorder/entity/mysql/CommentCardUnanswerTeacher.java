package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ansel on 16/7/25.
 */

@Data
@Entity
@Table(name = "comment_card_unanswer_teacher")
public class CommentCardUnanswerTeacher {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "card_id", nullable = true)
    private Long cardId;

    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;

    @Column(name = "create_time", nullable = true)
    private Date createTime;
}
