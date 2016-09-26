package com.boxfishedu.workorder.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by hucl on 16/9/24.
 */
@Data
public class AccountCourseBean {
    private Integer leftAmount;
    private CardCourseInfo courseInfo;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CardCourseInfo{
        private String courseId;
        private String courseName;
        private String courseType;
        private Integer difficulty;
        private String thumbnail;
        private Integer isFreeze;
        private Integer status;
    }
}


