package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.Column;
import java.util.Date;

/**
 * 小班课显示列表
 * Created by jiaozijun on 16/12/13.
 */

@Data
public class SmallClassResult {

//,StStudentApplyRecords.ReadStatus isRead
    public SmallClassResult( Long id,
             Date createTime,
             Long teacherId,
             String teacherName,
             String groupName,
             String groupId,
             Long chatRoomId,
             String courseId,
             String courseType,
             String courseName,
             String difficultyLevel,
             Integer status,
             Date startTime,
             Date endTime,
             Date actualStartTime,
             Date actualEndTime,
             Long classNum){

         this.id=id;
        this.createTime=createTime;
        this.teacherId=teacherId;
        this.teacherName=teacherName;
        this. groupName=groupName;
        this. groupId=groupId;
        this.chatRoomId=chatRoomId;
        this.courseId=courseId;
        this.courseType=courseType;
        this. courseName=courseName;
        this.difficultyLevel=difficultyLevel;
        this.status=status;
        this. startTime=startTime;
        this. endTime=endTime;
        this. actualStartTime=actualStartTime;
        this.actualEndTime=actualEndTime;
        this. classNum=classNum ;

    }

    private Long id;
    private Date createTime;

    private Long teacherId;

    private String teacherName;

    private String groupName;

    private String groupId;

    private Long chatRoomId;

    private String courseId;

    private String courseType;
    private String courseName;
    private String difficultyLevel;
    private Integer status;
    private Date startTime;

    private Date endTime;

    private Date actualStartTime;

    private Date actualEndTime;
    private Long classNum = 0L;   //课程数量  人数

}
