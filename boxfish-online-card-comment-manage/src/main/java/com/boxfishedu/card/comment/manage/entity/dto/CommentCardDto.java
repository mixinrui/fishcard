package com.boxfishedu.card.comment.manage.entity.dto;

import com.boxfishedu.card.comment.manage.entity.dto.merger.AliCloudPathMerger;
import com.boxfishedu.card.comment.manage.entity.dto.merger.CommentCardDtoStatusMerger;
import com.boxfishedu.card.comment.manage.entity.enums.CommentCardDtoStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;
import org.jdto.annotation.Sources;
import org.jdto.mergers.DateFormatMerger;

import java.util.Date;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/9/2.
 * 点评dto
 */
@Data
public class CommentCardDto {

    private Long id;

    // 编号
    private Long previous_id;

    private Long studentId;

    private String studentPicturePath;

    @Source(value = "askVoicePath", merger = AliCloudPathMerger.class)
    private String askVoicePath;

    private Long voiceTime;

    private Date studentAskTime;

    private Long teacherId;

    private String teacherName;

    private String teacherPicturePath;

    private Integer assignTeacherCount = 0;

    private Date assignTeacherTime;

    @Source(value = "teacherAnswerTime", merger = DateFormatMerger.class, mergerParam = "yyyy-MM-dd HH:mm:ss")
    private String teacherAnswerTime;

//    private Date teacherAnswerTime;

    @Source(value = "answerVideoPath", merger = AliCloudPathMerger.class)
    private String answerVideoPath;

    private Long answerVideoTime;

    private Long answerVideoSize;

    private String courseId;

    private String courseName;

    private String courseType;

    private String courseDifficulty;

    private String questionName;

    private String cover;

    private Long orderId;

    private String orderCode;

    private int studentReadFlag;

    private int teacherReadFlag;

    private String studentCommentGoodTagCode;

    private String studentCommentBadTagCode;

    private Date studentCommentTeacherTime;

    @Sources(value = {@Source(value = "status"), @Source(value = "studentAskTime")}, merger = CommentCardDtoStatusMerger.class)
    @JsonIgnore
    private CommentCardDtoStatus commentStatus;

    @Source(value = "createTime", merger = DateFormatMerger.class, mergerParam = "yyyy-MM-dd HH:mm:ss")
    private String createTime;

//    private Date createTime;

    @Source(value = "updateTime", merger = DateFormatMerger.class, mergerParam = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;

//    private Date updateTime;


    @DTOTransient
    public int getStatus() {
        return commentStatus.value();
    }

    @DTOTransient
    public String getStatusDesc() {
        return commentStatus.desc();
    }

    @DTOTransient
    public Long getCode() {
        return Objects.isNull(previous_id) ? id: previous_id;
    }
}
