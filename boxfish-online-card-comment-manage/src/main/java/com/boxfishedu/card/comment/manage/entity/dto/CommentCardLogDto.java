package com.boxfishedu.card.comment.manage.entity.dto;

import com.boxfishedu.card.comment.manage.entity.enums.CommentCardStatus;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/9/12.
 */
public class CommentCardLogDto {

    private LinkedHashMap<String, Date> logs;

    public CommentCardLogDto(CommentCard firstCommentCard, List<CommentCard> commentCardList) {
        if(CollectionUtils.isEmpty(commentCardList)) {
            init(firstCommentCard);
            return;
        }
        // 第一个
        logs = Maps.newLinkedHashMap();
        logs.put("创建", firstCommentCard.getStudentAskTime());

        if(Objects.nonNull(firstCommentCard.getTeacherId())) {
            logs.put("分配老师" + firstCommentCard.getId() + ":" + firstCommentCard.getTeacherName(), new Date());
        }

        // 中间值
        commentCardList.forEach( card -> logs.put("分配老师" + card.getId() + ":" + card.getTeacherName(), new Date()));
        CommentCard latestCommentCard = commentCardList.get(commentCardList.size() - 1);

        // 最后一个
        if(Objects.equals(latestCommentCard.getStatus(), CommentCardStatus.ANSWERED.getCode())) {
            logs.put("已点评", latestCommentCard.getTeacherAnswerTime());
        }

        if(Objects.equals(latestCommentCard.getStatus(), CommentCardStatus.ANSWERED.getCode())) {
            logs.put("学生已评价", latestCommentCard.getStudentCommentTeacherTime());
        }
    }


    public CommentCardLogDto(CommentCard firstCommentCard) {
        init(firstCommentCard);
    }

    private void init(CommentCard firstCommentCard) {
        logs = Maps.newLinkedHashMap();
        logs.put("创建", firstCommentCard.getStudentAskTime());
        if(Objects.nonNull(firstCommentCard.getTeacherId())) {
            logs.put("分配老师" + firstCommentCard.getId() + ":" + firstCommentCard.getTeacherName(), new Date());
        }
        if(Objects.equals(firstCommentCard.getStatus(), CommentCardStatus.ANSWERED.getCode())) {
            logs.put("已点评", firstCommentCard.getTeacherAnswerTime());
        }

        if(Objects.equals(firstCommentCard.getStatus(), CommentCardStatus.ANSWERED.getCode())) {
            logs.put("学生已评价", firstCommentCard.getStudentCommentTeacherTime());
        }
    }

    public LinkedHashMap<String, Date> getLogs() {
        return logs;
    }
}
