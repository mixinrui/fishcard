package com.boxfishedu.workorder.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * Created by hucl on 16/9/24.
 */
@Data
public class AccountCourseBean {
    private Integer leftAmount;
    private CardCourseInfo courseInfo;
    //2012-11-12 11:11:11
    private Date dateInfo;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CardCourseInfo{
        private String courseId;
        private String courseName;
        private String courseType;
        private Integer difficulty;
        private String thumbnail;
        private Integer isFreeze;
        private Integer studentReadFlag;
        private Integer status;
    }

}


