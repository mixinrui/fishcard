package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * Created by oyjun on 16/2/29.
 * TODO:需要增加实际上课时间,实际结束时间;评价字段应该单独出表
 */
@Component
@Data
@Entity
@Table(name = "instant_class_card")
public class InstantClassCard {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "request_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date requireTime;

    //学生最近发起请求的时间
    @Column(name = "student_request_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date studentRequestTime;

    //最近一次请求教师的时间
    @Column(name = "teacher_request_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date teacherRequestTime;

    //请求次数
    @Column(name = "request_times", nullable = true)
    private Integer requestTimes;

    //请求教师的次数
    @Column(name = "request_teacher_times", nullable = true)
    private Integer requestTeacherTimes;

    //发起请求,正在匹配老师,未匹配上老师,匹配上老师
    @Column(name = "status", nullable = true)
    private Integer status;

    //鱼卡id,如果有值,表示已经在这个时间点上匹配过老师
    @Column(name = "work_order_id", nullable = true)
    private Long workOrderId;

    @Column(name = "course_id", nullable = true)
    private String courseId;

    @Temporal(TemporalType.DATE)
    private Date classDate;

    @Column(name = "slot_id")
    private Integer slotId;

    @Column(name = "role_id", nullable = true)
    private Integer roleId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date updateTime = DateTime.now().toDate();

    //返回学生的结果标志,0:未返回,1:已返回
    @Column(name = "is_read_flag", nullable = true)
    private Short returnStudentFlag;

}
