package com.boxfishedu.card.comment.manage.entity.dto;

import lombok.Data;

/**
 * Created by LuoLiBing on 16/9/6.
 * 所有老师
 */
@Data
public class TeacherInfo {

    public final static int FREEZE = 0;

    public final static int UNFREEZE = 1;

    private Integer status;

    private String statusDesc;
}
