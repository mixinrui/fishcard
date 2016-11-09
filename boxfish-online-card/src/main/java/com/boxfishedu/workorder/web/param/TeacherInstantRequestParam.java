package com.boxfishedu.workorder.web.param;

import lombok.Data;

/**
 * Created by hucl on 16/11/3.
 */
@Data
public class TeacherInstantRequestParam {
    private Long teacherId;
    private Long studentId;
    private String day;
    private Long slotId;
    private Long cardId;
}
