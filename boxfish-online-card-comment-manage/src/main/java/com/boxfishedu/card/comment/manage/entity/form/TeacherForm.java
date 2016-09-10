package com.boxfishedu.card.comment.manage.entity.form;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Objects;

/**
 * Created by ansel on 16/9/5.
 */

@Data
public class TeacherForm {
    private Long teacherId;
    private String teacherName;
    private Integer teacherStatus;

    public MultiValueMap<String,String> createValueMap() {
        MultiValueMap<String,String> result = new LinkedMultiValueMap<>();
        if(Objects.nonNull(teacherId)) {
            result.add("teacherId", teacherId.toString());
        }

        if(StringUtils.isNotBlank(teacherName)) {
            result.add("teacherName", teacherName);
        }

        if(Objects.nonNull(teacherStatus)) {
            result.add("teacherStatus", teacherStatus.toString());
        }
        return result;
    }
}
