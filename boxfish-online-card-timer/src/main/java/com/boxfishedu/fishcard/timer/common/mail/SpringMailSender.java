package com.boxfishedu.fishcard.timer.common.mail;


import com.boxfishedu.fishcard.timer.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import java.util.Date;

@Component
public class SpringMailSender {

    // Spring的邮件工具类，实现了MailSender和JavaMailSender接口
    private JavaMailSenderImpl mailSender;

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private Integer port;
    @Value("${spring.mail.username}")
    private String userName;
    @Value("${spring.mail.password}")
    private String passWord;
    @Value("${spring.mail.tousers}")
    private String toUsers;

    @PostConstruct
    private void init() {
        mailSender = new JavaMailSenderImpl();
        // 设置参数
        mailSender.setHost(this.host);
        mailSender.setPort(this.port);
        mailSender.setUsername(this.userName);
        mailSender.setPassword(this.passWord);
    }

    public void send(String subject, String text) throws Exception {
        //使用JavaMail的MimeMessage，支付更加复杂的邮件格式和内容
        MimeMessage msg = mailSender.createMimeMessage();
        //创建MimeMessageHelper对象
        String[] toUsersArr = toUsers.split(",");
        for (String toUser : toUsersArr) {
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            //使用辅助类MimeMessage设定参数
            helper.setFrom(mailSender.getUsername());
            helper.setTo(toUser);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(msg);
        }
    }

    public static void main(String[] args) throws Exception {
    }


    public void richContentSend() throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(msg, true);

        helper.setFrom(mailSender.getUsername());
        helper.setTo("393269748@qq.com");
        helper.setSubject("教师分配预警邮件-" + DateUtil.Date2String(new Date()));
        //第二个参数true，表示text的内容为html，然后注意<img/>标签，src='cid:file'，'cid'是 contentId的缩写，'file'是一个标记，需要在后面的代码中调用MimeMessageHelper的addInline方法替代成文件
        helper.setText(
                "<body><p>Hello Html Email</p><img src='cid:file'/></body>",
                true);
        mailSender.send(msg);
    }
}