package com.boxfishedu.card.mail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;

import java.util.List;

/**
 * Created by LuoLiBing on 16/9/1.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mail.noCourseIdOver24HoursRecipients")
public class WorkOrderJavaMailConfig {

    private List<String> recipients;

    private String sender;

    private String subject;

    @Bean(name = "noCourseIdTemplate")
    public SimpleMailMessage templateMessage() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(sender);
        simpleMailMessage.setSubject(subject);
        return simpleMailMessage;
    }

}
