package com.boxfishedu.workorder.entity.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/12/29.
 */
@Data
@Entity(noClassnameStored = true)
public class SmallClassLog {
    @JsonIgnore
    @Id
    private ObjectId id;

    private Long teacherId;

    //在线学生
    private List<Long> activeStudents;

    //小班课的所有学生
    private List<Long> allStudents;

    //在线的鱼卡
    private List<Long> activeWorkOrders;

    //所有鱼卡
    private List<Long> allWorkOrders;

    //小班课当前状态(教师动作)
    private Integer status;

    //进入房间的学生
    private Long studentId;
    
    //事件的id:当前是什么动作
    private Integer eventId;

    //状态描述
    private String desc;

    private Date createTime;
}
