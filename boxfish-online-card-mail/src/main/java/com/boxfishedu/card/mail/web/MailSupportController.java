package com.boxfishedu.card.mail.web;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.mail.config.BaseMailConfig;
import com.boxfishedu.card.mail.entity.form.MailParam;
import com.boxfishedu.card.mail.service.CardMimeMailSender;
import com.boxfishedu.card.mail.utils.SignatureUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.mail.MessagingException;

/**
 * Created by LuoLiBing on 17/1/6.
 */
@RequestMapping(value = "/mail")
public class MailSupportController {

    private final static Logger logger = LoggerFactory.getLogger(MailSupportController.class);


    @Autowired
    private BaseMailConfig mailConfig;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public Object sendMail(@RequestBody MailParam mailParam, String token) throws MessagingException {
        if(!checkSign(token, mailParam)) {
            return JsonResultModel.newJsonResultModel("签名失败");
        }

        mailParam.setFrom(mailConfig.getSender());
        logger.info("send message [{}]", mailParam);
        new CardMimeMailSender()
                .createMimeMail(createMailMessage(mailParam))
                .setRecipients(mailParam.getRecipients())
                .setMailContent(mailParam.getContent())
                .sendMail();
        return JsonResultModel.newJsonResultModel();
    }

    private SimpleMailMessage createMailMessage(MailParam mailParam) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailParam.getFrom());
        mailMessage.setSubject(mailMessage.getSubject());
        return mailMessage;
    }

    private boolean checkSign(String token, MailParam mailParam) {
        String tk = SignatureUtils.sign(mailConfig.getToken(), mailParam.params());
        return StringUtils.equals(token, tk);
    }
}