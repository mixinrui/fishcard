package com.boxfishedu.workorder.web.param.bebase3;

import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by wangshichao on 16/4/12.
 */
@Data
public class ScheduleModelSt {

    private Long Id;

    @NotNull(message = "日期不能为空")
    private Long day;

    private String courseType;

    @NotNull(message = "时间片Id不能为空")
    private Integer slotId;

    private Integer roleId;

    private Integer matchStatus   ;// '申请状态  0 不匹配  1 匹配  2 无时间片待匹配
    private Long oldTeacherId;//原老师id 需要指定老师的课的原来老师id  B老师


    private Long grabedStudentId; //  被抢学生Id;

    private Long grabedId;//课程表id

    @NotNull(message = "日期不能为空")
    private Long grabedday;

    private String grabedcourseType;

    @NotNull(message = "时间片Id不能为空")
    private Integer grabedSlotId;

    private Integer grabedRoleId;

    public ScheduleModelSt(CourseSchedule courseSchedule) {
        this.day = courseSchedule.getClassDate().getTime();
        this.slotId = courseSchedule.getTimeSlotId();
        this.courseType = courseSchedule.getCourseType();
        this.roleId = courseSchedule.getRoleId();
        this.oldTeacherId = courseSchedule.getTeacherId();

//        this.id = courseSchedule.getId();
    }
}
