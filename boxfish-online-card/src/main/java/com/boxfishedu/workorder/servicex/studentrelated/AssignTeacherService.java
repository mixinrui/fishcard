package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by jiaozijun on 16/12/14.
 */
@Component
public class AssignTeacherService {


    @Autowired
    private CourseScheduleService courseScheduleService;

    public JsonResultModel getAssginTeacherCourseList(Long studentId, Long teacherId, Pageable pageable) {
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findFinishCourseSchedulePage(studentId, pageable);
        trimPage(courseSchedulePage);
        return JsonResultModel.newJsonResultModel(courseSchedulePage);
    }

    private void trimPage(Page<CourseSchedule> page) {
        ((List<CourseSchedule>) page.getContent()).forEach(courseSchedule -> {
            if (courseSchedule.getId() % 2 == 0) {
                courseSchedule.setMatchStatus(1);
            } else {
                courseSchedule.setMatchStatus(2);
            }
        });
    }

}
