package com.boxfishedu.card.comment.manage.exception;

import com.boxfishedu.beans.view.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class ServiceOnlineBoxfishExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(value = Exception.class)
    public Object processAllException(Exception e) {
        logger.error("controller层:", e);
        final JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setReturnCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        jsonResultModel.setReturnMsg(ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(e.toString()).toString());
        return ResponseEntity.status(jsonResultModel.getReturnCode()).body(jsonResultModel);
    }

    /**
     * 网络异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = NetWorkException.class)
    public Object networkException(NetWorkException e) {
        return boxfishExceptionReturn(e);
    }

    /**
     * 校验异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = ValidationException.class)
    public Object validationException(ValidationException e) {
        return boxfishExceptionReturn(e);
    }

    /**
     * 业务异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = BusinessException.class)
    public Object businessException(BusinessException e) {
        return boxfishExceptionReturn(e);
    }

    /**
     * 认证异常,拒绝访问
     * @param e
     * @return
     */
    @ExceptionHandler(value = UnauthorizedException.class)
    public Object unauthorizedException(UnauthorizedException e) {
        return boxfishExceptionReturn(e);
    }

    private Object boxfishExceptionReturn(BoxfishException e) {
        logger.error("controller层:BoxFishException:{}", e.getReturnMsg(), e);
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setReturnCode(e.getReturnCode());
        jsonResultModel.setReturnMsg(e.getReturnMsg());
        return ResponseEntity.status(e.getReturnCode()).body(jsonResultModel);
    }

}
