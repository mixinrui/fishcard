package com.boxfishedu.card.comment.manage.entity.dto;

import com.boxfishedu.card.comment.manage.entity.dto.merger.CommentCardStatusMerger;
import lombok.Data;
import org.jdto.annotation.Source;

import java.util.Date;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@Data
public class CommentCardDto {

    private Long id;

    private Long studentId;

    private String studentPicturePath;

    private String askVoicePath;

    private Long voiceTime;

    private Date studentAskTime;

    private Long teacherId;

    private String teacherPicturePath;

    private Integer assignTeacherCount = 0;

    private Date assignTeacherTime;

    private Date teacherAnswerTime;

    private String answerVideoPath;

    private Long answerVideoTime;

    private Long answerVideoSize;

    private String courseId;

    private String courseName;

    private String questionName;

    private String cover;

    private Long orderId;

    private String orderCode;

    private int studentReadFlag;

    private int teacherReadFlag;

    private String studentCommentGoodTagCode;

    private String studentCommentBadTagCode;

    private Date studentCommentTeacherTime;

    private Integer status;

    @Source(value = "status", merger = CommentCardStatusMerger.class)
    private String statusDesc;

    private Date createTime;

    private Date updateTime;
}
