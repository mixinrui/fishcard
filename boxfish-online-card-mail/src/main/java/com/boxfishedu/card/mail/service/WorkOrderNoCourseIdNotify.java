package com.boxfishedu.card.mail.service;

import com.boxfishedu.card.mail.config.WorkOrderJavaMailConfig;
import com.boxfishedu.card.mail.entity.jpa.WorkOrderJpaRepository;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by LuoLiBing on 16/10/31.
 */
@Service
public class WorkOrderNoCourseIdNotify {

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    @Qualifier(value = "noCourseIdTemplate")
    private SimpleMailMessage templateMessage;

    @Autowired
    private WorkOrderJavaMailConfig javaMailConfig;

    @Autowired
    public void initMailSender(JavaMailSender mailSender, TemplateEngine templateEngine) {
        CardMimeMailSender.initMailSender(mailSender, templateEngine);
    }

//    @Scheduled(cron = "0 0 9-23/2 * * *")
    @Scheduled(cron = "0 0 5 * * *")
    public void notifyNoCourseIdTimeOut() throws MessagingException {
        List<Map<String, Object>> workOrders = getNoCourseIdTimeOutList();
        if(CollectionUtils.isNotEmpty(workOrders)) {
            new CardMimeMailSender()
                    .createMimeMail(templateMessage)
                    .setRecipients(javaMailConfig.getRecipients())
                    .setMailContent("nolesson_notify", Collections.singletonMap("workOrders", workOrders))
                    .sendMail();
        }
    }

    private List<Map<String, Object>> getNoCourseIdTimeOutList() {
        LocalDateTime startTime = LocalDateTime.now();
        Date endTime = Date.from(startTime.plusHours(48).atZone(ZoneId.systemDefault()).toInstant());

        return workOrderJpaRepository.findNoCourseIdTimeOutList(
                Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()), endTime);
    }
}
