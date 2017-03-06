package com.boxfishedu.workorder.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/3/16.
 */
@Data
@Component
public class UrlConf {
    @Value("${interface.address.teacher_service}")
    private String teacher_service;
    @Value("${interface.address.teacher_service_admin}")
    private String teacher_service_admin;//师生运营后台服务
    @Value("${interface.address.course_recommended_service}")
    private String course_recommended_service;

    // 新课程推荐
    @Value(("${interface.address.course_wudaokou_recommend_service}"))
    private String course_wudaokou_recommend_service;

    @Value(("${interface.address.course_wudaokou_detail_service}"))
    private String course_wudaokou_detail_service;

    @Value("${interface.address.fishcard_service}")
    private String fishcard_service;
    @Value("${interface.address.course_online_service}")
    private String course_online_service;

    @Value("${interface.address.msg_push_url}")
    private String msg_push_url;


    @Value("${interface.address.order_service}")
    private String order_service;

    @Value("${parameter.thumbnail_server}")
    private String thumbnail_server;
    @Value("${interface.address.auth_user}")
    private String auth_user;

    @Value("${interface.address.data_analysis_service}")
    private String data_analysis_service;

    /** zhong老师接口 **/
    @Value("${interface.address.student_teacher_relation}")
    private String student_teacher_relation;

    /** 学生旷课扣积分**/
    @Value("${interface.address.absenteeism_deduct_score}")
    private String absenteeism_deduct_score;

    /** 支付系统 **/
    @Value("${interface.address.pay_service}")
    private String pay_service;

    /** 查询课程类型和难度 **/
    @Value("${interface.address.course_type_and_difficulty}")
    private String course_type_and_difficulty;

    @Value("${interface.address.login_filter_url}")
    private String login_filter_url;//登录验证URL

    //教师图像
    @Value("${interface.address.teacher_photo}")
    private String teacher_photo;//登录验证URL

    //种老师那边系统域名
    @Value("${interface.address.resource_url}")
    private String resource_url;//登录验证URL

    // 邮件服务上报token
    @Value("${parameter.mail_token}")
    private String mailToken;

    // 邮件上报token
    @Value("${parameter.recipients}")
    private List<String> recipients;

    // 会员
    @Value("${interface.address.memberUrl}")
    private String memberUrl;

    // 免费补订单验证串couponCode
    @Value("${interface.address.order_check_couponCode}")
    private String order_check_couponCode;

    // 免费补订单验证串memberKey
    @Value("${interface.address.order_check_small_class_key}")
    private String order_check_small_class_key;

    //向用户完成课程系统发送一对一的终态鱼卡
    @Value("${parameter.fishcard_status}")
    private String fishcard_status;


    @Value("${parameter.fishcard_public_small_status}")
    private String fishcard_public_small_status;

    //小班课 公开课 计算累计时间 单位15分钟
    @Value("${parameter.total_minutes}")
    private Long total_minutes;

}
