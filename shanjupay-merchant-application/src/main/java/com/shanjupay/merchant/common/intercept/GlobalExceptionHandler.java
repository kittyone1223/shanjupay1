package com.shanjupay.merchant.common.intercept;


import com.shanjupay.common.domain.BusinessException;

import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.ErrorCode;
import com.shanjupay.common.domain.RestErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@ControllerAdvice  // 全局异常处理  (和ExceptionHandler)
public class GlobalExceptionHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse processHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        // 解析异常信息
        // 如果是系统自定义异常  直接去除errcode 和errormessage
        if (e instanceof BusinessException) {
            // 解析系统自定义异常信息
            LOGGER.info(e.getMessage(),e);
            BusinessException businessException = (BusinessException) e;
            ErrorCode errorCode = businessException.getErrorCode();
            return new RestErrorResponse(errorCode.getDesc(), String.valueOf(errorCode.getCode()));
        }
        // 如果不是系统自定义的异常   未知的错误
        LOGGER.error("系统异常",e);
        //return new RestErrorResponse(CommonErrorCode.UNKNOWN.getDesc(),String.valueOf(CommonErrorCode.UNKNOWN.getCode()));
        return new RestErrorResponse(String.valueOf(CommonErrorCode.UNKNOWN.getCode()),CommonErrorCode.UNKNOWN.getDesc());
    }

}
