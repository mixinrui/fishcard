package com.boxfishedu.card.comment.manage.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Created by LuoLiBing on 16/9/10.
 * 老师冻结日志
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FreezeLogDto {
    private Long id;
    private Long teacherId;
    private Boolean freezeStauts;
    private Long createTime;
}
