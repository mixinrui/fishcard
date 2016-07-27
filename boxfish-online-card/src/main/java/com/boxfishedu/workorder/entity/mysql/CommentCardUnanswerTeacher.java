package com.boxfishedu.workorder.entity.mysql;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;

    @Column(name = "create_time", nullable = true)
    private Date createTime;

    @JoinColumn(name = "card_id", referencedColumnName = "id")//设置对应数据表的列名和引用的数据表的列名
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    private CommentCard commentCard;

    @Override
    public String toString() {
        return "CommentCardUnanswerTeacher{" +
                "id=" + id +
                ", teacherId=" + teacherId +
                ", createTime=" + createTime +
                ", commentCard=" + commentCard +
                '}';
    }
}
