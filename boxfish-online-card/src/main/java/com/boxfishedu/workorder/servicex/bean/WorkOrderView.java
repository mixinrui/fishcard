package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.workorder.entity.mysql.Service;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 用于存储传输鱼卡
 * Created by jiaozijun on 16/7/12.
 */
public class WorkOrderView implements Cloneable, Serializable {


    private Long id;

    private Long orderId;

    private Long studentId;

    private String studentName;

    private Long teacherId;

    private String teacherName;

    private Date startTime;

    private Date endTime;

    private Date actualStartTime;

    private Date actualEndTime;

    private Date assignTeacherTime;

    private Integer status;

    private String evaluationToTeacher;

    private String evaluationToStudent;

    private Date createTime;

    private String courseId;

    private String courseName;

    private String courseType;

    private Integer slotId;

    private Date updateTime;

    private Integer skuIdExtra;

    private String statusDesc;

    private Long skuId;

    private String makeUpOrNot;

    private Integer teachingType;

    private String idDesc;

    private Integer seqNum;

    private Long parentId;

    private Long parentRootId;

    private Integer makeUpSeq;

    private Short makeUpFlag;

    private String orderCode;
}
