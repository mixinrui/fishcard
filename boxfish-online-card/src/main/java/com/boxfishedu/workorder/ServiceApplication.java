package com.boxfishedu.workorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Created by JiangTengfei on 15/11/16.
 * 配置相关移到configuration包下
 */
@EnableScheduling
//@SpringBootApplication(scanBasePackages = {"com.boxfishedu"})
@SpringBootApplication
@ServletComponentScan
public class ServiceApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ServiceApplication.class, args);
//        String[] beanNames = ctx.getBeanDefinitionNames();
//        Arrays.sort(beanNames);
//        for (String beanName : beanNames) {
//            System.out.println(beanName);
//        }
    }
}
