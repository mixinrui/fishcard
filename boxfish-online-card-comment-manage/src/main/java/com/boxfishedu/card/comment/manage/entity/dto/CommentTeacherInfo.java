package com.boxfishedu.card.comment.manage.entity.dto;

import com.boxfishedu.card.comment.manage.entity.dto.merger.TeacherInfoRpcMerger;
import lombok.Data;
import org.jdto.annotation.Source;

import java.math.BigDecimal;
import java.math.BigInteger;

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

    @Source(value = "teacherId", merger = TeacherInfoRpcMerger.class)
    private TeacherInfo teacherInfo;

    public CommentTeacherInfo(BigInteger teacherId, String teacherName, BigInteger commentCount,
                              BigDecimal finishCount, BigDecimal unfinishCount, BigDecimal timeoutCount) {
        this.teacherId = teacherId.longValue();
        this.teacherName = teacherName;
        this.commentCount = commentCount.intValue();
        this.finishCount = finishCount.intValue();
        this.unfinishCount = unfinishCount.intValue();
        this.timeoutCount = timeoutCount.intValue();
    }



//    public static List<CommentTeacherInfo>

}
