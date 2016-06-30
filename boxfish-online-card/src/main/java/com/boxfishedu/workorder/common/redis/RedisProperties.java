package com.boxfishedu.workorder.common.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {
    private String hostName;
    private String password;
    private int port;
    private int timeout;
}
