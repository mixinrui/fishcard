package com.boxfishedu.card.comment.manage.entity.dto.merger;

import com.boxfishedu.card.comment.manage.entity.enums.CommentCardStatus;

/**
 * Created by LuoLiBing on 16/9/5.
 */
public class CommentCardStatusMerger extends SimpleEnumMerger {

    @Override
    public String getName(Integer value) {
        return CommentCardStatus.getStatus(value);
    }
}
