package com.boxfishedu.workorder.entity.mysql;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;


import javax.persistence.*;
import java.util.Date;

/**
 * Created by oyjun on 16/2/29.
 * TODO:需要增加实际上课时间,实际结束时间;评价字段应该单独出表
 */
@Component
@Data
@Entity
@Table(name = "work_order")
@EqualsAndHashCode(exclude = "workOrderLogs")
public class WorkOrder{
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "order_id", nullable = true)
    private Long orderId;

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "student_name", nullable = true, length = 20)
    private String studentName;

    @Column(name = "teacher_id", nullable = true)
    private Long teacherId;

    @Column(name = "teacher_name", nullable = true, length = 64)
    private String teacherName;

    @Column(name = "start_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(name = "actual_start_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualStartTime;

    @Column(name = "actual_end_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualEndTime;

    @Column(name = "assign_teacher_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignTeacherTime;

    @Column(name = "status", nullable = true)
    private Integer status;

    @Column(name = "evaluation_to_teacher", nullable = true, length = 100)
    private String evaluationToTeacher;

    @Column(name = "evaluation_to_student", nullable = true, length = 100)
    private String evaluationToStudent;

    @Column(name = "create_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "course_id", nullable = true, length = 255)
    private String courseId;

    @Column(name = "course_name", nullable = true, length = 128)
    private String courseName;

    @Column(name = "course_type")
    private String courseType;

    @Column(name = "slot_id", nullable = true)
    private Integer slotId;

    @Column(name = "update_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    //不为1的时候只看sku_id
    @Column(name = "sku_id_extra", nullable = true)
    private Integer skuIdExtra;

    @Column(name = "order_channel")
    private String orderChannel;

    @JoinColumn(name = "service_id", referencedColumnName = "id")//设置对应数据表的列名和引用的数据表的列名
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private Service service;

//    @Column(name = "service_id", nullable = false)
//    private Long serviceId;

    @Transient
    private String statusDesc;

    @Transient
    private Long skuId;

    //是否补过课
    @Transient
    private String makeUpOrNot;

    @Transient
    private Integer teachingType;

    //给前端显示使用,特别用在补课的id后面补"B"
    @Transient
    private String idDesc;

    @Column(name = "seq_num", nullable = false)
    private Integer seqNum;

    //被补课的鱼卡id
    @Column(name = "parent_id")
    private Long parentId;

    //被补课的根节点id.增加该字段,提高性能
    @Column(name="parent_root_id")
    private Long parentRootId;

    //补课的次序(方便统计,显示)
    @Column(name="make_up_seq")
    private Integer makeUpSeq;

    //是否已安排补课
    @Column(name="make_up_flag")
    private Short makeUpFlag;

    @Column(name="is_course_over")
    private Short isCourseOver;

    //order_code字段
    @Column(name="order_code", length = 128)
    private String orderCode;
    //0 True 1 False  是否手动修改过
    @Column(name="update_manul_flag", length = 1)
    private String updateManulFlag;


    /** 更改课程时间  **/
    @Column(name = "updatetime_changecourse", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatetimeChangecourse;

    /** 关于更改课程 是否发送过消息   1 未发送  0 表示已经发送 **/
    @Column(name = "sendflagcc", nullable = true)
    private String sendflagcc;



    /** 关于更改课程 是否发送过消息   1 未确认  0 已经确认 **/
    @Column(name = "confirm_flag", nullable = true)
    private String confirmFlag;

    /**  鱼卡退款状态  **/
    @Column(name = "status_recharge", nullable = true)
    private Integer    statusRecharge;

    /**  退款原因  **/
    @Column(name = "reason_recharge", nullable = true)
    private String reasonRecharge;

    /**  鱼卡退款状态变更时间  **/;
    @Column(name = "updatetime_recharge", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatetimeRecharge;


    @Override
    public String toString() {
        return "WorkOrder{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", teacherId=" + teacherId +
                ", teacherName='" + teacherName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", actualStartTime=" + actualStartTime +
                ", actualEndTime=" + actualEndTime +
                ", status=" + status +
                ", evaluationToTeacher='" + evaluationToTeacher + '\'' +
                ", evaluationToStudent='" + evaluationToStudent + '\'' +
                ", createTime=" + createTime +
                ", courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", updateTime=" + updateTime +
                ", updateMauulFlag=" + updateManulFlag +
                '}';
    }
}
