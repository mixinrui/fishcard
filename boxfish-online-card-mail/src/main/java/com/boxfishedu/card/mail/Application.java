package com.boxfishedu.card.mail;

import com.boxfishedu.card.mail.utils.ApplicationContextProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by LuoLiBing on 16/8/31.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ApplicationContextProvider.createInstance(SpringApplication.run(Application.class, args));
    }
}
