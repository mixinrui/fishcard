package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicClassBuilderParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.TrialSmallClassParam;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/12/28.
 * 小班课
 */
@Data
@Entity
@Table(name = "small_class")
public class SmallClass implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "course_type")
    private String courseType;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    private String cover;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+08:00")
    private Date classDate;

    @Column(name = "slot_id")
    private Integer slotId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "teacher_name")
    private String teacherName;

    @Column(name = "teacher_photo")
    private String teacherPhoto;


    private Long groupLeader;

    private Long groupLeaderCard;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date actualStartTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date actualEndTime;

    private Integer status;

    private Integer demoFlag;

    @JsonIgnore
    @Transient
    private Date reportTime;

    //是否为试讲课
    @JsonIgnore
    @Transient
    private Boolean isTrial;

    @JsonIgnore
    @Transient
    private Long statusReporter;

    @Transient
    private Long classNum = 0L;   //课程数量

    @JsonIgnore
    @Transient
    private String writeBackDesc;

    @JsonIgnore
    @Transient
    private List<Long> allStudentIds;

    @JsonIgnore
    @Transient
    private List<WorkOrder> allCards;

    @JsonIgnore
    @Transient
    private PublicClassInfoStatusEnum classStatusEnum;

    @Transient
    private List<Long> members;

    //班级类型
    private String classType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date updateTime = DateTime.now().toDate();

    @Override
    public SmallClass clone() {
        SmallClass smallClass = null;
        try {
            smallClass = (SmallClass) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new BusinessException("clone error");
        }
        return smallClass;
    }

    public SmallClass() {

    }

    public SmallClass(PublicClassBuilderParam publicClassBuilderParam) {
        this.setTeacherName(publicClassBuilderParam.getTeacherName());
        this.setTeacherId(publicClassBuilderParam.getTeacherId());
        this.setUpdateTime(new Date());
        this.setRoleId(publicClassBuilderParam.getRoleId().intValue());
        this.setClassType(ClassTypeEnum.PUBLIC.name());
        this.setClassDate(DateUtil.simpleString2Date(publicClassBuilderParam.getDate()));
        this.setSlotId(publicClassBuilderParam.getSlotId().intValue());
        this.setCreateTime(new Date());
        this.setDifficultyLevel(publicClassBuilderParam.getDifficulty());
    }

    public SmallClass(TrialSmallClassParam trialSmallClassParam) {
        this.setTeacherId(trialSmallClassParam.getTeacherId());
        this.setTeacherName(trialSmallClassParam.getTeacherName());
        this.setRoleId(trialSmallClassParam.getRoleId().intValue());

        this.setClassDate(DateUtil.simpleString2Date(trialSmallClassParam.getStartTime()));

        this.setStartTime(DateUtil.String2Date(trialSmallClassParam.getStartTime()));
        this.setSlotId(trialSmallClassParam.getTimeSlotId());

        this.setIsTrial(true);

        this.setClassType("SMALLCLASS_TRIAL");

        this.setCreateTime(new Date());
        this.setUpdateTime(new Date());
        this.setDifficultyLevel(trialSmallClassParam.getDifficultyLevel());
    }

    public ClassTypeEnum getStatusEnum() {
        return ClassTypeEnum.getByName(this.getClassType());
    }

    //小班课上课时间30分钟
    public boolean reachOverTime() {
        return new Date().after(this.getEndTime());
    }

    public boolean isDemo() {
        return this.demoFlag == null ? false :
                (1 == this.demoFlag ? true : false);
    }
}
