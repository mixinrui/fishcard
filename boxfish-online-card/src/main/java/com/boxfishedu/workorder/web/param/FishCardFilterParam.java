package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.util.Date;

/**
 * Created by hucl on 16/5/9.
 */
@Data
public class FishCardFilterParam {
    private Long studentId;
    private Long teacherId;
    private String orderCode;
    private String beginDate;
    private String endDate;
    private Date beginDateFormat;
    private Date endDateFormat;
    private Integer[] status;
    private String statuses;// 以数组的方式传递

    private String teacherName;
    private String createBeginDate;
    private String createEndDate;
    private Date createBeginDateFormat;
    private Date createEndDateFormat;
    /** desc asc **/
    private String startTimeSort;
    /** desc asc **/
    private String actualStartTimeSort;
    /** 课程类型 数组形式 **/
    private String courseType;


    /** 鱼卡是否确认 1 未确认   0  已经确认  **/
    private String confirmFlag;

    /**  显示退款页面 取值   before  after   点击退款前  和 点击退款后 **/
    private String rechargeType;

    private Integer rechargeValue;


}
