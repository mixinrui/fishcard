package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.bean.CommentCardStarEnum;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ansel on 16/11/12.
 */
@Component
@Entity
@Data
@Table(name = "comment_card_star")
public class CommentCardStar {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "comment_card_id", nullable = false)
    private Long commentCardId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "star_level")
    private Integer starLevel;

    @Column(name = "point")
    private Integer point;

    @Column(name = "create_time")
    private Date createTime;

    public CommentCardStar(){}

    public CommentCardStar(Long cardId, Long studentId,Long teacherId,int starLevel){
        this.commentCardId = cardId;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.status = 1;
        this.starLevel = starLevel;
        this.point = CommentCardStarEnum.getPoint(starLevel);
        this.createTime = new Date();
    }
}
