package com.boxfishedu.card.mail.dto.merger;

import org.jdto.SinglePropertyValueMerger;

/**
 * Created by LuoLiBing on 16/9/1.
 */
public abstract class BaseRpcMerger<T, K> implements SinglePropertyValueMerger<T, K> {

    @Override
    public T mergeObjects(K value, String[] extraParam) {
        return rpcCall(value);
    }

    @Override
    public boolean isRestoreSupported(String[] params) {
        return false;
    }

    @Override
    public K restoreObject(T object, String[] params) {
        return null;
    }

    public abstract T rpcCall(K identify);
}

