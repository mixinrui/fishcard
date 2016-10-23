package com.boxfishedu.fishcard.timer;

import com.boxfishedu.fishcard.timer.task.NotifyTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@EnableScheduling
@SpringBootApplication
public class ServiceTimerApplication extends SpringBootServletInitializer {

    @Autowired
    private NotifyTimer notifyTimer;

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ServiceTimerApplication.class, args);
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
//            System.out.println(beanName);
        }
    }

    @RequestMapping(value = "/recommend", method = RequestMethod.GET)
    public Object notifyRecommend() {
        notifyTimer.recommendCourses();
        return ResponseEntity.ok().build();
    }

}
