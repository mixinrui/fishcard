package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by LuoLiBing on 16/3/11.
 */
@Table
@Entity
@Data
public class CourseSchedule {

    /**
     * 没有分配老师的ID
     */
    public final static Integer NO_ASSIGN_TEACHER_ID = 0;

    @Id
    @GeneratedValue
    private Long id;

    private Long studentId;

    private Long teacherId;

    @Column(name = "timeslots_id")
    private Integer timeSlotId;

    @Column(name = "course_id", nullable = true, length = 255)
    private String courseId;

    /**
     * 10:老师已选,20:学生已选,30:已推课,40:已上课,50:已过期,60:已作废
     */
    private Integer status = 10;

    @Column(name = "workorder_id")
    private Long workorderId;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+08:00")
    private Date classDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date updateTime = DateTime.now().toDate();
    
    @Column(name = "course_name", nullable = true, length = 255)
    private String courseName;

    @Column(name = "role_id", nullable = true)
    private Integer roleId;

    @Column(name = "course_type")
    private String courseType;

    //不为1的时候只看sku_id
    @Column(name = "sku_id_extra", nullable = true)
    private Integer skuIdExtra;

    @Column(name = "is_freeze", nullable = true)
    private Integer isFreeze;

    //立即上课:INSTANT
    @Column(name = "class_type", nullable = true)
    private String classType;

    @Column(name = "instant_start_time", nullable = true)
    private String instantStartTtime;

    @Column(name = "instant_end_time", nullable = true)
    private String instantEndTtime;

    @Column(name = "start_time", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    /** 由于假期原因,提示该鱼卡需要更换时间   10 需要更换时间  **/
    @Column(name = "need_change_time", nullable = true)
    private Integer needChangeTime;

    @Column(name = "small_class_id", nullable = true)
    private Long smallClassId;

    @Transient
    private Integer matchStatus;  // 用于指定老师列表判断

    public boolean isSmallClass(){
        return StringUtils.equals(this.getClassType(), ClassTypeEnum.SMALL.name());
    }
}