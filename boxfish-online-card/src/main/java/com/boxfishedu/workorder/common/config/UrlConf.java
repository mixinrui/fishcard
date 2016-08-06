package com.boxfishedu.workorder.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    @Value("${interface.address.fishcard_service}")
    private String fishcard_service;
    @Value("${interface.address.course_online_service}")
    private String course_online_service;

    @Value("${interface.address.order_service}")
    private String order_service;

    @Value("${parameter.thumbnail_server}")
    private String thumbnail_server;
    @Value("${interface.address.auth_user}")
    private String auth_user;

    @Value("${interface.address.data_analysis_service}")
    private String data_analysis_service;
}
