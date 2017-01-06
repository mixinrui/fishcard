package com.boxfishedu.card.mail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by LuoLiBing on 17/1/6.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mail")
public class BaseMailConfig {

    private String sender;

    private String token;
}
