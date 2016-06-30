package com.boxfishedu.workorder.configuration;

import com.boxfishedu.workorder.common.util.RestTemplateUtil;
import org.jdto.DTOBinder;
import org.jdto.spring.SpringDTOBinder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.Filter;

/**
 * Created by LuoLiBing on 16/4/28.
 */
@Configuration
@EnableCaching
@EnableScheduling
//@EnableRetry
public class ApplicationConfig {

    @Bean
    public DTOBinder dtoBinder() {
        return new SpringDTOBinder();
    }

    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate= RestTemplateUtil.getTemplate();
        return restTemplate;
    }

    /**
     * etag 值的filter
     * @return
     */
    @Bean
    public Filter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    /********** 需要retry的service ************/

//    @Bean
//    public ServiceSDK serviceSDK() {
//        return new ServiceSDK();
//    }

    /********** 需要retry的service ************/
}
