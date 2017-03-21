package com.boxfishedu.card.fixes;

import com.boxfishedu.card.fixes.entity.mongo.ScheduleCourseInfoMorphiaRepository;
import com.boxfishedu.card.fixes.service.BaseTimeSlotService;
import com.boxfishedu.card.fixes.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by LuoLiBing on 16/10/17.
 */
@RestController
@EnableScheduling
@SpringBootApplication
public class Application implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private ScheduleCourseInfoMorphiaRepository repository;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private BaseTimeSlotService baseTimeSlotService;

//    @RequestMapping(value = "/courseInfo/synchronous/{courseId}", method = RequestMethod.GET)
    public void courseInfoSynchronous(@PathVariable String courseId) {
        workOrderService.synchronousCourseInfoById(courseId);
    }

//    @RequestMapping(value = "/fixes/{type}", method = RequestMethod.PUT)
    public void fixes(@PathVariable Integer type) {
        switch (type) {
            case 0: repository.updateCourseInfos(); break;
            case 1: repository.updateCourseDifficultys(); break;
            case 2: workOrderService.handleAllDifferent(); break;
            case 3: workOrderService.handleScheduleCourseInfo(); break;
            case 4: repository.updateCourseEnglishNames(); break;
        }
        System.out.println("finish fixes");
    }

    @RequestMapping(value = "/baseTime/init/{days}")
    public Object initBaseTimeSlots(@PathVariable Integer days) {
        baseTimeSlotService.initBaseTimeSlots(days);
        return ResponseEntity.ok().build();
    }


    //课程修补程序
    @Scheduled(cron = "0 30 2 * * ?")
//    @Scheduled(cron = "0/50 * * * * ?")
    public void synchronousCourseInfo() {
        LocalDateTime now = LocalDateTime.now();
        repository.updateCourseInfosByDateRange(
                convertToDate(now), convertToDate(now.plusWeeks(1))
        );
    }


    @RequestMapping(value = "/handle/thumbnail", method = RequestMethod.GET)
    public void handleThumbnail() {
        workOrderService.handlThumbnails();
    }

    private static Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public void run(String... args) throws Exception {
//        repository.updateCourseInfoByFile();
//        workOrderService.handlThumbnails();
    }
}
