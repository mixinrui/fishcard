package com.boxfishedu.card.mail.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by LuoLiBing on 16/9/1.
 */
@Entity
@Data
@Table(name = "comment_card")
public class CommentCard {

    @Id
    // read only
    @Column(name = "id", insertable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "student_ask_time")
    private Date studentAskTime;

    @Column(name = "status")
    private Integer status;
}
