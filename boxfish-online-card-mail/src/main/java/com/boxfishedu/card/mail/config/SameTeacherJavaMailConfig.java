package com.boxfishedu.card.mail.config;

import com.boxfishedu.card.mail.service.CardMimeMailSender;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.util.List;

/**
 * Created by jiaozijun on 17/4/5.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mail.sameTeacher1Month")
public class SameTeacherJavaMailConfig {

    private List<String> recipients;

    private String sender;

    private String subject;

    @Bean(name = "sameTeacherTemplate")
    public SimpleMailMessage templateMessage() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(sender);
        simpleMailMessage.setSubject(subject);
        return simpleMailMessage;
    }

    @Autowired
    public void initMailSender(JavaMailSender mailSender, TemplateEngine templateEngine) {
        CardMimeMailSender.initMailSender(mailSender, templateEngine);
    }

}
