package com.boxfishedu.card.mail.dto;

import com.boxfishedu.card.mail.dto.merger.TeacherInfoRpcMerger;
import com.boxfishedu.card.mail.dto.merger.TimeIntervalMerger;
import lombok.Data;
import org.jdto.annotation.Source;
import org.jdto.mergers.DateFormatMerger;

/**
 * Created by LuoLiBing on 16/9/1.
 */
@Data
public class CommentCardDto {

    private Long id;

    private Long studentId;

    @Source(value = "teacherId", merger = TeacherInfoRpcMerger.class)
    private TeacherInfo teacherInfo;

    @Source(value = "studentAskTime", merger = DateFormatMerger.class, mergerParam = "yyyy-MM-dd HH:mm:ss")
    private String studentAskTime;

    @Source(value = "studentAskTime", merger = TimeIntervalMerger.class)
    private Integer hours;
}
