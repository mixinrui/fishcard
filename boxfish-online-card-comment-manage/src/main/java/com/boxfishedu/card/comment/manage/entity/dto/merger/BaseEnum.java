package com.boxfishedu.card.comment.manage.entity.dto.merger;

import javax.transaction.NotSupportedException;

/**
 * Created by LuoLiBing on 16/9/5.
 */
public interface BaseEnum {
    public static String getName(int id) throws NotSupportedException {
        throw new NotSupportedException();
    }
}
