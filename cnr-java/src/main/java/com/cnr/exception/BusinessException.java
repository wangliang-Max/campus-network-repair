package com.cnr.exception;

import com.cnr.common.ErrorCode;
import lombok.Getter;

/**
 * 业务异常：业务规则不满足时抛出，由全局异常处理器转换为 Result
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
