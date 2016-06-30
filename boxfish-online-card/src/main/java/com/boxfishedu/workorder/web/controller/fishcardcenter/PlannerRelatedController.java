package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.servicex.fishcardcenter.PlannerRelatedServiceX;
import com.boxfishedu.workorder.web.view.course.CourseView;
import com.boxfishedu.workorder.web.view.fishcard.ServiceView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/3/31.
 * 与课程规划师相关的接口,主要为后台管理相关的接口
 */
@CrossOrigin
@RestController
@RequestMapping("/planner")
public class PlannerRelatedController {
    @Autowired
    private PlannerRelatedServiceX plannerRelatedServiceX;
    /**
     * 根据订单id获取服务列表
     *
     * @param orderId 订单id号
     * @return 返回服务列表
     */
    @RequestMapping(value = "/order/{order_id}/services", method = RequestMethod.GET)
    public JsonResultModel getServicesByOrder(@PathVariable("order_id") Long orderId){
        List<ServiceView> serviceViewList = plannerRelatedServiceX.getServicesByOrder(orderId);
        return JsonResultModel.newJsonResultModel(serviceViewList);
    }

    /**
     * 根据订单号与指定的类型获取工单列表
     *
     * @param orderId
     * @param serviceType 服务类型:1.在线授课 2.在线答疑 3.课程规划 4.欧美外教
     * @return 返回该订单里特定服务的所有工单
     * @param{page:1,size:10}
     */
    @RequestMapping(value = "/order/{order_id}/service/{service_type}/workorders", method = RequestMethod.GET)
    public JsonResultModel getServiceWorkOrdersByOrder(@PathVariable("order_id") Long orderId,
                                                       @PathVariable("service_type") Long serviceType,
                                                       Pageable pageable) {
        JsonResultModel jsonResultModel = plannerRelatedServiceX.getWorkOrdersByOrder(orderId, serviceType, pageable);
        return jsonResultModel;
    }

    /**
     * 根据workorder_id获取教师列表,在换教师部分会用到.
     *
     * @param workorderId
     * @param pageable:分页的接口,page:当前页数,size:每页大小
     * @return 返回分页后的可选教师列表
     */
    @RequestMapping(value = "/workorder/{workorder_id}/avaliable/teachers", method = RequestMethod.GET)
    public JsonResultModel getTeachersByWorkOrder(@PathVariable("workorder_id") Long workorderId,
                                                  Pageable pageable) {
        return plannerRelatedServiceX.getTeachersByWorkOrder(workorderId, pageable);
    }

    /**
     * 更换推荐课程
     *
     * @param idsMap {"ids": [1,2,3,4]}
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/workorder/courses", method = RequestMethod.PUT)
    public JsonResultModel updateCourseIntoWorkOrder(@RequestBody Map<String,List<Long>> idsMap) {
        List<Long> workorderIds=idsMap.get("ids");
        List<CourseView> courseViews=plannerRelatedServiceX.updateCoursesIntoWorkorders(workorderIds);
        return JsonResultModel.newJsonResultModel(courseViews);
    }

    /**
     * 查询某规划师的所有工单状态及数量;author:zhihao
     *
     * @param dateFlag 日期区间标识:today-当天,week-本周
     */
    @RequestMapping("/listStatus/{plannerID}/{dateFlag}")
    public JsonResultModel listAmount(@PathVariable Long plannerID, @PathVariable String dateFlag) {
        String workOrders = plannerRelatedServiceX.getAmountByPlanner(plannerID, dateFlag);
        return JsonResultModel.newJsonResultModel(workOrders);
    }

    /**
     * 根据工单状态分页查询某规划师的数据;author:zhihao
     */
    @RequestMapping("/page/{plannerID}/status/{status}")
    public JsonResultModel page(@PathVariable Long plannerID, @PathVariable String status, Pageable pageable, String dateFlag) {
        return JsonResultModel.newJsonResultModel(plannerRelatedServiceX.getPageByPlanner(pageable, plannerID, status, dateFlag));
    }
}
