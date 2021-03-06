package com.boxfishedu.card.comment.manage.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.http.HttpStatus;

/**
 * Created by ansel on 16/6/15.
 */
@Data
@EqualsAndHashCode
public class UnauthorizedException extends BoxfishException {

    public final Integer returnCode = HttpStatus.SC_UNAUTHORIZED;

    private String returnMsg;

    public UnauthorizedException(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public UnauthorizedException() {
        returnMsg = "认证失败,拒绝访问!!!";
    }
}
