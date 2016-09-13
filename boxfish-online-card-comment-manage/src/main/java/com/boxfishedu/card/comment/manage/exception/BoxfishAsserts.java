package com.boxfishedu.card.comment.manage.exception;

import org.springframework.util.Assert;

/**
 * Created by LuoLiBing on 16/9/13.
 */
public class BoxfishAsserts extends Assert {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
    }
}
