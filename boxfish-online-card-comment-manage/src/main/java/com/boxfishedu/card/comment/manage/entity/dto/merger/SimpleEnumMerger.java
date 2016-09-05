package com.boxfishedu.card.comment.manage.entity.dto.merger;

import org.jdto.SinglePropertyValueMerger;

/**
 * Created by LuoLiBing on 16/9/5.
 */
public abstract class SimpleEnumMerger implements SinglePropertyValueMerger<String, Integer> {

    @Override
    public String mergeObjects(Integer value, String[] extraParam) {
        if(value == null) {
            return null;
        }
        if(extraParam == null || extraParam.length == 0) {
            return null;
        }

        return getName(value);
    }

    @Override
    public boolean isRestoreSupported(String[] strings) {
        return false;
    }

    @Override
    public Integer restoreObject(String s, String[] strings) {
        return null;
    }

    public abstract String getName(Integer value);
}
