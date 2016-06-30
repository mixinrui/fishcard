package com.boxfishedu.workorder.web.view.fishcard;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.view.base.BaseView;
import lombok.Data;
import org.jdto.annotation.DTOTransient;

import java.util.Date;

/**
 * Created by hucl on 16/3/10.
 */
@Data
public class WorkOrderView extends BaseView {
    private Long id;
    private Long orderId;
    private Long studentId;
    private Long serviceId;
    private String studentName;
    private Long teacherId;
    private String teacherName;
    private String startTime;
    private String endTime;
    private Integer status;
    private String statusName;
    private String statusDesc;
    //workorder log里的评论内容
    private String content;
    private String evaluationToTeacher;
    private String evaluationToStudent;
    private String courseId;
    private String courseName;
    private Long skuId;
    private String orderCode;
    private Integer timeSlotId;
    @DTOTransient
    private ServiceView service;
    @DTOTransient
    private WorkOrderLogView[] workOrderLogs;

    public WorkOrderView(){

    }

    public WorkOrderView(Long serviceId,Long skuId,String orderCode){
        this.serviceId=serviceId;
        this.skuId=skuId;
        this.orderCode=orderCode;
    }


    public WorkOrderView(Long id,Long orderId, Long studentId, Long serviceId, String studentName,
                         Long teacherId, String teacherName, Date startTime, Date endTime, Integer status,
                         String courseId, String courseName, Long skuId, String orderCode) {
        this.id=id;
        this.orderId = orderId;
        this.studentId = studentId;
        this.serviceId = serviceId;
        this.studentName = studentName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        if(null!=startTime) {
            this.startTime = DateUtil.Date2String(startTime);
        }
        if(null!=endTime) {
            this.endTime = DateUtil.Date2String(endTime);
        }
        this.status = status;
        this.courseId = courseId;
        this.courseName = courseName;
        this.skuId = skuId;
        this.orderCode = orderCode;
    }

    public WorkOrderView(WorkOrder workOrder, Long serviceId,Long skuId,String orderCode) {
//        BeanUtils.copyProperties(this,workOrder);
        this.serviceId=serviceId;
        this.skuId=skuId;
        this.orderCode=orderCode;
    }

    public WorkOrderView(WorkOrder workOrder, Service service){
        this.serviceId=service.getId();
        this.skuId=service.getSkuId();
        this.orderCode=service.getOrderCode();
    }
}
