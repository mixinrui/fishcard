package com.boxfishedu.workorder.web.view.course;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by hucl on 16/6/12.
 */
@Data
public class RecommandCourseView {
    private String courseId;
    private String courseName;
    private String englishName;
    @JsonProperty(value = "type")
    private String courseType;
    private String publicDate;
    private Integer bundleOrder;
    private String difficulty;
    private String cover;

}
