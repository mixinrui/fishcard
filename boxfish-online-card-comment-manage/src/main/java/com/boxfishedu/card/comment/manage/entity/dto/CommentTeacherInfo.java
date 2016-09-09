package com.boxfishedu.card.comment.manage.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by LuoLiBing on 16/9/6.
 * 已点评老师列表
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentTeacherInfo {

    private Long teacherId;

    private Integer commentCount;

    private Integer finishCount;

    private Integer unfinishCount;

    private Integer timeoutCount;

    public CommentTeacherInfo(BigInteger teacherId, BigInteger commentCount,
                              BigDecimal finishCount, BigDecimal unfinishCount, BigDecimal timeoutCount) {
        this.teacherId = teacherId.longValue();
        this.commentCount = commentCount.intValue();
        this.finishCount = finishCount.intValue();
        this.unfinishCount = unfinishCount.intValue();
        this.timeoutCount = timeoutCount.intValue();
    }

    public CommentTeacherInfo() {}
}
