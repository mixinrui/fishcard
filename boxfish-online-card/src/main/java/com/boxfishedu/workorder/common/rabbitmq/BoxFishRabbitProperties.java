package com.boxfishedu.workorder.common.rabbitmq;

import com.boxfishedu.workorder.common.util.ConstantUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Component(ConstantUtil.RABBITMQ_PROPERTIES_BEAN)
public class BoxFishRabbitProperties {
    private Integer port;
    private String username;
    private String password;
    private String virtualHost;
    private String address;
}
