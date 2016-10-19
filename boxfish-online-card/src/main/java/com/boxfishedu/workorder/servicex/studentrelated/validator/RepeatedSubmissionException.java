package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.exception.ValidationException;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by LuoLiBing on 16/10/19.
 */
@Data
@EqualsAndHashCode
public class RepeatedSubmissionException extends ValidationException {

    public final Integer returnCode = 400;

    private String returnMsg;

    public RepeatedSubmissionException(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public RepeatedSubmissionException() {
        this.returnMsg = "重复提交,请稍候...";
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
