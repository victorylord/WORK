package com.panda.sport.rcs.trade.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.panda.sport.rcs.vo.HttpResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 	异常处理器
 *
 * @author jordan
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    /**
     * 	默认异常
     *
     * @param exception
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    public HttpResponse<?> exceptionHandler(Exception exception, HttpServletRequest request) {
    	log.error(request.getRequestURI() + "====接口异常", exception);
    	return HttpResponse.failToMsg("服务器错误");
    }


}
