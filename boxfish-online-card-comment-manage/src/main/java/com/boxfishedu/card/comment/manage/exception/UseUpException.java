package com.boxfishedu.card.comment.manage.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.http.HttpStatus;

/**
 * Created by ansel on 16/7/21.
 */
@Data
@EqualsAndHashCode
public class UseUpException extends BoxfishException{
    public final Integer returnCode = HttpStatus.SC_UNAUTHORIZED;

    private String returnMsg;

    public UseUpException(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public UseUpException() {
        returnMsg = "外教点评次数用尽,请先购买点评!!!";
    }
}
