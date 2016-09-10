package com.boxfishedu.card.comment.manage.entity.dto.merger;

import com.boxfishedu.card.comment.manage.entity.enums.CommentCardDtoStatus;
import com.boxfishedu.card.comment.manage.entity.enums.CommentCardStatus;
import com.boxfishedu.card.comment.manage.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jdto.MultiPropertyValueMerger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/9/8.
 * 点评状态计算,通过status和studentAskTime来判断状态
 * NOTANSWER0(0,"未点评"),NOTANSWER1(1, "未点评"), NOTANSWER2(2, "未点评"), ANSWERED(3, "已点评"), TIMEOUT(4, "超时未点评"), EVALUATED(5, "学生已评价");
 *
 * 1 status = 400  ANSWERED
 * 2 status = 600  EVALUATED
 * 3 status <=300  studentAskTime<24h       NOTANSWER0
 * 4 status <=300  36h>studentAskTime>24h   NOTANSWER1
 * 5 status <=300  48h>studentAskTime>36h   NOTANSWER2
 * 6 已补次点评,需要新增状态
 */
public class CommentCardDtoStatusMerger implements MultiPropertyValueMerger<Object> {

    @Override
    public Object mergeObjects(List<Object> values, String[] extraParam) {
        if(CollectionUtils.isEmpty(values) || values.size() < 2) {
            return CommentCardDtoStatus.UNKNOW;
        }
        if(Objects.isNull(values.get(0))
                || Objects.isNull(values.get(1))) {
            return CommentCardDtoStatus.UNKNOW;
        }

        Integer status = (Integer) values.get(0);
        CommentCardStatus commentCardStatus = CommentCardStatus.resolve(status);
        if(Objects.isNull(commentCardStatus)) {
            return CommentCardDtoStatus.UNKNOW;
        }

        Date studentAskTime = (Date) values.get(1);
        switch (commentCardStatus) {
            case ASKED: case REQUEST_ASSIGN_TEACHER: case ASSIGNED_TEACHER: return getNotAnswerStatus(studentAskTime);
            case ANSWERED: return CommentCardDtoStatus.ANSWERED;
            case STUDENT_COMMENT_TO_TEACHER: return CommentCardDtoStatus.EVALUATED;
            default: return CommentCardDtoStatus.UNKNOW;
        }
    }

    private CommentCardDtoStatus getNotAnswerStatus(Date studentAskTime) {
        Duration duration = Duration.between(DateUtils.parseFromDate(studentAskTime), LocalDateTime.now());
        long hours = duration.toHours();
        if(hours < 24) {
            return CommentCardDtoStatus.NOTANSWER0;
        } else if(hours < 36) {
            return CommentCardDtoStatus.NOTANSWER1;
        } else {
            return CommentCardDtoStatus.NOTANSWER2;
        }
    }

}
