package com.boxfishedu.workorder.web.param;

import lombok.Data;

/**
 * Created by hucl on 16/5/16.
 */
@Data
public class TeacherChangeParam {
    private Long workOrderId;
    private Long teacherId;
    private String teacherName;
    private String changeReason; /**  如果老师请假  takeforleave  change **/
}
