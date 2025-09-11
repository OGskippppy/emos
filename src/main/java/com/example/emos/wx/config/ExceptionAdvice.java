package com.example.emos.wx.config;

import com.example.emos.wx.exception.EmoException;
import com.example.emos.wx.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {
        if (e instanceof MethodArgumentNotValidException){
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            return Result.error(ex.getBindingResult().getFieldError().getDefaultMessage());
        } else if (e instanceof EmoException) {
            EmoException ex = (EmoException) e;
            return Result.error(ex.getMsg());
        } else if (e instanceof UnauthorizedException) {
            return Result.error("未认证异常");
        }else {
            return Result.error("后端执行异常");
        }
    }
}
