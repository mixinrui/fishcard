package com.boxfishedu.card.comment.manage.config;

import org.jdto.DTOBinder;
import org.jdto.spring.SpringDTOBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public DTOBinder dtoBinder() {
       return new SpringDTOBinder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
