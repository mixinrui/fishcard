package com.boxfishedu.card.comment.manage.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

/**
 * Created by liuzhihao on 16/3/14.
 * boxfish异常超类,默认返回200
 */
@Data
@EqualsAndHashCode
public class BoxfishException extends RuntimeException {

    private final static HttpStatus status = HttpStatus.OK;

    private String returnMsg;

    protected BoxfishException(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public BoxfishException() {
    }

    public Integer getReturnCode() {
        return status.value();
    }

    public String getReturnMsg() {
        return returnMsg;
    }

}
