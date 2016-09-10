package com.boxfishedu.card.comment.manage.entity.dto;

import com.boxfishedu.card.comment.manage.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Created by LuoLiBing on 16/9/6.
 * 所有老师
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeacherInfo {

    public final static int FREEZE = 0;

    public final static int UNFREEZE = 1;

    public final static TeacherInfo UNKNOW = new UnknowTeacherInfo();

    private Long id;

    private Long teacherId;

    private Integer teacherType;

    private Integer todayReviewCount;

    private Integer todayRemainReviewCount;

    private String teacherName;

    private Boolean freezeStatus;

    public static class UnknowTeacherInfo extends TeacherInfo {
        public UnknowTeacherInfo() {
            super.teacherType = -1;
            super.teacherName = "未知";
            super.todayReviewCount = 0;
            super.todayRemainReviewCount = 0;
            super.freezeStatus = true;
        }
    }

    /**
     * 校验老师是否有权限以及是否有多余的次数
     */
    public void validate(int count) {
        if(freezeStatus) {
            throw new ValidationException("老师为冻结状态");
        }
        if(todayRemainReviewCount < count) {
            throw new ValidationException("次数不够");
        }
    }
}
