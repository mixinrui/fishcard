package com.boxfishedu.card.mail.service;

import com.boxfishedu.card.mail.config.SameTeacherJavaMailConfig;
import com.boxfishedu.card.mail.config.WorkOrderJavaMailConfig;
import com.boxfishedu.card.mail.entity.jpa.WorkOrderJpaRepository;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 相同老师同一个时间片 有两节课
 * Created by jiaozijun on 17/04/05.
 */
@Service
public class SameTeacherNotify {

    private final static Logger logger = LoggerFactory.getLogger(SameTeacherNotify.class);

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    @Qualifier(value = "sameTeacherTemplate")
    private SimpleMailMessage templateMessage;

    @Autowired
    private SameTeacherJavaMailConfig javaMailConfig;

    @Scheduled(cron = "0 30 4 * * *")
    public void notifySameTeacher() throws MessagingException {
        List<Map<String, Object>> workOrders = getSameTeacherList();
        logger.info("notifySameTeacher workOrder, find out size=[{}]", workOrders == null ? 0 : workOrders.size());
        if(CollectionUtils.isNotEmpty(workOrders)) {
            new CardMimeMailSender()
                    .createMimeMail(templateMessage)
                    .setRecipients(javaMailConfig.getRecipients())
                    .setMailContent("same_teacher_notify", Collections.singletonMap("workOrders", workOrders))
                    .sendMail();
        }
    }

    private List<Map<String, Object>> getSameTeacherList() {
        return workOrderJpaRepository.getSameClassOfTeacher();
    }
}
