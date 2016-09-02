package com.boxfishedu.card.comment.manage.config;

import org.jdto.DTOBinder;
import org.jdto.spring.SpringDTOBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public DTOBinder dtoBinder() {
       return new SpringDTOBinder();
    }
}
