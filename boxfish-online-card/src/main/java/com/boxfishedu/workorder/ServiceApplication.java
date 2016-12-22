package com.boxfishedu.workorder;

import com.boxfishedu.workorder.common.config.ApplicationContextAccessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Created by JiangTengfei on 15/11/16.
 * 配置相关移到configuration包下
 */
@EnableScheduling
@SpringBootApplication
@ServletComponentScan
public class ServiceApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        ApplicationContextAccessor.setContext(SpringApplication.run(ServiceApplication.class, args));
    }
}
