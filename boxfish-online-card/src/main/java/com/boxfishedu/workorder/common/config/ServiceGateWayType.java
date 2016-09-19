package com.boxfishedu.workorder.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ansel on 16/9/19.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "service.gateway")
public class ServiceGateWayType {
    private String type;
}
