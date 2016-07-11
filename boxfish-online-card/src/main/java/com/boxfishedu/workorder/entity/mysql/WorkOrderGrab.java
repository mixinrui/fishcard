package com.boxfishedu.workorder.entity.mysql;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by jiaozijun on 16/7/11.
 * 抢单信息表
 */
@Component
@Data
@Entity
@Table(name = "work_order_grab")
public class WorkOrderGrab {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;

    @Column(name = "workorder_id", nullable = true)
    private Long workorderId;



    @Column(name = "start_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;


    @Column(name = "create_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;


    @Column(name = "update_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

     @Override
    public String toString() {
        return "WorkOrder{" +
                "id=" + id +
                ", teacherId=" + teacherId +
                ", workorderId=" + workorderId +
                ", startTime='" + startTime + '\'' +
                ", createTime=" + createTime +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }
}
