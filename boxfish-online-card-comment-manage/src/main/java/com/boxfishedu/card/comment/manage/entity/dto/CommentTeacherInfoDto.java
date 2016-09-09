package com.boxfishedu.card.comment.manage.entity.dto;

import com.boxfishedu.card.comment.manage.entity.dto.merger.TeacherInfoRpcMerger;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.jdto.annotation.Source;

/**
 * Created by LuoLiBing on 16/9/9.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentTeacherInfoDto extends CommentTeacherInfo {

    @Source(value = "teacherId", merger = TeacherInfoRpcMerger.class)
    private TeacherInfo teacherInfo;
}
