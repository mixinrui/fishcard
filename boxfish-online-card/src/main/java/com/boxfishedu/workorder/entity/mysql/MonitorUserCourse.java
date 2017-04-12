package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ansel on 2017/3/22.
 */
@Data
@Table(name = "monitor_user_course")
@Entity
public class MonitorUserCourse {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "monitor_user_id")
    private Long monitorUserId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "class_type")
    private String classType;

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "monitor_flag")
    private Integer monitorFlag = 0;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "create_time")
    private Date createTime;

}
