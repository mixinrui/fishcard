package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ansel on 16/8/29.
 */

@Component
@Data
@Entity
@Table(name = "comment_card_statistics")
public class CommentCardStatistics {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "comment_card_id", nullable = true)
    private Long commentCardId;

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "service_id", nullable = true)
    private Long ServicedId;

    @Column(name = "operation_type", nullable = true)
    private int operationType;

    @Column(name = "amount", nullable = true)
    private Integer amount;

    @Column(name = "create_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime = new Date();
}
