package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;

/**
 * 体验课名单
 * 用于鱼卡显示
 * Created by jiaozijun on 17/2/9.
 */
@Component
@Data
@Entity
@Table(name = "work_order")
public class WorkOrderInspect implements Cloneable{
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;



    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "student_name", nullable = true, length = 20)
    private String studentName;
}
