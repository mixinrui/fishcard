package com.boxfishedu.workorder.entity.mysql;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by oyjun on 16/2/29.
 * TODO:需要增加实际上课时间,实际结束时间;评价字段应该单独出表
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

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "student_name", nullable = true, length = 20)
    private String studentName;

    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;

    @Column(name = "teacher_name", nullable = true, length = 20)
    private String teacherName;

    @Column(name = "status", nullable = true)
    private Integer status;

    @Column(name = "create_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "assign_teacher_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignTeacherTime;

    @Column(name = "student_ask_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date studentAskTime;

    @Column(name = "teacher_answer_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date teacherAnswerTime;

    @Column(name = "question_name", nullable = true, length = 1024)
    private String questionName;

    @Column(name = "ask_voice_id", nullable = true, length = 20)
    private Long askVoiceId;

    @Column(name = "voice_time", nullable = true, length = 20)
    private Long voiceTime;

    @Column(name = "ask_voice_path", nullable = true, length = 255)
    private String askVoicePath;

    @Column(name = "answer_video_path", nullable = true, length = 255)
    private String answerVideoPath;

    @Column(name = "answer_video_thumbnail", nullable = true, length = 255)
    private String answerVideoThumbnail;

    @Column(name = "evaluation_to_teacher", nullable = true, length = 11)
    private Integer evaluationToTeacher;

    @Column(name = "course_id", nullable = true, length = 255)
    private String courseId;

    @Column(name = "course_name", nullable = true, length = 500)
    private String courseName;

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

    @Column(name="student_comment_good_tag_code", length = 128)
    private String studentCommentGoodTagCode;

    @Column(name="student_comment_bad_tag_code", length = 128)
    private String studentCommentBadTagCode;

    @Column(name = "student_comment_teacher_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date studentCommentTeacherTime;

    @Column(name="assign_teahcer_count", nullable = true)
    private Integer assignTeacherCount = 0;


    @Column(name = "teacher_read_flag", nullable = true)
    private int teacherReadFlag;

    @Column(name = "student_read_flag", nullable = true)
    private int studentReadFlag;

    @OneToMany(fetch = FetchType.LAZY)//指向多的那方的pojo的关联外键字段
    @JoinColumn(name = "card_id")
    @JsonManagedReference
    private Set<CommentCardUnanswerTeacher> unAnswerTeacherCards = new HashSet<CommentCardUnanswerTeacher>(0);

    public static CommentCard getCommentCard(CommentCardForm commentCardForm){
        CommentCard commentCard = new CommentCard();
        commentCard.setStudentName(commentCardForm.getStudentName());
        commentCard.setQuestionName(commentCardForm.getQuestionName());
        commentCard.setCourseId(commentCardForm.getCourseId());
        commentCard.setCourseName(commentCardForm.getCourseName());
        commentCard.setCover(commentCardForm.getCover());
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

}
