package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.param.TeacherChangeParam;
import com.boxfishedu.workorder.web.view.course.CourseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by hucl on 16/5/10.
 */
@Component
public class FishCardModifyServiceX {
    @Autowired
    private ServeService serveService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UrlConf urlConf;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //选择五门课程供给学生选择
    public JsonResultModel getCoursesForUpdate(Long studentId) {
        List<CourseView> courseViews = serveService.getCoursesForUpdate(studentId, 5);
        return JsonResultModel.newJsonResultModel(courseViews);
    }

    //TODO:目前为假数据,需要进一步联调
    public JsonResultModel changeTeacher(TeacherChangeParam teacherChangeParam) {
        //获取对应的workorder和courseschedule
        WorkOrder workOrder = workOrderService.findOne(teacherChangeParam.getWorkOrderId());
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(teacherChangeParam.getWorkOrderId());

        if ((null == workOrder) || (null == courseSchedule)) {
            throw new BusinessException("无对应的记录,请检查所传参数是否合法");
        }
        //对workorder和courseschedule做控制
        workOrder.setTeacherId(teacherChangeParam.getTeacherId());
        workOrder.setTeacherName(teacherChangeParam.getTeacherName());
        courseSchedule.setTeacherId(teacherChangeParam.getTeacherId());

        //通知师生运营更换老师
        teacherStudentRequester.notifyChangeTeacher(workOrder);

        //更新新的教师到workorder和courseschedule,此处做事务控制
        workOrderService.updateWorkOrderAndSchedule(workOrder, courseSchedule);
        //返回结果
        return JsonResultModel.newJsonResultModel(null);
    }
}
