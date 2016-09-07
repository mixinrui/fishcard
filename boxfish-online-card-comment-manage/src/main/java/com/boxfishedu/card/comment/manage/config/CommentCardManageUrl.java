package com.boxfishedu.card.comment.manage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ansel on 16/9/2.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "service.sdk")
public class CommentCardManageUrl {
    private String teacherStudentBusinessUrl;
    private String innerTeacherUrl;
    private String authenticationUrl;
}
