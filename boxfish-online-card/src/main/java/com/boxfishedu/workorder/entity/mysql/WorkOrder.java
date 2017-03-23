package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.bean.FishCardNetStatusEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.web.param.fishcardcenetr.TrialSmallClassParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Created by oyjun on 16/2/29.
 * TODO:需要增加实际上课时间,实际结束时间;评价字段应该单独出表
 */
@Component
@Data
@Entity
@Table(name = "work_order")
@EqualsAndHashCode(exclude = "workOrderLogs")
public class WorkOrder implements Cloneable {
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
    private Boolean inspectFlag;   //  true 显示体验学生  flase 不显示学生

    /**
     * (TeachingType.WAIJIAO.getCode() ==wo.getSkuId()
     **/
    @Column(name = "sku_id")
    private Integer skuId;

    /**
     * 更改次数
     **/
    @Column(name = "changtime_times")
    private Integer changtimeTimes;

    //是否补过课
    @Transient
    private String makeUpOrNot;


    @Transient
    private String orderTypeDesc;

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
    @Column(name = "parent_root_id")
    private Long parentRootId;

    //补课的次序(方便统计,显示)
    @Column(name = "make_up_seq")
    private Integer makeUpSeq;

    //是否已安排补课 (1 表示已经不过课)
    @Column(name = "make_up_flag")
    private Short makeUpFlag;

    @Column(name = "is_course_over")
    private Short isCourseOver;

    //order_code字段
    @Column(name = "order_code", length = 128)
    private String orderCode;
    //0 True 1 False  是否手动修改过
    @Column(name = "update_manul_flag", length = 1)
    private String updateManulFlag;
    /**
     * 关于更改课程 是否发送过消息   1 未发送  0 表示已经发送
     **/
    @Column(name = "sendflagcc", nullable = true)
    private String sendflagcc;

    /**
     * 更改课程时间
     **/
    @Column(name = "updatetime_changecourse", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatetimeChangecourse;

    /**
     * 关于更改课程 是否发送过消息   1 未确认  0 已经确认
     **/
    @Column(name = "confirm_flag", nullable = true)
    private String confirmFlag;

    /**
     * 鱼卡退款状态
     **/
    @Column(name = "status_recharge", nullable = true)
    private Integer statusRecharge;

    /**
     * 退款原因
     **/
    @Column(name = "reason_recharge", nullable = true)
    private String reasonRecharge;

    /**  鱼卡退款状态变更时间  **/
    ;
    @Column(name = "updatetime_recharge", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatetimeRecharge;

    //是否被冻结,0.未冻结 1.冻结
    @Column(name = "is_freeze", nullable = true)
    private Integer isFreeze;

    @Column(name = "combo_type", nullable = true)
    private String comboType;

    //class_type[区分该鱼卡的生成方式,传统方式?实时上课?]
    @Column(name = "class_type", nullable = true)
    private String classType;

    // 生成方式  小班课超级用户 super  ClassUserTypeEnum
    @Column(name = "generator_type", nullable = true)
    private String generatorType;

    //小班课id
    @Column(name = "small_class_id")
    private Long smallClassId;

    @Transient
    private Boolean freezeBtnShowFlag = false;

    @Transient
    private String tutorType;

    //解冻按钮是否显示标记
    @Transient
    private Boolean unfreezeBtnShowFlag = false;
    /**
     * 学生旷课扣积分标记
     **/
    @Column(name = "deduct_score_status", nullable = true)
    private Integer deductScoreStatus;

    /**
     * 由于假期原因,提示该鱼卡需要更换时间   10 需要更换时间
     **/
    @Column(name = "need_change_time", nullable = true)
    private Integer needChangeTime;

    // 1对1  小班课  公开课  向学生系统发送 完成标示  1 发送过  0 或者null  未发送
    @Column(name = "is_compute_send")
    private Short isComputeSend;


    @Transient
    private String groupId; // 群组id

    @Transient
    private boolean normal = false; //是否正常

    @Transient

    private Long chatRoomId;//房间号

    @Transient
    private boolean haveTeacherRequested = true; //是否有教师操作

    @Transient
    private Integer teacherNetStatus;

    @Transient
    private String teacherNetStatusDesc;

    @Transient
    private Integer studentNetStatus;

    @Transient
    private String studentNetStatusDesc;

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

    public void initCourseInfo(RecommandCourseView courseView) {
        setCourseId(courseView.getCourseId());
        setCourseName(courseView.getCourseName());
        setCourseType(courseView.getCourseType());

        // 课程ID不为空,状态为分配老师,不予修改状态
        if (StringUtils.isNotEmpty(courseView.getCourseId()) && (status == FishCardStatusEnum.CREATED.getCode())) {
            setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
        }
    }

    @JsonIgnore
    public int getRecommendSequence() {
        if (Objects.isNull(seqNum)) {
            throw new BusinessException("错误的序列号!!");
        }

        if (seqNum % 8 == 0) {
            return 8;
        } else {
            return seqNum % 8;
        }
    }

    @Override
    public WorkOrder clone() {
        WorkOrder prototypeClass = null;
        try {
            prototypeClass = (WorkOrder) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("克隆对象失败");
        }
        return prototypeClass;
    }

    public void addStudentStatus(FishCardNetStatusEnum fishCardNetStatusEnum) {
        this.addNetStatus(fishCardNetStatusEnum, "student");
    }

    public void addTeacherStatus(FishCardNetStatusEnum fishCardNetStatusEnum) {
        this.addNetStatus(fishCardNetStatusEnum, "teacher");
    }

    private void addNetStatus(FishCardNetStatusEnum fishCardNetStatusEnum, String role) {
        if (StringUtils.equals("student", role)) {
            this.setStudentNetStatus(fishCardNetStatusEnum.getCode());
            this.setStudentNetStatusDesc(fishCardNetStatusEnum.getDesc());
        }
        if (StringUtils.equals("teacher", role)) {
            this.setTeacherNetStatus(fishCardNetStatusEnum.getCode());
            this.setTeacherNetStatusDesc(fishCardNetStatusEnum.getDesc());
        }
    }

    public boolean isCourseNotOver() {
        return Objects.isNull(this.getIsCourseOver()) || 1 != this.getIsCourseOver();
    }

    public boolean isStudentAbsent() {
        return FishCardStatusEnum.STUDENT_ABSENT.getCode() == this.getStatus();
    }

    public boolean isTeacherAbsent() {
        return FishCardStatusEnum.TEACHER_ABSENT.getCode() == this.getStatus();
    }

    public boolean statusFinished() {
        return FishCardStatusEnum.COMPLETED.getCode() == this.getStatus()
                || FishCardStatusEnum.COMPLETED_FORCE.getCode() == this.getStatus();
    }

    public boolean reachOverTime() {
        return !new Date().before(this.getEndTime());
    }

    public boolean isGroupCard() {
        return Objects.equals(this.getClassType(), ClassTypeEnum.SMALL.name())
                || Objects.equals(this.getClassType(), ClassTypeEnum.PUBLIC.name());
    }

    public boolean notGroupWorkOrder() {
        return !Objects.equals(this.getClassType(), ClassTypeEnum.PUBLIC.name())
                && !Objects.equals(this.getClassType(), ClassTypeEnum.SMALL.name());
    }

    public boolean notPublicWorkOrder() {
        return !Objects.equals(this.getClassType(), ClassTypeEnum.PUBLIC.name());
    }
}
