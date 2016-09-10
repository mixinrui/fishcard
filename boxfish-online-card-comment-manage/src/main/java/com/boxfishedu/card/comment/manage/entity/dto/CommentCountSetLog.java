package com.boxfishedu.card.comment.manage.entity.dto;

import lombok.Data;

/**
 * Created by LuoLiBing on 16/9/8.
 * 老师点评次数设置日志
 */
@Data
public class CommentCountSetLog {

    private Long id;

    private Long teacherId;

    private Long createDatetime;

    private Integer reviewCount;
}
