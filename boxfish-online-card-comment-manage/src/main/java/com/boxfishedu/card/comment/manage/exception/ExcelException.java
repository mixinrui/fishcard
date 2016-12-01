package com.boxfishedu.card.comment.manage.exception;

/**
 * Created by ansel on 16/11/30.
 */
public class ExcelException extends RuntimeException {

    public ExcelException() {
    }

    public ExcelException(String message) {
        super(message);
    }

    public ExcelException(Throwable cause) {
        super(cause);
    }
}
