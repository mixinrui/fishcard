package com.boxfishedu.card.fixes;

import com.boxfishedu.card.fixes.entity.mongo.ScheduleCourseInfoMorphiaRepository;
import com.boxfishedu.card.fixes.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

/**
 * Created by LuoLiBing on 16/10/17.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private ScheduleCourseInfoMorphiaRepository repository;

    @Autowired
    private WorkOrderService workOrderService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("参数" + Arrays.toString(args));
        String type;
        // args.length == 0
        if(true) {
            type = "2";
        } else {
            type = args[0];
        }
        try {
            switch (type) {
                case "0": repository.updateCourseInfos(); break;
                case "1": repository.updateCourseDifficultys(); break;
                case "2": workOrderService.handleAllDifferent(); break;
            }
            System.out.println("finish fixes");
            Runtime.getRuntime().exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }
    }
}
