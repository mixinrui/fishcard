package com.boxfishedu.workorder.entity.mysql;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by hucl on 16/4/7.
 */
//TODO:评论表以后单独出来
@Data
public class WorkOrderEvaluate {
        @Id
        @Column(name = "id", nullable = false)
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;
        @Column(name = "work_order_id", nullable = true)
        private Long workOrderId;

        @Column(name = "student_id", nullable = true)
        private Long studentId;

        @Column(name = "teacher_id", nullable = true)
        private Long teacherId;

        @Column(name = "evaluation_to_teacher", nullable = true, length = 128)
        private String evaluationToTeacher;

        @Column(name = "evaluation_to_student", nullable = true, length = 128)
        private String evaluationToStudent;

        @Column(name = "create_time", nullable = true)
        @Temporal(TemporalType.TIMESTAMP)
        private Date createTime;

        @ManyToOne(fetch = FetchType.LAZY)
        @JsonBackReference
        private Service service;

//        @OneToOne(fetch = FetchType.LAZY, mappedBy = "workOrder")
//        @Cascade(value = {org.hibernate.annotations.CascadeType.ALL})
//        @JsonManagedReference
//        private Set<WorkOrderLog> workOrderLogs = new HashSet<WorkOrderLog>(0);
}
