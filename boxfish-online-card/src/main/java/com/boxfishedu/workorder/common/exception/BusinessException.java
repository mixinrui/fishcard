package com.boxfishedu.workorder.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

/**
 * Created by LuoLiBing on 16/3/17.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends BoxfishException {

    public final Integer returnCode = HttpStatus.BAD_REQUEST.value();

    private String returnMsg;

    public BusinessException(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public BusinessException() {
        returnMsg = "无效的请求,请重试";
    }
}
