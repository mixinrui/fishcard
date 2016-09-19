package com.boxfishedu.workorder.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

/**
 * Created by LuoLiBing on 16/3/17.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotFoundException extends BoxfishException {

    public final Integer returnCode = HttpStatus.NOT_FOUND.value();

    private String returnMsg;

    public NotFoundException(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public NotFoundException() {
        returnMsg = "无对应请求";
    }
}
