package com.boxfishedu.workorder.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by ansel on 16/8/10.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "service.sdk")
public class CommentCardUrlConf {
    private String teacherAbsenceUrl;
    private String getPictureUrl;
    private String pushInfoIrl;
    private String innerTeacherUrl;
    private String scoreUrl;
    private String errorReportMailUrl;
    private String info2TeacherAndStudentUrl;
}
