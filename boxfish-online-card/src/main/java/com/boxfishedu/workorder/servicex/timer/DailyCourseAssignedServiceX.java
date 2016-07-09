package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.service.timer.DailyCourseAssignedService;
import com.boxfishedu.workorder.web.view.fishcard.TeacherAssignedCourseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/7/7.
 */
@Component
public class DailyCourseAssignedServiceX {
    @Autowired
    private DailyCourseAssignedService dailyCourseAssignedService;

    public List<TeacherAssignedCourseView> getCardAssignedDaily() {
        return dailyCourseAssignedService.getCardAssignedDaily();
    }

}
