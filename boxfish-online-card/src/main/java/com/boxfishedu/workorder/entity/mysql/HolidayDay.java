package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

/**
 * 假期列表 用于自动确认状态过滤假期
 * Created by jiaozijun on 16/11/3.
 */
@Component
@Data
@Entity
@Table(name = "holiday_day")
public class HolidayDay {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "start_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;


    @Column(name = "holiday_name")
    private String holidayName;

}
