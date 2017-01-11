package com.boxfishedu.workorder.common.exception;

import com.boxfishedu.workorder.common.bean.PublicClassMessageEnum;

/**
 * Created by LuoLiBing on 17/1/10.
 */
public class PublicClassException extends BusinessException {

    public final PublicClassMessageEnum publicClassMessage;

    public PublicClassException(PublicClassMessageEnum publicClassMessage) {
        this.publicClassMessage = publicClassMessage;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
