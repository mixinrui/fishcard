package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.course.CourseView;
import com.boxfishedu.workorder.web.view.fishcard.ServiceView;
import com.boxfishedu.workorder.web.view.fishcard.WorkOrderView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.apache.commons.lang3.StringUtils;
import org.jdto.DTOBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created by hucl on 16/3/31.
 */
@org.springframework.stereotype.Service
public class PlannerRelatedServiceX {
    @Autowired
    private ServeService serveService;
    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private DTOBinder dtoBinder;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private DTOBinder binder;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private CourseScheduleService courseScheduleService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //查找订单中的所有Services
    public List<ServiceView> getServicesByOrder(Long orderId) throws BoxfishException {
        List<Service> serviceList = serveService.findByOrderId(orderId);
        if (CollectionUtils.isEmpty(serviceList)) {
            throw new BusinessException("没有对应的服务列表");
        }
        List<ServiceView> serviceViewList = new ArrayList<>(serviceList.size());
        for (Service service : serviceList) {
            ServiceView serviceView = binder.bindFromBusinessObject(ServiceView.class, service);
            serviceViewList.add(serviceView);
        }
        return serviceViewList;
    }

    //根据订单号和服务的类型获取对应的工单列表
    public JsonResultModel getWorkOrdersByOrder(Long orderId, Long serviceType, Pageable pageable) throws BoxfishException {
        //根据orderId,serviceType(skuid)获取Service对象
        Service service = serveService.findTop1ByOrderIdAndSkuId(orderId, serviceType);
        if (null == service) {
            throw new BusinessException("不存在对应的服务");
        }
        //根据service的id获取对应的工单列表
        Page<WorkOrder> workOrderPage = workOrderService.findByServiceIdOrderByStartTime(service.getId(), pageable);
        if (null == workOrderPage || 0 == workOrderPage.getTotalElements()) {
            throw new BusinessException("服务不存在对应的工单");
        }
        //将工单列表转为WorkOrderView列表
        List<WorkOrderView> workOrderViews = dtoBinder.bindFromBusinessObjectList(
                WorkOrderView.class, workOrderPage.getContent());
        for (WorkOrderView workOrderView : workOrderViews) {
            workOrderView.setServiceId(service.getId());
            workOrderView.setStatusDesc(FishCardStatusEnum.getDesc(workOrderView.getStatus()));
        }
        Page<WorkOrderView> workOrderViewPage = new PageImpl<>(workOrderViews, pageable, workOrderPage.getTotalElements());
        return JsonResultModel.newJsonResultModel(workOrderViewPage);
    }

    public JsonResultModel getTeachersByWorkOrder(Long workorderId, Pageable pageable) throws BoxfishException {
        WorkOrder workOrder=workOrderService.findOne(workorderId);
        CourseSchedule courseSchedule=courseScheduleService.findByWorkOrderId(workorderId);
        if(null==workOrder){
            throw new BusinessException("对应工单号无记录");
        }
        if(null==courseSchedule){
            throw new BusinessException("无对应的课程规划表");
        }
        //根据课程,timeslot等信息获取教师资源
        return teacherStudentRequester.getPageableTeachers(courseSchedule,pageable);
    }

    @Transactional
    public void updateTeacherIntoWorkorder(Long workorderId, TeacherView teacherView) throws BoxfishException {
        WorkOrder workOrder = workOrderService.findOne(workorderId);
        workOrder.setTeacherId(teacherView.getTeacherId());
        workOrder.setTeacherName(teacherView.getTeacherName());
        workOrderService.save(workOrder);
        //向教师服务中心发起更换老师请求,更换答疑师,规划师是同一个接口;但是更换老师则是另外的接口
        /***************************/
    }


    @Transactional
    public List<CourseView> updateCoursesIntoWorkorders(List<Long> workorderIds) throws BoxfishException {
        if (CollectionUtils.isEmpty(workorderIds)) {
            throw new ValidationException("鱼卡id不能为空");
        }
        List<CourseView> courseViews=new ArrayList<>();
        workorderIds.forEach(workOrderId->{
            CourseView courseView=updateCourseIntoWorkorder(workOrderId);
            courseViews.add(courseView);
        });
        return courseViews;
    }

    /**
     * 更换推荐课程
     *
     * @param workorderId
     * @throws BoxfishException
     */
    public CourseView updateCourseIntoWorkorder(Long workorderId) throws BoxfishException {
        WorkOrder workOrder = workOrderService.findOne(workorderId);
        Service service = workOrder.getService();
        CourseView courseView = serveService.getCourseByService(service);
        if (null == courseView) {
            throw new BusinessException("获取推荐课程失败");
        }
        workOrder.setCourseId(courseView.getBookSectionId());
        workOrder.setCourseName(courseView.getName());
        workOrderService.save(workOrder);
        return courseView;
        //TODO:向教师服务中心发起更换课程请求
        /*****************************/
    }

    private Map<String, Date> getDateInterval(String dateFlag) {
        if (StringUtils.isEmpty(dateFlag)) {
            throw new BusinessException("日期区间标识不能为空!");
        }

        Map<String, Date> interval = new HashMap<>();
        Date beginDate = null;
        Date endDate = null;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        switch (dateFlag) {
            case "today":
                beginDate = calendar.getTime();
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDate = calendar.getTime();
                break;
            case "yesterday":
                break;
            case "thisWeek":
                int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 2;
                calendar.add(Calendar.DAY_OF_WEEK, -weekday);
                beginDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_WEEK, 6);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDate = calendar.getTime();
                break;
            case "lastWeek":
                break;
            case "thisMonth":
                break;
            case "lastMonth":
                break;
            default:
                throw new BusinessException("非法的日期区间标识!");
        }

        interval.put("beginDate", beginDate);
        interval.put("endDate", endDate);

        return interval;
    }

    public String getAmountByPlanner(Long plannerID, String dateFlag) {
        Map<String, Date> dateInterval = getDateInterval(dateFlag);
        Date beginDate = dateInterval.get("beginDate");
        Date endDate = dateInterval.get("endDate");

        int exceptionAmount = 0;
        int normalAmount = 0;
        //查询异常状态的工单
        List<WorkOrder> allWorkOrders = workOrderService.
                findByTeacherIdAndCreateTimeBetween(plannerID, beginDate, endDate);
        for (WorkOrder workOrder : allWorkOrders) {
            Integer status = workOrder.getStatus();
            if (FishCardStatusEnum.EXCEPTION.getCode() == status) {
                exceptionAmount++;
            } else {
                normalAmount++;
            }
        }
        String exception = "[{\"status\":\"exception\",\"label\":\"紧急\",\"amount\":" + exceptionAmount + "},";
        String normal = "{\"status\":\"normal\",\"label\":\"正常\",\"amount\":" + normalAmount + "},";
        String total = "{\"status\":\"all\",\"label\":\"所有\",\"amount\":" + (exceptionAmount + normalAmount) + "}]";

        return exception + normal + total;
    }

    public Page<WorkOrderView> getPageByPlanner(Pageable pageable, Long plannerID, String status, String dateFlag) {
        Map<String, Date> dateInterval = this.getDateInterval(dateFlag);
        Date beginDate = dateInterval.get("beginDate");
        Date endDate = dateInterval.get("endDate");

        int statusCode = FishCardStatusEnum.EXCEPTION.getCode();
        Page<WorkOrder> page = null;
        switch (status) {
            case "exception":
                page = workOrderService.findByTeacherIdAndStatusAndCreateTimeBetween(pageable, plannerID, statusCode, beginDate, endDate);
                break;
            case "normal":
                page = workOrderService.findByTeacherIdAndStatusLessThanAndCreateTimeBetween(pageable, plannerID, statusCode, beginDate, endDate);
                break;
            default:
                page = workOrderService.findByTeacherIdAndCreateTimeBetween(pageable, plannerID, beginDate, endDate);
        }
        List<WorkOrderView> list = dtoBinder.bindFromBusinessObjectList(WorkOrderView.class, page.getContent());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }
}
