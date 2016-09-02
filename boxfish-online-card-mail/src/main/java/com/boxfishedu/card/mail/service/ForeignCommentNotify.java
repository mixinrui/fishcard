package com.boxfishedu.card.mail.service;

import com.boxfishedu.card.mail.config.JavaMailConfig;
import com.boxfishedu.card.mail.dto.CommentCardDto;
import com.boxfishedu.card.mail.entity.CommentCard;
import com.boxfishedu.card.mail.entity.jpa.CommentCardJpaRepository;
import org.jdto.DTOBinder;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 16/9/1.
 */
@Service
public class ForeignCommentNotify {

    @Autowired
    private CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    @Qualifier(value = "templateMessage")
    private SimpleMailMessage templateMessage;

    @Autowired
    private DTOBinder dtoBinder;

    @Autowired
    private JavaMailConfig javaMailConfig;

    @Autowired
    public void initMailSender(JavaMailSender mailSender, TemplateEngine templateEngine) {
        CardMimeMailSender.initMailSender(mailSender, templateEngine);
    }


    //9-23/2
    @Scheduled(cron = "0 0 9-23/2 * * *")
//    @Scheduled(cron = "5-50/20 * * * * *")
    public void notifyNotAnswerOver12Hours() throws MessagingException {
        List<CommentCard> commentCards = getNotAnswerOver12HoursList();
        new CardMimeMailSender()
                .createMimeMail(templateMessage)
                .setRecipients(javaMailConfig.getRecipients())
                .setMailContent("notanswer_notify",
                        Collections.singletonMap("cards",
                                dtoBinder.bindFromBusinessObjectList(CommentCardDto.class, commentCards)))
                .sendMail();
    }

    private List<CommentCard> getNotAnswerOver12HoursList() {
        LocalDateTime now = LocalDateTime.now();
        Date startTime = Date.from(now.minusHours(12).atZone(ZoneId.systemDefault()).toInstant());
        return commentCardJpaRepository.findNotAnswerOver12Hours(startTime);
    }
}
