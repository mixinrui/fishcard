package com.boxfishedu.workorder.web.param;

import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import lombok.Data;

/**
 * Created by hucl on 16/4/21.
 */
@Data
public class ScheduleModel {
        private Long day;
        private Integer slotId;
        private String courseType;
        private Integer roleId;
        //此处id为请求方发向师生运营组的数据请求参数;此处暂时使用courseschedule的id作为标示
        private Long id;
        private String classType;
        public ScheduleModel() {}

        public ScheduleModel(CourseSchedule courseSchedule) {
                this.day = courseSchedule.getClassDate().getTime();
                this.slotId = courseSchedule.getTimeSlotId();
                this.courseType = courseSchedule.getCourseType();
                this.roleId = courseSchedule.getRoleId();
                this.id = courseSchedule.getId();
        }
}
