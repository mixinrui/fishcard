package com.boxfishedu.workorder.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by LuoLiBing on 16/3/17.
 * 校验错
 */
@Data
@EqualsAndHashCode
public class ValidationException extends BoxfishException {

    public final Integer returnCode = 400;

    private String returnMsg;

    public ValidationException(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public ValidationException() {
        this.returnMsg = "参数错误,请检查!!";
    }
}
