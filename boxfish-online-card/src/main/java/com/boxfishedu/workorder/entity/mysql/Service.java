package com.boxfishedu.workorder.entity.mysql;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by oyjun on 16/2/29.
 */
@Data
@Entity
@Table(name = "service")
@ToString(exclude = "workOrders")
@EqualsAndHashCode(exclude = "workOrders")
public class Service {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "student_name", nullable = true, length = 45)
    private String studentName;

    @Column(name = "order_id", nullable = true)
    private Long orderId;

    @Column(name = "order_code", nullable = true, length = 128)
    private String orderCode;

    @Column(name = "original_amount", nullable = true)
    private Integer originalAmount;

    @Column(name = "amount", nullable = true)
    private Integer amount;

    @Column(name = "start_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(name = "sku_id", nullable = true)
    private Long skuId;

    @Column(name = "sku_name", nullable = true, length = 45)
    private String skuName;

    @Column(name = "create_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @OneToMany(fetch = FetchType.LAZY)//指向多的那方的pojo的关联外键字段
    @JoinColumn(name = "service_id")
    @JsonManagedReference
    private Set<WorkOrder> workOrders = new HashSet<WorkOrder>(0);

    @Column(name = "validaty_day", nullable = true)
    private Integer validityDay;


//    @Column(name = "role_id", nullable = true)
//    private Integer roleId;

    //一共几个月
    @Column(name = "combo_cycle", nullable = true)
    private Integer comboCycle;

    //每月的的上课次数
//    @Column(name = "count_in_month", nullable = true)
//    private Integer countInMonth;

    @Transient
    private Date currDate;

    @Column(name = "courses_selected", nullable = false)
    private Integer coursesSelected;

    /**
     * 课程推荐时使用: CN, FRN, MIXED 分别推中教\外教\中外教
     */
    @Column(name = "tutor_type", nullable = true)
    private String tutorType;

    /**
     * 订单类型: STANDARD(标准),EXPERIENCE(体验),EXCHANGE(兑换)
     */
    @Column(name = "order_channel", nullable = true)
    private String orderChannel;

    /**
     * 产品分类: 1001为上课,  1002为外教点评
     */
    @Column(name = "product_type", nullable = true)
    private Integer productType;

//    /**
//     * 课程类型
//     */
//    @Column(name = "teaching_type", nullable = true)
//    private Integer teachingType;

}
