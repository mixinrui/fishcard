package com.boxfishedu.workorder.web.param.bebase3;

import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by wangshichao on 16/4/12.
 */
@Data
public class ScheduleModelSt implements Serializable{


    private Long id;
    private Long workOrderId;
    @NotNull(message = "日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+08:00")
    private Date day;

    private String courseType;

    @NotNull(message = "时间片Id不能为空")
    private Integer slotId;

    private Integer roleId;

    private StStudentApplyRecords.MatchStatus matchStatus   ;// '申请状态  0 不匹配  1 匹配  2 无时间片待匹配
    private Long oldTeacherId;//原老师id 需要指定老师的课的原来老师id  B老师


    private Long grabedStudentId; //  被抢学生Id;

    private Long grabedId;//课程表id

    @NotNull(message = "日期不能为空")
    private Long grabedDay;

    private String grabedcourseType;

    @NotNull(message = "时间片Id不能为空")
    private Integer grabedSlotId;

    private Integer grabedRoleId;

    private Long grabedWorkOrderId;
    public ScheduleModelSt(){}
    public ScheduleModelSt(CourseSchedule courseSchedule) {
        this.day = courseSchedule.getClassDate();
        this.slotId = courseSchedule.getTimeSlotId();
        this.courseType = courseSchedule.getCourseType();
        this.roleId = courseSchedule.getRoleId();
        this.oldTeacherId = courseSchedule.getTeacherId();
        this.id = courseSchedule.getId();
        this.workOrderId = courseSchedule.getWorkorderId();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
