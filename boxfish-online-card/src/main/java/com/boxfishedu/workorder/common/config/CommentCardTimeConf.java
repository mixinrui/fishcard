package com.boxfishedu.workorder.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ansel on 16/12/8.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "time.task")
public class CommentCardTimeConf {
    private String nodeOne;

    private String nodeTwo;

    private String nodeThree;

    private String nodeFour;
}
