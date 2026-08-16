package com.example.logindemo.exception;

import com.example.logindemo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理表单提交时的参数校验异常
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Result.error(message);
    }

    // 处理 JSON 提交时的参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Result.error(message);
    }

    // 处理其他所有异常
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(e.getMessage());
    }
}