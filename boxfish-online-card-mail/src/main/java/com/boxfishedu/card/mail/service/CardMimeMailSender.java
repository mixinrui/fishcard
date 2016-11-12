package com.boxfishedu.card.mail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.Assert;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/8/31.
 */
public class CardMimeMailSender {

    private final static Logger logger = LoggerFactory.getLogger(CardMimeMailSender.class);

    private MimeMessage mimeMailMessage;

    private MimeMessageHelper mimeMessageHelper;

    private static JavaMailSender mailSender;

    private static TemplateEngine templateEngine;

    private String content;

    private Context context = new Context();

    public static void initMailSender(JavaMailSender javaMailSender, TemplateEngine _templateEngine) {
        mailSender = javaMailSender;
        templateEngine = _templateEngine;
    }

    /**
     * 创建一个Mail
     * @return
     */
    public CardMimeMailSender createMimeMail(SimpleMailMessage templateMessage) throws MessagingException {
        mimeMailMessage = mailSender.createMimeMessage();
        mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, "UTF-8");
        mimeMessageHelper.setSubject(templateMessage.getSubject());
        mimeMessageHelper.setFrom(templateMessage.getFrom());
        return this;
    }

    /**
     * 添加收件人
     * @param recipients
     * @return
     */
    public CardMimeMailSender setRecipients(List<String> recipients) {
        Assert.notEmpty(recipients, "收件人列表不能为空");
        recipients.forEach(address -> {
            try {
                mimeMessageHelper.addTo(address);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        return this;
    }


    public CardMimeMailSender setMailContent(String templateName, Map<String, Object> contextMap) throws MessagingException {
        context.setVariables(contextMap);
        content = templateEngine.process(templateName, context);
        mimeMessageHelper.setText(content, true);
        return this;
    }


    /**
     * 设置邮件内容
     * @param content
     * @param html
     * @return
     */
    public CardMimeMailSender setMailContent(String content, boolean html) throws MessagingException {
        mimeMessageHelper.setText(content, html);
        return this;
    }

    /**
     * 发送
     */
    public void sendMail() {
        logger.info("send mail to Recipients");
        mailSender.send(mimeMailMessage);
    }
}
