package com.boxfishedu.card.comment.manage.entity.mysql;

import com.boxfishedu.card.comment.manage.entity.enums.CommentCardStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Created by oyjun on 16/2/29.
 */
@Component
@Data
@Entity
@Table(name = "comment_card")
@ToString(exclude = {"unAnswerTeacherCards","service"})
@EqualsAndHashCode(exclude = {"unAnswerTeacherCards"})
public class CommentCard implements Serializable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "previous_id", nullable = true)
    private Long previous_id;

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "student_name", nullable = true)
    private String studentName;

    @Column(name = "student_picture_path")
    private String studentPicturePath;

    @Column(name = "ask_voice_path", nullable = true, length = 255)
    private String askVoicePath;

    @Column(name = "voice_time", nullable = true, length = 20)
    private Long voiceTime;

    @Column(name = "student_ask_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date studentAskTime;

    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;

    @Column(name = "teacher_first_name", nullable = true)
    private String teacherFirstName;

    @Column(name = "teacher_last_name",nullable = true)
    private String teacherLastName;

    @Column(name = "teacher_name", nullable = true)
    private String teacherName;

    @Column(name = "teacher_picture_path")
    private String teacherPicturePath;

    @Column(name = "teacher_status")
    private Integer teacherStatus;

    @Column(name="assign_teacher_count", nullable = true)
    private Integer assignTeacherCount = 0;

    @Column(name = "assign_teacher_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignTeacherTime;

    @Column(name = "teacher_answer_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date teacherAnswerTime;

    @Column(name = "answer_video_path", nullable = true, length = 255)
    private String answerVideoPath;

    @Column(name = "answer_video_time", nullable = true, length = 20)
    private Long answerVideoTime;

    @Column(name = "answer_video_size",nullable = true, length = 20)
    private Long answerVideoSize;

    @Column(name = "course_id", nullable = true, length = 255)
    private String courseId;

    @Column(name = "course_name", nullable = true, length = 500)
    private String courseName;

    @Column(name = "question_name", nullable = true, length = 1024)
    private String questionName;

    @Column(name = "cover", nullable = true, length = 50)
    private String cover;

    @Column(name = "order_id", nullable = true)
    private Long orderId;

    @Column(name="order_code", length = 128)
    private String orderCode;

    @Column(name = "student_read_flag", nullable = true, columnDefinition = "1")
    private int studentReadFlag;

    @Column(name = "teacher_read_flag", nullable = true, columnDefinition = "1")
    private int teacherReadFlag;

    @Column(name="student_comment_good_tag_code", length = 128)
    private String studentCommentGoodTagCode;

    @Column(name="student_comment_bad_tag_code", length = 128)
    private String studentCommentBadTagCode;

    @Column(name = "student_comment_teacher_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date studentCommentTeacherTime;

    @Column(name = "status", nullable = true)
    private Integer status;

    @Column(name = "create_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    public String[] getStudentCommentGoodTagCode() {
        if(StringUtils.isNotEmpty(studentCommentGoodTagCode)) {
            return studentCommentGoodTagCode.split(",");
        }
        return null;
    }

    public void setStudentCommentGoodTagCode(List studentCommentGoodTagCode) {
        this.studentCommentGoodTagCode = String.join(",", studentCommentGoodTagCode);
    }

    public String[] getStudentCommentBadTagCode() {
        if(StringUtils.isNotEmpty(studentCommentBadTagCode)) {
            return studentCommentBadTagCode.split(",");
        }
        return null;
    }

    public void setStudentCommentBadTagCode(List studentCommentBadTagCode) {
        this.studentCommentBadTagCode = String.join(",", studentCommentBadTagCode);
    }

    // 换老师逻辑
    public CommentCard changeTeacher(Long targetTeacher) {
        Duration duration = Duration.between(studentAskTime.toInstant(), Instant.now());
        // 没有超过24小时的不能换老师
        if(duration.toHours() < 24) {
            throw new IllegalArgumentException("没有超过24小时不能更换老师");
        }

        Date now = new Date();
        // 复制一份点评作为新的外教点评
        CommentCard newCommentCard = (CommentCard) SerializationUtils.clone(this);
        newCommentCard.setId(null);
        newCommentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
        newCommentCard.setTeacherId(targetTeacher);
        newCommentCard.setUpdateTime(now);

        // 之前的老师标记为过期,并且标记为后台强制更换老师
        status = CommentCardStatus.OVERTIME.getCode();
        assignTeacherCount = CommentCardStatus.ASSIGN_TEACHER_TRIPLE.getCode();
        teacherReadFlag = CommentCardStatus.TEACHER_UNREAD.getCode();
        updateTime = now;
        assignTeacherTime = now;
        return newCommentCard;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
