package com.boxfishedu.card.comment.manage.entity.dto;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by mk on 16/7/20.
 */
@Data
public class TeacherReviewInfo {

    private Long id;

    private Long teacherId;//老师Id

    private Double markScore;

    private String teacherType;

    private Integer todayReviewCount;

    //评价日期
    @Column(name = "update_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date updateDate;

    //冻结状态,表示是否冻结  初始默认  false
    @Column(name = "freeze_status", nullable = true)
    private String freezeStatus;

    @Column(name = "nationality", nullable = true)
    private String nationality;

    @Column(name = "teacher_name", nullable = true)
    private String teacherName;

    @Column(name = "first_name", nullable = true)
    private String firstName;

    @Column(name = "last_name", nullable = true)
    private String lastName;

    //当日剩余点评次数
    @Transient
    private String todayRemainReviewCount;

}
