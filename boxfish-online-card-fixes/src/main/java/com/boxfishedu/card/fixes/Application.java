package com.boxfishedu.card.fixes;

import com.boxfishedu.card.fixes.entity.mongo.ScheduleCourseInfoMorphiaRepository;
import com.boxfishedu.card.fixes.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by LuoLiBing on 16/10/17.
 */
@RestController
@SpringBootApplication
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private ScheduleCourseInfoMorphiaRepository repository;

    @Autowired
    private WorkOrderService workOrderService;

    @RequestMapping(value = "/courseInfo/synchronous/{courseId}", method = RequestMethod.GET)
    public void courseInfoSynchronous(@PathVariable String courseId) {
        workOrderService.synchronousCourseInfoById(courseId);
    }

    @RequestMapping(value = "/fixes/{type}", method = RequestMethod.PUT)
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
}
