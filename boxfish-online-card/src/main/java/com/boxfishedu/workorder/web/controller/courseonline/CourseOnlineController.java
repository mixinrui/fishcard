package com.boxfishedu.workorder.web.controller.courseonline;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.courseonline.CourseOnlineServiceX;
import com.boxfishedu.workorder.servicex.teacherrelated.TeacherAppRelatedServiceX;
import com.boxfishedu.workorder.web.view.fishcard.WorkOrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by hucl on 16/3/22.
 * 与上课相关的流程在此处理
 */
@CrossOrigin
@RestController
@RequestMapping("/coursing")
public class  CourseOnlineController {
    @Autowired
    private CourseOnlineServiceX courseOnlineServiceX;

    @Autowired
    private TeacherAppRelatedServiceX teacherAppRelatedServiceX;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CourseScheduleService courseScheduleService;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    /**
     * 通过教师id获取其对应时间段的工单信息
     *
     * @param teacherId
     * @param start     开始日期,不得带时分秒
     * @param end       结束日期,不得带时分秒
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/teacher/{teacher_id}/workorders", method = RequestMethod.GET)
    public JsonResultModel getWorkordersByTeacher(@PathVariable("teacher_id") Long teacherId,
                                                  String start, String end, Pageable pageable) {
        Page<WorkOrder> workOrders = courseOnlineServiceX.getWorkOrdersByTeacher(teacherId, start, end, pageable);
        return JsonResultModel.newJsonResultModel(workOrders);
    }

    /**
     * 老师请求上课,功能为校验工单的有效性
     *
     * @param workOrderId
     * @return
     */
    @RequestMapping(value = "/workorder/{workorder_id}/teacher/class", method = RequestMethod.GET)
    public JsonResultModel teacherRequestClass(@PathVariable("workorder_id") Long workOrderId) {
        Map<String, Object> map = teacherAppRelatedServiceX.isWorkOrderTimeValid(workOrderId);
        return JsonResultModel.newJsonResultModel(map);
    }

    /**
     * 更新鱼卡的状态,同时保存异常的说明
     *
     * @param workOrderView: content为异常内容说明 {"id":10,"status":30,"content":"我是内容"}
     * @return
     */
    @RequestMapping(value = "/workorder/status", method = RequestMethod.PUT)
    public JsonResultModel updateWorkOrderStatus(@RequestBody WorkOrderView workOrderView) {
        courseOnlineServiceX.updateWorkOrderStatus(workOrderView);
        return JsonResultModel.newJsonResultModel(null);
    }

    @RequestMapping(value = "/courseschedule/{workOrderId}")
    public JsonResultModel courseSchedule(@PathVariable Long workOrderId) {
        return JsonResultModel.newJsonResultModel(courseScheduleService.findByWorkOrderId(workOrderId));
    }
}
