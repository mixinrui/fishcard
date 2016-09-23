package com.boxfishedu.workorder.web.view.course;

import lombok.Data;

/**
 * Created by hucl on 16/6/12.
 */
@Data
public class RecommandCourseView {
    private String courseId;
    private String courseName;
    private String englishName;
    private String courseType;
    private String publicDate;
    private Integer bundleOrder;
    private Integer difficulty;
    private String cover;
}
