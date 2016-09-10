package com.boxfishedu.card.comment.manage.entity.form;

import lombok.Data;

import java.util.Date;

/**
 * Created by ansel on 16/9/2.
 */
@Data
public class FromTeacherStudentForm {
    private boolean operation;
    private Date operationTime;
    private String operator;
}
