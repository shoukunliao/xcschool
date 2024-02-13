package com.xuecheng.base.globalExceptionHandles;

import com.xuecheng.base.errorenum.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e){
        log.error("系统异常{}",e.getErrMessage());
        return new RestErrorResponse(e.getErrMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e) {

        log.error("【系统异常】{}",e.getMessage(),e);

        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());

    }

}
