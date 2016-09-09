package com.boxfishedu.card.comment.manage.entity.dto.merger;

import org.jdto.SinglePropertyValueMerger;

/**
 * Created by LuoLiBing on 16/9/9.
 */
public abstract class BaseStringMerger<T> implements SinglePropertyValueMerger<String, T> {

    @Override
    public String mergeObjects(T value, String[] extraParam) {
        return append(value, extraParam);
    }

    @Override
    public boolean isRestoreSupported(String[] params) {
        return false;
    }

    @Override
    public T restoreObject(String object, String[] params) {
        return null;
    }

    public abstract String append(T value, String[] extraParam);
}
