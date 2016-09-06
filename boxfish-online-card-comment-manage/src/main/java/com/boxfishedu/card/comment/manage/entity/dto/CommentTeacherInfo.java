package com.boxfishedu.card.comment.manage.entity.dto;

import lombok.Data;

/**
 * Created by LuoLiBing on 16/9/6.
 */
@Data
public class CommentTeacherInfo {

    private Long teacherId;

    private String teacherName;

    private Integer commentCount;

    private Integer finishCount;

    private Integer unfinishCount;

    private Integer timeoutCount;

    private Integer status;


}
