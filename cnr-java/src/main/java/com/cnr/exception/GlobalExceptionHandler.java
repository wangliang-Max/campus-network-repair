package com.cnr.exception;

import com.cnr.common.ErrorCode;
import com.cnr.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：业务异常 / 参数校验异常 / 系统异常，统一返回 Result
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("参数校验失败");
        return Result.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 只记录异常堆栈，不打印请求参数（避免泄露密码 / Token）
        log.error("系统异常", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR);
    }
}
