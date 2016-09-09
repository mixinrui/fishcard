package com.boxfishedu.card.comment.manage.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Created by LuoLiBing on 16/9/8.
 * 未点评老师列表
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoCommentTeacherInfoDto {

    private Long id;

    private Long teacherId;

    private String teacherName;

    private Long markScore;

    private String teacherType;

    private Boolean freezeStatus;

    private String nationality;
}
