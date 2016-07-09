package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.bean.TeachingOnlineMsg;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
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

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    public void batchNotifyTeacherAssignedCourse(){
        this.notifyTeachingOnline(getCardAssignedDaily());
    }

    private List<TeacherAssignedCourseView> getCardAssignedDaily() {
        return dailyCourseAssignedService.getCardAssignedDaily();
    }

    private void notifyTeachingOnline(List<TeacherAssignedCourseView> teacherAssignedCourseViews){
        teacherAssignedCourseViews.forEach(teacherAssignedCourseView -> {
            TeachingOnlineMsg teachingOnlineMsg=new TeachingOnlineMsg();
            teachingOnlineMsg.setPush_title("您今天有"+teacherAssignedCourseView.getCount()+"节新匹配的课程,详情请点击课表查看");
            teachingOnlineMsg.setUser_id(teacherAssignedCourseView.getTeacherId());
            courseOnlineRequester.pushWrappedMsg(teachingOnlineMsg);
        });
    }

}
