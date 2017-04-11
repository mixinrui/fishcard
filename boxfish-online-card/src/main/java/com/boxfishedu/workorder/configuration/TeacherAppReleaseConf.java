package com.boxfishedu.workorder.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ansel on 2017/4/11.
 */
@Data
@Configuration
@ConfigurationProperties(value = "teacher.app")
public class TeacherAppReleaseConf {
    private String getReleaseUrl;
}
