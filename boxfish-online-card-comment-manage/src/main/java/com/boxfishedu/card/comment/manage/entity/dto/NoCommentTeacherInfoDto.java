package com.boxfishedu.card.comment.manage.entity.dto;

import lombok.Data;

import java.util.Date;

/**
 * Created by LuoLiBing on 16/9/8.
 * 未点评老师列表
 */
@Data
public class NoCommentTeacherInfoDto {

    private Long id;

    private Long teacherId;

    private Long markScore;

    private String teacherType;

    private Integer todayReviewCount;

    private Boolean freezeStatus;

    private Date updateDate;

    private String nationality;
}
