package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.exception.ValidationException;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/9/24.
 */
public final class BoxfishAsserts {

    public static void notNull(Object object, String message) {
        if (Objects.isNull(object)) {
            throw new ValidationException(message);
        }
    }

    public static void notEmpty(Collection collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ValidationException(message);
        }
    }
}
