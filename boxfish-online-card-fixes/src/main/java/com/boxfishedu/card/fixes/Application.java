package com.boxfishedu.card.fixes;

import com.boxfishedu.card.fixes.entity.mongo.ScheduleCourseInfoMorphiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by LuoLiBing on 16/10/17.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    private int type = 0;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private ScheduleCourseInfoMorphiaRepository repository;

    @Override
    public void run(String... args) throws Exception {
        try {
            switch (type) {
                case 0: repository.updateCourseInfos(); break;
                case 1: repository.updateCourseDifficultys(); break;
            }
            System.out.println("finish fixes");
            Runtime.getRuntime().exit(0);
        } catch (Exception e) {
            Runtime.getRuntime().exit(1);
        }
    }
}
