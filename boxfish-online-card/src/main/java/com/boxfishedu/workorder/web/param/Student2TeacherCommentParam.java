package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/7/23.
 */
@Data
public class Student2TeacherCommentParam {
    private Long commentCardId;
    private List<String> forGoodReviews;
    private List<String> forBadReviews;
    private Long studentId;
}
