package com.boxfishedu.card.comment.manage;

import com.boxfishedu.card.comment.manage.util.ApplicationContextProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ApplicationContextProvider.createInstance(SpringApplication.run(Application.class, args));
    }
}
