package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.util.Date;

/**
 * Created by hucl on 17/2/9.
 */
@Data
public class InstantCardFilterParam {
    private Long id;

    private String tutorType;

    private Long studentId;

    private Long teacherId;

    private String teacherName;

    //学生请求次数
    private Integer studentRequestTimes;

    //请求教师的次数
    private Integer requestTeacherTimes;

    //发起请求,正在匹配老师,未匹配上老师,匹配上老师
    private Integer status;

    //鱼卡id,如果有值,表示已经在这个时间点上匹配过老师
    private Long workorderId;

    private String courseId;

    private String courseType;

    private Date classDate;

    private Long slotId;

    private Integer roleId;

    private Date createTime;

    //返回学生的未匹配结果标志,0:未返回,1:已返回
    private Integer resultReadFlag;

    //返回学生的结果标志,0:未返回,1:已返回
    private Integer matchResultReadFlag;

    //返回学生的结果标志,0:课程表入口,1:其他入口
    private Integer entrance;

    private String groupName;

    private String groupId;

    private Long chatRoomId;
}
