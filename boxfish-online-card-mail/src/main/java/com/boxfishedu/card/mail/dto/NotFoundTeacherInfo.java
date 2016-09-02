package com.boxfishedu.card.mail.dto;

/**
 * Created by LuoLiBing on 16/9/1.
 */
public class NotFoundTeacherInfo extends TeacherInfo {
    public NotFoundTeacherInfo(Long teacherId) {
        super.setTeacherId(teacherId);
        super.setName("空");
        super.setEmail("空");
    }
}
