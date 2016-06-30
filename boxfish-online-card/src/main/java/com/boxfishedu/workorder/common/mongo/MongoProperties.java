package com.boxfishedu.workorder.common.mongo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.mongo")
public class MongoProperties {

    private String serverAddress;

    private String dbName;

    private String username;

    private String password;
}
