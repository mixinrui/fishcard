package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.mall.enums.OrderChannelDesc;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.mysql.jdbc.StringUtils;

import java.util.Date;

/**
 * Created by jiaozijun on 16/8/5.
 */
public class WorkOrderViewExcel {
    private Long id;
    private String createTime;
    private Long studentId;
    private String studentName;
    private Long teacherId;
    private String teacherName;
    private String courseType;
    private String couserName;
    private String status;
    private String planStartTime;
    private String planEndTime;
    private String realStartTime;
    private String realEndTime;
    private String orderCode;

    private String orderType;

    private String hasAddCourse;//是否补过课


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        if(null == createTime){
            this.createTime ="";
        }else {
            this.createTime = DateUtil.Date2String(createTime);
        }
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getCouserName() {
        return couserName;
    }

    public void setCouserName(String couserName) {
        this.couserName = couserName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = FishCardStatusEnum.get(status).getDesc();
    }

    public String getPlanStartTime() {
        return planStartTime;
    }

    public void setPlanStartTime(Date planStartTime) {
        if(null == planStartTime){
            this.planStartTime ="";
        }else {
            this.planStartTime = DateUtil.Date2String(planStartTime);
        }
    }

    public String getPlanEndTime() {
        return planEndTime;
    }

    public void setPlanEndTime(Date planEndTime) {
        if(null == planEndTime){
            this.planEndTime ="";
        }else {
            this.planEndTime = DateUtil.Date2String(planEndTime);
        }
    }

    public String getRealStartTime() {
        return realStartTime;
    }

    public void setRealStartTime(Date realStartTime) {
        if(null == realStartTime){
            this.realStartTime ="";
        }else {
            this.realStartTime = DateUtil.Date2String(realStartTime);
        }
    }

    public String getRealEndTime() {
        return realEndTime;
    }

    public void setRealEndTime(Date actualEndTime) {
        if(null == actualEndTime){
            this.realEndTime ="";
        }else {
            this.realEndTime = DateUtil.Date2String(actualEndTime);
        }

    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getHasAddCourse() {
        return hasAddCourse;
    }

    public void setHasAddCourse(Short makeUpFlag) {
        if(null==makeUpFlag){
            this.hasAddCourse="否";
        }else {
            this.hasAddCourse="是";
        }
    }



    public String setOrderType(String orderType) {
        if(StringUtils.isNullOrEmpty(OrderChannelDesc.get(orderType).getDesc() ))
            return orderType;
        return this.orderType = OrderChannelDesc.get(orderType).getDesc();
    }
    public String getOrderType() {
        return orderType;
    }


}
