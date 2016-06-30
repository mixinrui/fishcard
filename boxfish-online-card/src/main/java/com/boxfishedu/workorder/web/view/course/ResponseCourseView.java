package com.boxfishedu.workorder.web.view.course;

import com.boxfishedu.workorder.web.view.base.ResponseBaseView;
import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/4/12.
 */
@Data
public class ResponseCourseView extends ResponseBaseView {
    private List<CourseView> data;
}
