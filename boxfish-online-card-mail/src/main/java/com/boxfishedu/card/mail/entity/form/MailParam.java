package com.boxfishedu.card.mail.entity.form;

import lombok.Data;

import java.util.List;

/**
 * Created by LuoLiBing on 17/1/6.
 */
@Data
public class MailParam {

    private List<String> recipients;

    private String content;

    private String subject;

    private String from;

    public Object[] params() {
        // 收件人, 内容, 主题是可变的
        return new Object[] {recipients, content, subject};
    }
}
