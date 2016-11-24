package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.*;
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
public class CommentCard {
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

    @Column(name = "course_type", nullable = true, length = 45)
    private String courseType;

    @Column(name = "course_difficulty", nullable = true, length = 45)
    private String courseDifficulty;

    @Column(name = "question_name", nullable = true, length = 1024)
    private String questionName;

    @Column(name = "cover", nullable = true, length = 50)
    private String cover;

    @JoinColumn(name = "service_id", referencedColumnName = "id")//设置对应数据表的列名和引用的数据表的列名
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    private Service service;

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

    public static CommentCard getCommentCard(CommentCardForm commentCardForm){
        CommentCard commentCard = new CommentCard();
        commentCard.setQuestionName(commentCardForm.getQuestionName());
        commentCard.setCourseId(commentCardForm.getCourseId());
        commentCard.setCourseName(commentCardForm.getCourseName());
        commentCard.setCover(commentCardForm.getCover());
        commentCard.setCourseType(commentCardForm.getCourseType());
        commentCard.setCourseDifficulty(commentCardForm.getCourseDifficulty());
        commentCard.setAskVoicePath(commentCardForm.getAskVoicePath());
        commentCard.setVoiceTime(commentCardForm.getVoiceTime());
        return commentCard;
    }

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

    public CommentCard cloneCommentCard() {
        CommentCard temp = new CommentCard();
        temp.setStudentId(this.studentId);
        temp.setStudentPicturePath(studentPicturePath);
        temp.setAskVoicePath(askVoicePath);
        temp.setVoiceTime(voiceTime);
        temp.setStudentAskTime(studentAskTime);
        temp.setCourseId(courseId);
        temp.setCourseName(courseName);
        temp.setCourseType(this.courseType);
        temp.setCourseDifficulty(this.courseDifficulty);
        temp.setQuestionName(questionName);
        temp.setCover(cover);
        temp.setService(service);
        temp.setOrderId(orderId);
        temp.setOrderCode(orderCode);
        temp.setCreateTime(new Date());
        temp.setUpdateTime(new Date());
        return temp;
    }

    /**
     * 超过24小时换老师
     */
    public void changeToOverTime() {
        setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_TWICE.getCode());
        setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
        setTeacherReadFlag(CommentCardStatus.TEACHER_READ.getCode());
        setStatus(CommentCardStatus.REQUEST_ASSIGN_TEACHER.getCode());
    }

    /**
     * 超过48小时退换次数
     */
    public void changeToReturn() {
        setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_TWICE.getCode());
        setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
        setStudentReadFlag(CommentCardStatus.STUDENT_UNREAD.getCode());
        setStatus(CommentCardStatus.OVERTIME.getCode());
    }
}
