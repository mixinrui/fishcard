package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/12/28.
 * 小班课
 */
@Component
@Data
@Entity
@Table(name = "small_class")
public class SmallClass implements Cloneable {
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

    @Temporal(TemporalType.DATE)
    private Date classDate;

    @Column(name = "slot_id")
    private Integer slotId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "teacher_name")
    private String teacherName;

    private Long groupLeader;

    private Long groupLeaderCard;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    private Integer status;

    @JsonIgnore
    @Transient
    private List<Long> allStudentIds;

    @JsonIgnore
    @Transient
    private List<Long> allCardIds;

    //班级类型
    public String smallClassType;

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
}
