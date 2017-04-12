package com.boxfishedu.card.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAutoConfiguration
@SpringBootApplication
@ServletComponentScan
public class HealthApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(HealthApplication.class, args);
    }
}
