package com.boxfishedu.card.mail.config;

import org.jdto.DTOBinder;
import org.jdto.spring.SpringDTOBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by LuoLiBing on 16/9/1.
 */
@Configuration
@EnableScheduling
public class ApplicationConfig {

    @Bean
    public DTOBinder dtoBinder() {
        return new SpringDTOBinder();
    }
}
