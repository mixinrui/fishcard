package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.web.param.SelectedTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Component
@Data
@Entity
@Table(name = "instant_class_card")
public class InstantClassCard {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "order_id", nullable = true)
    private Long orderId;

    @Column(name = "product_type", nullable = true)
    private Integer productType;

    @Column(name = "tutor_type", nullable = true)
    private String tutorType;

    @Column(name = "combo_type", nullable = true)
    private String comboType;

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;

    @Column(name = "teacher_name", nullable = true)
    private String teacherName;

    //记录最后一次访问时间，详细访问日志在mongo的InstantClassCardLog
    @Column(name = "student_request_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date studentRequestTime;

    //学生请求次数
    @Column(name = "student_request_times", nullable = true)
    private Integer studentRequestTimes;

    //最近一次请求分配教师的时间
    @Column(name = "request_match_teacher_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestMatchTeacherTime;

    //请求教师的次数
    @Column(name = "request_teacher_times", nullable = true)
    private Integer requestTeacherTimes;

    //发起请求,正在匹配老师,未匹配上老师,匹配上老师
    @Column(name = "status", nullable = true)
    private Integer status;

    //鱼卡id,如果有值,表示已经在这个时间点上匹配过老师
    @Column(name = "workorder_id", nullable = true)
    private Long workorderId;

    @Column(name = "course_id", nullable = true)
    private String courseId;

    @Column(name = "course_type", nullable = true)
    private String courseType;

    @Temporal(TemporalType.DATE)
    private Date classDate;

    @Column(name = "slot_id")
    private Long slotId;

    @Column(name = "role_id", nullable = true)
    private Integer roleId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date updateTime = DateTime.now().toDate();

    //返回学生的未匹配结果标志,0:未返回,1:已返回
    @Column(name = "result_read_flag", nullable = true)
    private Integer resultReadFlag;

    //返回学生的结果标志,0:未返回,1:已返回
    @Column(name = "match_result_read_flag", nullable = true)
    private Integer matchResultReadFlag;

    //返回学生的结果标志,0:课程表入口,1:其他入口
    @Column(name = "entrance", nullable = true)
    private Integer entrance;

    @Column(name = "group_name", nullable = true)
    private String groupName;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "chat_room_id")
    private Long chatRoomId;
}
